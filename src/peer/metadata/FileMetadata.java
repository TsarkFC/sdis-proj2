package peer.metadata;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class FileMetadata implements Serializable {
    private final String pathname;
    private final String id;
    private final int repDgr;
    private final int size;
    private boolean deleted = false;

    /**
     * Maps chunk no to peer Ids that store the chunk
     */
    private final ConcurrentHashMap<Integer, ConcurrentSkipListSet<Integer>> chunksData = new ConcurrentHashMap<>();

    public FileMetadata(String pathname, String id, int repDgr, int size) {
        this.pathname = pathname;
        this.id = id;
        this.repDgr = repDgr;
        this.size = size;
    }

    public String getPathname() {
        return pathname;
    }

    public String getId() {
        return id;
    }

    public int getRepDgr() {
        return repDgr;
    }

    public ConcurrentHashMap<Integer, ConcurrentSkipListSet<Integer>> getChunksData() {
        return chunksData;
    }

    public int getSize() {
        return size;
    }

    public void addChunk(Integer chunkId, Integer peerId) {
        ConcurrentSkipListSet<Integer> peersIds = chunksData.get(chunkId);
        if (peersIds != null) {
            peersIds.add(peerId);
        } else {
            peersIds = new ConcurrentSkipListSet<>();
            peersIds.add(peerId);
            chunksData.put(chunkId, peersIds);
        }
    }

    public void removeID(int peersId) {
        for (ConcurrentSkipListSet<Integer> peerIds : chunksData.values()) {
            if (peerIds != null) {
                peerIds.remove(peersId);
            }
        }
    }

    public boolean deletedAllChunksAllPeers() {
        for (ConcurrentSkipListSet<Integer> peerIds : chunksData.values()) {
            if (peerIds.size() != 0) return false;
        }
        return true;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
