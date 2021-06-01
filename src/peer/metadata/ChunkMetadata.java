package peer.metadata;

import java.io.Serializable;
import java.util.concurrent.ConcurrentSkipListSet;

public class ChunkMetadata implements Serializable {
    private final int sizeKb;
    private final String id;
    private final int repDgr;

    public ChunkMetadata(int sizeKb, String id, int repDgr) {
        this.sizeKb = sizeKb;
        this.id = id;
        this.repDgr = repDgr;
    }

    public ChunkMetadata() {
        sizeKb = 0;
        id = "";
        repDgr = 0;
    }

    public String getString() {
        return String.format("%d, %s, %d", sizeKb, id, repDgr);
    }

    public int getSizeKb() {
        return sizeKb;
    }

    public String getId() {
        return id;
    }

    public int getRepDgr() {
        return repDgr;
    }
}
