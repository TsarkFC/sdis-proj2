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
import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ChordNode implements Serializable {
    private boolean isBoot;

    private final int id;
    private HashMap<Integer, ChordNode> fingerTable = new HashMap<>();
    private ChordNode predecessor;
    private ChordNode successor;
    private final AddressPortList addressPortList;
    private ScheduledThreadPoolExecutor executor;

    public ChordNode(Peer peer) {
        this.addressPortList = peer.getArgs().getAddressPortList();
        this.id = generateHash(addressPortList.getChordAddressPort().getAddress(), addressPortList.getChordAddressPort().getPort());
        this.isBoot = peer.getArgs().isBoot();
        System.out.println("Chord Peer was created id: " + id);

        // Schedule stabilize
        //this.executor = new ScheduledThreadPoolExecutor(Constants.numThreads);
        //executor.scheduleAtFixedRate(this::stabilize, Constants.executorDelay, Constants.executorDelay, TimeUnit.SECONDS);
    }

    public void create() {
        predecessor = null;
        successor = this;
    }

    //Endereço de chord que ja esta no ring
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

    public void addToFingerTable(int id, ChordNode node) {
        this.fingerTable.put(id, node);
    }

    public void stabilize() {
        System.out.println("Hello!");

        ChordNode x = successor.getPredecessor();
        if (x.getId() > id && x.getId() < successor.getId()) {
            successor = x;
        }
        successor.notifyPeer(this);
    }

    public void notifyPeer(ChordNode peer) {
        if (predecessor == null || (peer.getId() < predecessor.getId() && peer.getId() < id)) {
            predecessor = peer;
        }
    }

    public void checkPredecessor() {
        //if predecessor has failed
        //predessore = null
    }

    /**
     * Equivalent to function fix_fingers() in paper
     */
    public void updateFingerTable() {

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
            System.out.println("LOOP: " + i);
            ChordNode node = fingerTable.get(i);
            System.out.println("NODE: " + node);

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
