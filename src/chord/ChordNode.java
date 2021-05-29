package chord;

import constants.Constants;
import peer.Peer;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ChordNode {
    private boolean isBoot;

    private final int id;
    private HashMap<Integer, ChordNode> fingerTable = new HashMap<>();

    private ChordNode predecessor;
    private ChordNode successor;

    private final String address;
    private final Integer port;
    private String bootAddress;
    private Integer bootPort;

    private ScheduledThreadPoolExecutor executor;

    public ChordNode(Peer peer) {
        this.address = peer.getArgs().getAddress();
        this.port = peer.getArgs().getPort();
        this.bootAddress = peer.getArgs().getBootAddress();
        this.bootPort = peer.getArgs().getBootPort();
        this.id = generateHash(address, port);
        this.isBoot = peer.getArgs().isBoot();
        System.out.println("Chord Peer was created id: " + id);
    }

    public ChordNode(int id, String address, Integer port) {
        this.id = id;
        this.address = address;
        this.port = port;
        this.isBoot = true;

        // Schedule stabilize
        this.executor = new ScheduledThreadPoolExecutor(Constants.numThreads);
        executor.scheduleAtFixedRate(this::stabilize, Constants.executorDelay, Constants.executorDelay, TimeUnit.SECONDS);
    }

    public ChordNode(int id, String address, Integer port, String bootAddress, Integer bootPort) {
        this.id = id;
        this.address = address;
        this.port = port;
        this.bootAddress = bootAddress;
        this.bootPort = bootPort;
        this.isBoot = false;

        // Schedule stabilize
        this.executor = new ScheduledThreadPoolExecutor(Constants.numThreads);
        executor.scheduleAtFixedRate(this::stabilize, Constants.executorDelay, Constants.executorDelay, TimeUnit.SECONDS);
    }

    public void create() {
        predecessor = null;
        successor = this;
    }

    //Endereço de chord que ja esta no ring
    public void join(String bootAddress, int bootPort) {
        predecessor = null;
        // send message to peer ()
        // peer finds successor
        // receives successor
        successor = this.findSuccessor(this.getId());
    }


    public void addSuccessor(ChordNode chordNode) {
    }

    public void removeFromFingerTable(ChordNode chordNode) {
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
        //if(n < id && id <= sucessor) return succesor;
        //else{
        //  ns = closest.preceding_node(id);
        //  return ns.findSuccessor(id)
        //}
        return new ChordNode(1, "1", 1);
    }

    public void closestPrecedingNode(int id) {
        /*for (int i = m; i > 1; i--) {
            //if(finger[i] > n && finger[i] < id) return finger[i];
        }*/
        //return n;
    }

    public ChordNode findPredecessor(Integer id) {
        return new ChordNode(1, "1", 1);
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
}
