package chord;

import constants.Constants;
import messages.Messages;
import peer.Peer;
import ssl.SslSender;
import utils.AddressPort;
import utils.AddressPortList;
import utils.SerializeChordData;
import utils.Utils;
import peer.metadata.ChunkMetadata;
import protocol.BackupProtocol;
import filehandler.FileHandler;
import messages.protocol.PutChunk;
import messages.MessageSender;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;



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
     * Peer predecessor data
     */
    private ChordNodeData predecessor;

    /**
     * Peer successor data
     */
    private ChordNodeData successor;

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
        System.out.println("Chord Peer was created id: " + id);
        //TODO Nao faz sentido por tudo no mesmo?
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(Constants.numThreads);
        executor.scheduleAtFixedRate(this::stabilize, Constants.executorDelay, Constants.executorDelay, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::fixFingers, Constants.executorDelay, Constants.executorDelay, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::printChordInfo, Constants.executorDelay, Constants.executorDelay, TimeUnit.MILLISECONDS);
        System.out.println("Executors ready!");
    }

    public void create() {
        predecessor = null;
        successor = this.data;
    }

    public void join(String chordAddress, int chordPort) {
        predecessor = null;

        String message = Messages.JOIN + " " + this.id + "\r\n\r\n";
        byte[] successorInfoCRLF = sendMessage(message.getBytes(), chordAddress, chordPort);
        byte[] successorInfo = Utils.readUntilCRLF(successorInfoCRLF);
        successor = new SerializeChordData().deserialize(successorInfo);

        System.out.println("Successor id: " + successor.getId());
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
        byte[] predecessorInfoCRLF = sendMessage(message.getBytes(), addressPort.getAddress(), addressPort.getPort());
        byte[] predecessorInfo = Utils.readUntilCRLF(predecessorInfoCRLF);
        ChordNodeData x = new SerializeChordData().deserialize(predecessorInfo);

        if (x != null && isInInterval(x.getId(), this.id, successor.getId())) {
            successor = x;
            //System.out.println("[Stabilize] peer " + id + ": updated successor, is now: " + successor.getId());
        }

        // Sending notify message
        byte[] serialized = new SerializeChordData().serialize(this.data);
        byte[] crlf = Utils.getDoubleCRLF();
        int bufferSize = Messages.NOTIFY.length() + serialized.length + crlf.length + 1;

        byte[] notifyMessage = new byte[bufferSize];
        System.arraycopy((Messages.NOTIFY + " ").getBytes(), 0, notifyMessage, 0, Messages.NOTIFY.length() + 1);
        System.arraycopy(serialized, 0, notifyMessage, Messages.NOTIFY.length() + 1, serialized.length);
        System.arraycopy(crlf, 0, notifyMessage, Messages.NOTIFY.length() + 1 + serialized.length, crlf.length);

        addressPort = successor.getAddressPortList().getChordAddressPort();

        sendMessage(notifyMessage, addressPort.getAddress(), addressPort.getPort());
    }

    public void receiveNotify(ChordNodeData n) {
        if (this.id == n.getId()) return;

        if (predecessor == null || isInInterval(n.getId(), predecessor.getId(), this.id)) {
            predecessor = n;
            //System.out.println("[Notify] peer " + this.id + ": updated predecessor, is now: " + predecessor.getId());

            for (Map.Entry<String, ChunkMetadata> entry : this.peer.getMetadata().getStoredChunksMetadata().getChunksInfo().entrySet()) {
                String key = entry.getKey();
                ChunkMetadata chunkMetadata = entry.getValue();
                int fileIdHashed = generateHash(chunkMetadata.getFileId());
                System.out.println("FileIdHashed: " + fileIdHashed + " PredecessorId: " + predecessor.getId());
                if (!isInInterval(fileIdHashed, predecessor.getId(), this.id)) {
                    FileHandler fileHandler = new FileHandler(FileHandler.getFile(FileHandler.getChunkPath(this.peer.getFileSystem(), chunkMetadata.getFileId(), chunkMetadata.getChunkNum())));
                    AddressPort addressPort = this.addressPortList.getMcAddressPort();
                    PutChunk backupMsg = new PutChunk(addressPort.getAddress(), addressPort.getPort(), chunkMetadata.getFileId(),
                        chunkMetadata.getChunkNum(), 1, fileHandler.getChunkFileData());
                    byte[] message = backupMsg.getBytes();
                    System.out.println("Zaaaaaaaaaah: " + new String(message));
                    AddressPort predecessorAddrPort = predecessor.getAddressPortList().getMdbAddressPort();
                    MessageSender.sendTCPMessage(predecessorAddrPort.getAddress(), predecessorAddrPort.getPort(), message);
                }
            }
        }
    }

    /**
     * Updates finger table periodically
     */
    public void fixFingers() {
        next++;
        if (next > Chord.m - 1) next = 0;

        //ChordNodeData node = findSuccessor(this.id + (int) Math.pow(2, next));
        ChordNodeData node = findSuccessor(calculateKey(this.id, next));

        if (next < fingerTable.size()) fingerTable.set(next, node);
        else fingerTable.add(node);

        //logFingerTable();
    }

    public void checkPredecessor() {
        //TODO: connect to peer
        //if predecessor has failed
        //predecessor = null
    }

    public void populateFingerTable(ChordNodeData chordNode) {
        for (int i = 1; i <= Chord.m; i++) {
            ChordNodeData chordNodeI = findSuccessor(getId(chordNode.getId(), i));
            //chordNode.addToFingerTable(chordNodeI.getId(), new ChordNode());
        }
    }

    public int getId(int nodeId, int indexFingerTable) {
        return nodeId + 2 ^ (indexFingerTable - 1);
    }

    public ChordNodeData findSuccessor(Integer id) {
        if (isInInterval(id, this.id, this.successor.getId() + 1)) {
            return this.successor;
        } else {
            ChordNodeData precedingNode = closestPrecedingNode(id);
            if (precedingNode == null) return this.data;

            // Sending Get Successor message
            String message = Messages.GET_SUCCESSOR + " " + id + "\r\n\r\n";
            AddressPort addressPort = precedingNode.getAddressPortList().getChordAddressPort();
            byte[] response = sendMessage(message.getBytes(), addressPort.getAddress(), addressPort.getPort());
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
    private byte[] sendMessage(byte[] message, String address, Integer port) {
        SslSender sender = new SslSender(address, port, message);
        sender.connect();
        sender.write(message);
        byte[] response = sender.read();
        sender.shutdown();
        return response;
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

    private boolean isInInterval(int element, int lowerBound, int upperBound) {
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
