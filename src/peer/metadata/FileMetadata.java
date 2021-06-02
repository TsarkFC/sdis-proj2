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

    public FileMetadata(String pathname, String id, int repDgr, int size, int numberChunks) {
        this.pathname = pathname;
        this.id = id;
        this.repDgr = repDgr;
        this.size = size;
        this.numberChunks = numberChunks;
    }

    public void print() {
        System.out.println("\n### File metadata");
        System.out.println("ID: " + id);
        System.out.println("Pathname: " + pathname);
        System.out.println("Rep Degree: " + repDgr);
        System.out.println("Size: " + size);
        System.out.println("###\n");
    }

    public String getString(){
        String tabs = "   ";
        StringBuilder state = new StringBuilder();
        state.append(tabs).append("* ID: ").append(id).append("\n");
        state.append(tabs).append("* Pathname: ").append(pathname).append("\n");
        state.append(tabs).append("* Rep Degree: ").append(repDgr).append("\n");
        state.append(tabs).append("* Size: ").append(size).append("\n");

        return state.toString();
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
            peersIds.add(new AddressPort(ipAddress, port));
        } else {
            peersIds = new ConcurrentSkipListSet<>();
            peersIds.add(new AddressPort(ipAddress, port));
            chunksData.put(chunkId, peersIds);
        }
    }


    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public int getNumberChunks() {
        return numberChunks;
    }

}
