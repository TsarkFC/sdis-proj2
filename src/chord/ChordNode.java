package chord;

import constants.Constants;
import filehandler.FileHandler;
import messages.MessageSender;
import messages.Messages;
import messages.protocol.PutChunk;
import peer.Peer;
import peer.metadata.ChunkMetadata;
import ssl.SslSender;
import utils.AddressPort;
import utils.AddressPortList;
import utils.SerializeChordData;
import utils.Utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ChordNode {
    /**
     * Peer corresponding to the node in the chord
     */
    private Peer peer;

    /**
     * True if peer is the started the chord ring
     */
    private boolean isBoot;

    /**
     * The id of the peer in the ring
     */
    private final int id;

    /**
     * Finger table containing peer's successors
     */
    private List<ChordNodeData> fingerTable = new ArrayList<>();

    /**
     * Peer own data on the ring
     */
    private ChordNodeData data;

    /**
     * Peer previous predecessor data
     */
    private ChordNodeData previousPredecessor;

    /**
     * Peer predecessor data
     */
    private ChordNodeData predecessor;

    /**
     * Peer successor data
     */
    private ChordNodeData successor;

    /**
     * Successor's successor, used in case of failure
     */
    private ChordNodeData safeSuccessor;

    /**
     * Object containing ip addresses and ports of all the 4 channels
     */
    private final AddressPortList addressPortList;

    /**
     * Used in fixFingers, to know which entry to update
     */
    private int next = 0;

    public ChordNode(Peer peer) {
        this.peer = peer;
        this.addressPortList = peer.getArgs().getAddressPortList();
        this.id = generateHash(addressPortList.getChordAddressPort().getAddress(), addressPortList.getChordAddressPort().getPort());
        this.isBoot = peer.getArgs().isBoot();
        this.data = new ChordNodeData(id, addressPortList);
        System.out.println("[CHORD] Node was created id: " + id);

        //TODO Nao faz sentido por tudo no mesmo?
        //TODO: Verificar tempos
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(Constants.numThreads);
        executor.scheduleAtFixedRate(this::stabilize, Constants.executorDelay, Constants.executorDelay, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::fixFingers, Constants.executorDelay, Constants.executorDelay, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::fixSafeSuccessor, Constants.executorDelay, Constants.executorDelay, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::checkPredecessor, Constants.executorDelay, Constants.executorDelay, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::checkSuccessor, Constants.executorDelay, Constants.executorDelay, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::printChordInfo, Constants.executorDelay, Constants.executorDelay, TimeUnit.MILLISECONDS);
        System.out.println("Executors ready!");
    }

    public void create() {
        predecessor = null;
        successor = this.data;
        System.out.println("[CHORD] node ready");
    }

    public void join(String chordAddress, int chordPort) {
        predecessor = null;

        String message = Messages.JOIN + " " + this.id + "\r\n\r\n";
        byte[] successorInfoCRLF = sendMessageAndWait(message.getBytes(), chordAddress, chordPort);
        if (successorInfoCRLF == null) {
            System.out.println("[CHORD] join failed");
            return;
        }
        byte[] successorInfo = Utils.readUntilCRLF(successorInfoCRLF);
        successor = new SerializeChordData().deserialize(successorInfo);
        System.out.println("[CHORD] node ready");
    }

    public void printChordInfo() {
        if (successor != null) {
            System.out.println("Peer " + this.id + " successor: " + successor.getId());
        } else {
            System.out.println("Peer " + this.id + " successor: null");
        }
        if (predecessor != null) {
            System.out.println("Peer " + this.id + " predecessor: " + predecessor.getId());
        } else {
            System.out.println("Peer " + this.id + " predecessor: null");
        }

    }

    public void stabilize() {
        if (successor == null) return;

        String message = Messages.GET_PREDECESSOR + "\r\n\r\n";
        AddressPort addressPort = successor.getAddressPortList().getChordAddressPort();
        byte[] predecessorInfoCRLF = sendMessageAndWait(message.getBytes(), addressPort.getAddress(), addressPort.getPort());
        if (predecessorInfoCRLF == null) {
            System.out.println("[CHORD] could not stabilize");
            return;
        }
        byte[] predecessorInfo = Utils.readUntilCRLF(predecessorInfoCRLF);
        ChordNodeData x = new SerializeChordData().deserialize(predecessorInfo);

        if (x != null && isInInterval(x.getId(), this.id, successor.getId())) {
            successor = x;
        }

        // Sending notify message
        byte[] serialized = new SerializeChordData().serialize(this.data);
        byte[] notifyMessage = Utils.concatBuffer((Messages.NOTIFY + " ").getBytes(), serialized);
        notifyMessage = Utils.addCRLF(notifyMessage);
        addressPort = successor.getAddressPortList().getChordAddressPort();
        sendMessage(notifyMessage, addressPort.getAddress(), addressPort.getPort());
    }

    public void receiveNotify(ChordNodeData n) {
        if (this.id == n.getId()) return;

        if (predecessor == null || isInInterval(n.getId(), predecessor.getId(), this.id)) {
            if (predecessor == null || predecessor.getId() != n.getId()) {
                this.previousPredecessor = predecessor;
                predecessor = n;
                if (this.previousPredecessor != null) fileTransfer();
            }
        }
    }

    private void fileTransfer() {
        for (Map.Entry<String, ChunkMetadata> entry : this.peer.getMetadata().getStoredChunksMetadata().getChunksInfo().entrySet()) {
            ChunkMetadata chunkMetadata = entry.getValue();
            String fileIdStr = FileHandler.createChunkFileId(chunkMetadata.getFileId(), chunkMetadata.getChunkNum(), chunkMetadata.getRepDgr());
            int fileId = peer.getChordNode().generateHash(fileIdStr);

            if (isInInterval(fileId, this.id, predecessor.getId())) {
                String chunkPath = FileHandler.getChunkPath(this.peer.getFileSystem(), chunkMetadata.getFileId(), chunkMetadata.getChunkNum());
                FileHandler fileHandler = new FileHandler(FileHandler.getFile(chunkPath));
                AddressPort addressPort = this.addressPortList.getMcAddressPort();
                PutChunk backupMsg = new PutChunk(addressPort.getAddress(), addressPort.getPort(), chunkMetadata.getFileId(),
                        chunkMetadata.getChunkNum(), 1, fileHandler.getChunkFileData());

                byte[] message = backupMsg.getBytes();
                AddressPort predecessorAddrPort = predecessor.getAddressPortList().getMdbAddressPort();
                MessageSender.sendTCPMessage(predecessorAddrPort.getAddress(), predecessorAddrPort.getPort(), message);
                FileHandler.deleteFile(chunkMetadata.getFileId() + "/" + chunkMetadata.getChunkNum(), this.peer.getFileSystem());

                peer.getMetadata().getStoredChunksMetadata().deleteChunk(chunkMetadata.getFileId(), chunkMetadata.getChunkNum());
            }
        }
    }

    /**
     * Updates finger table periodically
     */
    public void fixFingers() {
        next++;
        if (next > Chord.m - 1) next = 0;

        ChordNodeData node = findSuccessor(calculateKey(this.id, next));

        if (next < fingerTable.size()) fingerTable.set(next, node);
        else fingerTable.add(node);

        logFingerTable();
    }

    /**
     * Updates successors periodically
     */
    public void fixSafeSuccessor() {
        System.out.println("[CHORD] fix safe successor called, current safe = " +  ((safeSuccessor == null) ? "null" : safeSuccessor.getId()));
        if (successor == null) return;
        ChordNodeData newSuccessor = findSuccessor(successor.getId() + 1);
        if (safeSuccessor == null || newSuccessor != null && safeSuccessor.getId() != newSuccessor.getId()) {
            safeSuccessor = newSuccessor;
            System.out.println("[CHORD] safe successor updated!");
        }
    }

    public void checkPredecessor() {
        if (predecessor == null) return;
        AddressPort addressPort = predecessor.getAddressPortList().getChordAddressPort();
        if (!new SslSender(addressPort.getAddress(), addressPort.getPort(), null).connect()) {
            System.out.println("[CHORD] error connecting to predecessor...");
            predecessor = null;
        }
    }

    public void checkSuccessor() {
        if (successor == null || successor.getId() == this.id) return;
        AddressPort addressPort = successor.getAddressPortList().getChordAddressPort();
        if (!new SslSender(addressPort.getAddress(), addressPort.getPort(), null).connect()) {
            System.out.println("[CHORD] error connecting to successor...");
            successor = safeSuccessor;
        }
    }

    public ChordNodeData findSuccessor(Integer id) {
        if (this.successor != null &&
                isInInterval(id, this.id, this.successor.getId() + 1)) {
            return this.successor;
        } else {
            ChordNodeData precedingNode = closestPrecedingNode(id);
            if (precedingNode == null) return this.data;

            // Sending Get Successor message
            String message = Messages.GET_SUCCESSOR + " " + id + "\r\n\r\n";
            AddressPort addressPort = precedingNode.getAddressPortList().getChordAddressPort();
            byte[] response = sendMessageAndWait(message.getBytes(), addressPort.getAddress(), addressPort.getPort());
            if (response == null) {
                System.out.println("[CHORD] error processing successor");
                return null;
            }
            return new SerializeChordData().deserialize(response);
        }
    }

    public ChordNodeData closestPrecedingNode(int id) {
        for (int i = fingerTable.size() - 1; i >= 0; i--) {
            ChordNodeData node = fingerTable.get(i);
            if (node == null) continue;
            if (isInInterval(node.getId(), this.id, id)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Sends message through SSLEngine and processes response
     *
     * @param message
     * @param address
     * @param port
     * @return The response received from SSLEngine server
     */
    private byte[] sendMessageAndWait(byte[] message, String address, Integer port) {
        SslSender sender = new SslSender(address, port, message);
        sender.connect();
        sender.write(message);
        byte[] response = sender.read();
        sender.shutdown();
        return response;
    }

    private void sendMessage(byte[] message, String address, Integer port) {
        SslSender sender = new SslSender(address, port, message);
        sender.connect();
        sender.write(message);
    }

    public int generateHash(String ipAddr, int port) {
        String id = ipAddr + port;
        return generateHash(id);
    }

    public int generateHash(String name) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.reset();
            messageDigest.update(name.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if (messageDigest == null) return -1;
        byte[] digest = messageDigest.digest();
        String sha1 = String.format("%040x", new BigInteger(1, digest));
        int hashCode = sha1.hashCode();
        hashCode = hashCode < 0 ? -hashCode : hashCode;

        //Truncar ate ter m bits
        return (int) (hashCode % Math.pow(2, Chord.m));
    }

    public int getId() {
        return id;
    }

    public Peer getPeer() {
        return this.peer;
    }

    public ChordNodeData getData() {
        return data;
    }

    public ChordNodeData getPredecessor() {
        return this.predecessor;
    }

    public boolean isBoot() {
        return isBoot;
    }

    public ChordNodeData getSuccessor() {
        return successor;
    }

    public void setSuccessor(ChordNodeData successor) {
        this.successor = successor;
    }

    public AddressPortList getAddressPortList() {
        return addressPortList;
    }

    public boolean isInInterval(int element, int lowerBound, int upperBound) {
        if (lowerBound < upperBound)
            return element > lowerBound && element < upperBound;
        return element > lowerBound || element < upperBound;
    }

    private void logFingerTable() {
        int count = 0;
        System.out.println("----------------");
        for (ChordNodeData node : fingerTable) {
            System.out.print("key: " + calculateKey(this.id, count++));
            System.out.println(" | node id: " + node.getId());
        }
        System.out.println("----------------");
    }

    private int calculateKey(int id, int tablePos) {
        int key = id + (int) Math.pow(2, tablePos);
        return key % (int) Math.pow(2, Chord.m);
    }
}
