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

public class ChordPeer {
    private boolean isBoot;

    private final int id;
    private HashMap<Integer, Integer> fingerTable = new HashMap<>();

    private ChordPeer predecessor;
    private ChordPeer successor;

    private final String ipAddr;
    private final Integer port;

    private ScheduledThreadPoolExecutor executor;

    public ChordPeer(int id, String ipAddr, Integer port) {
        this.id = id;
        this.ipAddr = ipAddr;
        this.port = port;

        // Schedule stabilize
        this.executor =  new ScheduledThreadPoolExecutor(Constants.numThreads);
        executor.scheduleAtFixedRate(this::stabilize, Constants.executorDelay, Constants.executorDelay, TimeUnit.SECONDS);

    }

    public ChordPeer(Peer peer){
        this.ipAddr = peer.getArgs().getChordPeerIpAddr();
        this.port =  peer.getArgs().getChordPort();
        this.id = generateHash(ipAddr,port);
        System.out.println("Chord Peer was created id: " +  id);
    }

    public void create() {
        predecessor = null;
        successor = this;
    }
    //Endereço de chord que ja esta no ring
    public void join(String oldIpAddr,int oldChordPort) {
        //Mandar mensagem ao outro gajo a perguntar pela tabela deles i guess?
        predecessor = null;
        //TODO aqui e para passar o id ou o peer mm?
        successor = this.findSuccessor(this.getId());
        //Se boot
        //Cenas
        //Senao
        //Junta se ao boot
        //Manda se mensagem ao boot
        //
    }




    public void addSuccessor(ChordPeer chordPeer) {
    }

    public void removeFromFingerTable(ChordPeer chordPeer) {
    }

    public void addToFingerTable(int id, int index){
        this.fingerTable.put(index, id);
    }

    public void stabilize() {
        System.out.println("Hello!");

        ChordPeer x = successor.getPredecessor();
        if(x.getId() > id  && x.getId() < successor.getId()){
            successor = x;
        }
        successor.notifyPeer(this);
    }

    public void notifyPeer(ChordPeer peer) {
        if(predecessor==null || (peer.getId() < predecessor.getId() && peer.getId() < id)){
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

    public void populateFingerTable(ChordPeer chordPeer) {
        for (int i = 1; i <= Chord.m; i++) {
            ChordPeer chordPeerI = findSuccessor(getId(chordPeer.getId(),i));
            chordPeer.addToFingerTable(chordPeerI.getId(),i);
        }
    }

    public int getId(int nodeId, int indexFingerTable){
        return nodeId + 2^(indexFingerTable-1);
    }

    public ChordPeer findSuccessor(Integer id) {
        //if(n < id && id <= sucessor) return succesor;
        //else{
        //  ns = closest.preceding_node(id);
        //  return ns.findSuccessor(id)
        //}
        return new ChordPeer(1,"1",1);
    }
    public void closestPrecidingNode(int id){
        /*for (int i = m; i > 1; i--) {
            //if(finger[i] > n && finger[i] < id) return finger[i];
        }*/
        //return n;
    }

    public ChordPeer findPredecessor(Integer id) {
        return new ChordPeer(1,"1",1);
    }

    public int generateHash(String ipAddr,int port) {
        String id = ipAddr+port;
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
        byte[] digest = messageDigest.digest();
        String sha1 = String.format("%040x", new BigInteger(1, digest));
        if (digest == null) {
            System.out.println("Could not generate peer id with SHA-1 ");
            return -1;
        }
        int hashCode = sha1.hashCode();
        hashCode = hashCode < 0 ? - hashCode : hashCode;

        //Truncar ate ter m bits
        return (int) (hashCode % Math.pow(2, Chord.m)) ;
    }

    public int getId() {
        return id;
    }

    public ChordPeer getPredecessor() {
        return this.predecessor;
    }

    public boolean isBoot() {
        return isBoot;
    }

    public ChordPeer getSuccessor() {
        return successor;
    }

    public void setSuccessor(ChordPeer successor) {
        this.successor = successor;
    }
}
