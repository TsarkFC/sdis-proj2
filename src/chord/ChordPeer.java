package chord;

import constants.Constants;

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

    public ChordPeer(){
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
    }

    public void notifyPeer(ChordPeer peer) {

    }

    public void checkPredecessor() {

    }

    /**
     * Equivalent to function fix_fingers() in paper
     */
    public void updateFingerTable() {

    }

    public int generateHash(String name) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-1");
            System.out.println(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if (digest == null) {
            System.out.println("Could not generate peer id with SHA-1 ");
            return -1;
        }

        byte[] encodedhash = digest.digest(name.getBytes(StandardCharsets.UTF_8));

        //Truncar ate ter m bits
        return 0;
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
