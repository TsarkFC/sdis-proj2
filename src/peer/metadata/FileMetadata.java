package peer.metadata;

import utils.AddressPort;

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
    private int numberChunks;

    /**
     * Maps chunk no to peer Ids that store the chunk
     */
    private final ConcurrentHashMap<Integer, ConcurrentSkipListSet<AddressPort>> chunksData = new ConcurrentHashMap<>();

    public FileMetadata(String pathname, String id, int repDgr, int size,int numberChunks) {
        this.pathname = pathname;
        this.id = id;
        this.repDgr = repDgr;
        this.size = size;
        this.numberChunks = numberChunks;
    }

    public void print(){
        System.out.println("ID: " + id);
        System.out.println("Pathname: " + pathname);
        System.out.println("Rep Degree: " + repDgr);
        System.out.println("Size: " + size);
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

    public ConcurrentHashMap<Integer, ConcurrentSkipListSet<AddressPort>> getChunksData() {
        return chunksData;
    }

    public int getSize() {
        return size;
    }

    public void addChunk(Integer chunkId, String ipAddress, int port) {
        ConcurrentSkipListSet<AddressPort> peersIds = chunksData.get(chunkId);
        if (peersIds != null) {
            peersIds.add(new AddressPort(ipAddress,port));
        } else {
            peersIds = new ConcurrentSkipListSet<>();
            peersIds.add(new AddressPort(ipAddress,port));
            chunksData.put(chunkId, peersIds);
        }
    }
    //TODO mudar caso seja usado
    public void removeID(int peersId) {
        //Ele tinha chunk number e todos os peers que mandavam
        //Para cada chunk obtem se uma lista de peers
        for (ConcurrentSkipListSet<AddressPort> peerIds : chunksData.values()) {
            if (peerIds != null) {
                peerIds.remove(peersId);
            }
        }
    }

    public boolean deletedAllChunksAllPeers() {
        for (ConcurrentSkipListSet<AddressPort> peerIds : chunksData.values()) {
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

    public int getNumberChunks() {
        return numberChunks;
    }

    public void setNumberChunks(int numberChunks) {
        this.numberChunks = numberChunks;
    }
}
