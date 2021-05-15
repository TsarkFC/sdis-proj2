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





    public void chordNotify() {

    }




}
