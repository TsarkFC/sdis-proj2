package chord;

import constants.Constants;
import java.util.HashMap;

public class Chord {
    private final double maxNumNodes;
    private final int port;
    public static final int m = Constants.m;
    private HashMap<Integer, ChordPeer> nodes = new HashMap<>();

    public Chord(int port) {
        System.out.println("Chord ring was created in port: " + port);
        this.maxNumNodes = Math.pow(2, m);
        this.port = port;
    }


    public void create() {

    }

    public void join(ChordPeer peer) {

    }

    public void chordNotify() {

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
        return new ChordPeer(1,"1",1);
    }

    public ChordPeer findPredecessor(Integer id) {
        return new ChordPeer(1,"1",1);
    }


}
