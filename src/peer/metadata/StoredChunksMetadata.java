package peer.metadata;

import peer.Peer;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class StoredChunksMetadata implements Serializable {

    /**
     * Information about chunks saved by the peer.
     * String key identifies the chunk (<fileId>-<chunkNo>)
     * ChunkMetadata contains all chunk necessary information
     */
    final ConcurrentHashMap<String, ChunkMetadata> chunksInfo = new ConcurrentHashMap<>();

    /**
     * Has information if the peer already received putchunk of that chunk
     * if it has it should not create new chunkMetadata in stored messages
     * String key identifies the chunk (<fileId>-<chunkNo>)
     */
    final ConcurrentSkipListSet<String> alreadySavedChunk = new ConcurrentSkipListSet<>();

    public ConcurrentHashMap<String, ChunkMetadata> getChunksInfo() {
        return chunksInfo;
    }

    public String getChunkId(String fileId, Integer chunkNo) {
        return fileId + "-" + chunkNo;
    }

    public String[] getFileChunkIds(String chunkId) {
        return chunkId.split("-");
    }


    /**
     * Updating when received BACKUP messages and before sending STORED messages
     */
    public void updateChunkInfo(String fileId, Integer chunkNo, Integer repDgr, double chunkSize) {
        String chunkId = getChunkId(fileId, chunkNo);
        chunksInfo.put(chunkId, new ChunkMetadata((int) chunkSize, chunkId, repDgr));
    }

    public void deleteChunk(String fileId, Integer chunkNo) {
        String chunkId = fileId + "-" + chunkNo;
        if (!chunksInfo.containsKey(chunkId)) {
            System.out.println("[DELETE] Cannot delete Chunk from Metadata");
        } else {
            chunksInfo.remove(chunkId);
        }
    }

    public void deleteChunksFromFile(String fileId) {
        Iterator<String> it = chunksInfo.keySet().iterator();
        while (it.hasNext()) {
            String chunkId = it.next();
            String[] fileChunk = chunkId.split("-");
            if (fileChunk[0].equals(fileId)) {
                deleteReceivedChunk(fileId, Integer.parseInt(fileChunk[1]));
                it.remove();
            }
        }
    }

    public List<ChunkMetadata> getAllChunksFile(String fileId) {
        List<ChunkMetadata> fileChunks = new ArrayList<>();
        Iterator<String> it = chunksInfo.keySet().iterator();
        while (it.hasNext()) {
            String chunkId = it.next();
            String[] fileChunk = chunkId.split("-");
            if (fileChunk[0].equals(fileId)) {
                ChunkMetadata cm = chunksInfo.get(chunkId);
                fileChunks.add(cm);
                deleteReceivedChunk(fileId, Integer.parseInt(fileChunk[1]));
                it.remove();
            }
        }
        return fileChunks;
    }


    public ChunkMetadata getChunk(String fileId, Integer chunkNo) {
        String chunkId = fileId + "-" + chunkNo;
        return chunksInfo.getOrDefault(chunkId, null);
    }

    public String returnData() {
        String tabs = "   ";
        StringBuilder state = new StringBuilder();
        int chunkNum = 0;
        for (Map.Entry<String, ChunkMetadata> entry : chunksInfo.entrySet()) {
            state.append(tabs).append("Chunk  ").append(chunkNum).append("\n");
            chunkNum++;
            ChunkMetadata chunkMetadata = entry.getValue();
            String[] fileChunkIds = getFileChunkIds(entry.getKey());
            state.append(tabs).append("  * File ID: ").append(fileChunkIds[0]).append("\n");
            state.append(tabs).append("  * Chunk Id: ").append(fileChunkIds[1]).append("\n");
            state.append(String.format("%s  * Size (kb): %d\n%s  * Replication Degree: %d\n%s\n", tabs,
                    chunkMetadata.getSizeKb(), tabs, chunkMetadata.getRepDgr(), tabs));
        }
        return state.toString();
    }

    public int getStoredSize() {
        int size = 0;
        for (ChunkMetadata chunkMetadata : chunksInfo.values()) {
            size += chunkMetadata.getSizeKb();
        }
        return size;
    }

    public void deleteReceivedChunk(String fileId, int chunkNo) {
        String chunkId = getChunkId(fileId, chunkNo);
        alreadySavedChunk.remove(chunkId);
    }

}
