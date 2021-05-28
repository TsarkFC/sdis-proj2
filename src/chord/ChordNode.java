package chord;

import constants.Constants;
import peer.Peer;
import ssl.SslSender;
import utils.AddressPortList;
import utils.SerializeChordNode;
import utils.Utils;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ChordNode implements Serializable {
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
    private List<ChordNode> fingerTable = new ArrayList<>();

    /**
     * Peer predecessor on the ring
     */
    private ChordNode predecessor;

    /**
     * Peer successor on the ring
     */
    private ChordNode successor;

    /**
     * Object containing ip addresses and ports of all the 4 channels
     */
    private final AddressPortList addressPortList;

    /**
     * Used in fixFingers, to know which entry to update
     */
    private int next = 0;

    public ChordNode(Peer peer) {
        this.addressPortList = peer.getArgs().getAddressPortList();
        this.id = generateHash(addressPortList.getChordAddressPort().getAddress(), addressPortList.getChordAddressPort().getPort());
        this.isBoot = peer.getArgs().isBoot();
        System.out.println("Chord Peer was created id: " + id);

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(Constants.numThreads);
        executor.scheduleAtFixedRate(this::stabilize, Constants.executorDelay, Constants.executorDelay, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::fixFingers, Constants.executorDelay, Constants.executorDelay, TimeUnit.MILLISECONDS);
        System.out.println("Executors ready!");
    }

    public void create() {
        predecessor = null;
        successor = this;
    }

    public void join(String chordAddress, int chordPort) {
        predecessor = null;

        String message = "JOIN " + this.id + "\r\n\r\n";

        //TODO: place in executor for better concurrency
        SslSender sender = new SslSender(Constants.sslProtocol, chordAddress, chordPort);
        sender.connect();
        sender.write(message.getBytes());
        byte[] successorInfoCRLF = sender.read();
        sender.shutdown();

        byte[] successorInfo = Utils.readUntilCRLF(successorInfoCRLF);
        successor = new SerializeChordNode().deserialize(successorInfo);
        System.out.println("Successor id: " + successor.id);
    }

    public void stabilize() {
        if (successor == null) return;

        ChordNode x = successor.predecessor;
        if (x != null && x.id > id && x.id < successor.id) {
            successor = x;
            System.out.println("[Stabilize] updated successor");
        }
        successor.notify(this);
    }

    public void notify(ChordNode peer) {
        if (predecessor == null || (peer.getId() < predecessor.getId() && peer.getId() < id)) {
            predecessor = peer;
            System.out.println("[Notify] updated predecessor");
        }
    }

    /**
     * Updates finger table periodically
     */
    public void fixFingers() {
        next++;
        if (next > Chord.m - 1) next = 0;

        ChordNode node = findSuccessor(id + (int) Math.pow(2, next));
        if (fingerTable.size() > next) fingerTable.set(next, node);
        else fingerTable.add(node);
    }

    public void checkPredecessor() {
        //TODO: connect to peer
        //if predecessor has failed
        //predecessor = null
    }

    public void populateFingerTable(ChordNode chordNode) {
        for (int i = 1; i <= Chord.m; i++) {
            ChordNode chordNodeI = findSuccessor(getId(chordNode.getId(), i));
            //chordNode.addToFingerTable(chordNodeI.getId(), new ChordNode());
        }
    }

    public int getId(int nodeId, int indexFingerTable) {
        return nodeId + 2 ^ (indexFingerTable - 1);
    }

    public ChordNode findSuccessor(Integer id) {
        if (this.id < id && id <= this.successor.id) {
            return this.successor;
        } else {
            ChordNode ns = closestPrecedingNode(id);
            if (ns == null) return this;
            return ns.findSuccessor(id);
        }
    }

    public ChordNode closestPrecedingNode(int id) {
        for (int i = Chord.m; i > 0; i--) {
            if (i >= fingerTable.size()) return null;
            ChordNode node = fingerTable.get(i);

            if (node == null) continue;
            if (node.id > this.id && node.id < id)
                return node;
        }

        return null;
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

    public ChordNode getPredecessor() {
        return this.predecessor;
    }

    public boolean isBoot() {
        return isBoot;
    }

    public ChordNode getSuccessor() {
        return successor;
    }

    public void setSuccessor(ChordNode successor) {
        this.successor = successor;
    }

    public AddressPortList getAddressPortList() {
        return addressPortList;
    }
}
