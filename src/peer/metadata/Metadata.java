package peer.metadata;

import peer.Peer;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class Metadata implements Serializable {

    /**
     * Maps fileId to FileMetadata
     */
    ConcurrentHashMap<String, FileMetadata> hostingFileInfo = new ConcurrentHashMap<>();

    /**
     * Contains information about stored chunks
     */
    StoredChunksMetadata storedChunksMetadata;

    /**
     * Path where metadata will be saved
     */
    final String path;

    /**
     * Max space the peer can store
     */
    double maxSpace = -1;

    public Metadata(String path) {
        this.path = path;
        storedChunksMetadata = new StoredChunksMetadata();
    }

    /**
     * Updating information on initiator peer data
     */
    public void addHostingEntry(FileMetadata fileMetadata) {
        hostingFileInfo.put(fileMetadata.getId(), fileMetadata);
        writeMetadata();
    }

    public List<FileMetadata> getAlmostDeletedFiles() {
        List<FileMetadata> almostDeletedFiles = new ArrayList<>();
        for (FileMetadata fileMetadata : hostingFileInfo.values()) {
            if (fileMetadata.isDeleted()) almostDeletedFiles.add(fileMetadata);
        }
        return almostDeletedFiles;
    }

    public void updateHostingInfo(FileMetadata hostingMetadata, Integer chunkNo, Integer peerId) {
        hostingMetadata.addChunk(chunkNo, peerId);
        writeMetadata();
    }

    public boolean hasFile(String fileId) {
        return hostingFileInfo.size() > 0 && hostingFileInfo.containsKey(fileId);
    }

    public String getFileIdFromPath(String pathName) {
        for (Map.Entry<String, FileMetadata> entry : hostingFileInfo.entrySet()) {
            if (entry.getValue().getPathname().equals(pathName)) return entry.getKey();
        }
        return null;
    }

    public void deleteFile(String fileId) {
        hostingFileInfo.remove(fileId);
        storedChunksMetadata.deleteChunksFromFile(fileId);
        writeMetadata();
    }

    public void deleteFileHosting(String fileID, Peer peer) {
        FileMetadata fileMetadata = hostingFileInfo.get(fileID);
        if (fileMetadata == null) return;
        if (!peer.isVanillaVersion() && fileMetadata.deletedAllChunksAllPeers()) {
            hostingFileInfo.remove(fileID);
            writeMetadata();
        }
    }

    /**
     * Updating information on stored chunks data
     */
    public void updateStoredInfo(String fileId, Integer chunkNo, Integer peerId, Peer peer) {
        FileMetadata hostingMetadata = hostingFileInfo.get(fileId);
        if (hostingMetadata != null) {
            updateHostingInfo(hostingMetadata, chunkNo, peerId);
        } else {

            storedChunksMetadata.updateChunkInfo(fileId, chunkNo, peerId, peer);
        }
        writeMetadata();
    }

    public void updateStoredInfo(String fileId, Integer chunkNo, Integer repDgr, Double chunkSize, Integer peerId) {
        int chunkSizeKb = (int) Math.round(chunkSize);
        storedChunksMetadata.updateChunkInfo(fileId, chunkNo, repDgr, chunkSizeKb, peerId);
        writeMetadata();
    }

    public boolean verifyRepDgr(String fileId, Integer repDgr, Integer numOfChunks) {

        //TODO Verificar aquele erro da apresentaçao
        //storedChunksMetadata.getFileChunkIds();
        ConcurrentHashMap<Integer, ConcurrentSkipListSet<Integer>> chunkData = hostingFileInfo.get(fileId).getChunksData();
        int chunksCount = 0;
        for (Map.Entry<Integer, ConcurrentSkipListSet<Integer>> entry : chunkData.entrySet()) {
            chunksCount++;
            if (entry.getValue().size() < repDgr) return false;
        }
        return chunksCount == numOfChunks;
    }

    public void writeMetadata() {
        ObjectOutputStream os;
        try {
            os = new ObjectOutputStream(new FileOutputStream(path));
            os.writeObject(this);
            os.close();
        } catch (IOException e) {
            System.out.println("Error writing metadata");
            e.printStackTrace();
        }
    }

    public Metadata readMetadata() {
        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(path));
            Metadata metadata = (Metadata) is.readObject();
            is.close();
            return metadata;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[METADATA] No data to read from peer");
            System.out.println("[METADATA] Creating new one...");
            return new Metadata(path);
        }
    }

    public String returnState() {
        StringBuilder state = new StringBuilder();
        state.append("\n********************************************************************************\n");
        state.append("******************************** State Metadata ********************************\n");
        // hosting data
        state.append("* Hosting:\n");
        for (String fileId : hostingFileInfo.keySet()) {
            state.append("   * File ID: ").append(fileId).append("\n");

            FileMetadata fileMetadata = hostingFileInfo.get(fileId);
            state.append(String.format("\t* Pathname: %s\n\t* Desired Replication Degree: %d\n",
                    fileMetadata.getPathname(), fileMetadata.getRepDgr()));
            state.append("\t* Hosting Chunks:\n");
            for (Map.Entry<Integer, ConcurrentSkipListSet<Integer>> entry : fileMetadata.getChunksData().entrySet()) {
                state.append("\t     [").append(entry.getKey()).append("]");
                state.append(" Perceived replication degree = ").append(entry.getValue().size()).append("\n");
            }
            state.append("\n");
        }

        // stored chunks data
        state.append("* Stored:\n");
        state.append(storedChunksMetadata.returnData());

        // storage capacity
        state.append("* Maximum storage capacity: ");
        state.append(maxSpace);

        state.append("\n* Current occupied space: ");
        state.append(storedChunksMetadata.getStoredSize()).append(" kb");

        state.append("\n********************************************************************************\n");
        state.append("********************************************************************************\n");


        return state.toString();
    }

    public void setMaxSpace(double maxSpace) {
        this.maxSpace = maxSpace;
        writeMetadata();
    }

    public boolean hasSpace(double newFileSizeKb) {
        int storedSize = storedChunksMetadata.getStoredSize();
        double finalSpace = storedSize + newFileSizeKb;

        System.out.println("STORED SIZE: " + storedSize);
        System.out.println("WOTH MEW FILE: " + finalSpace);
        if (maxSpace == -1) return true;
        return maxSpace > finalSpace;
    }

    public String getPath() {
        return path;
    }

    public FileMetadata getFileMetadata(String fileId) {
        return hostingFileInfo.get(fileId);
    }

    public StoredChunksMetadata getStoredChunksMetadata() {
        return storedChunksMetadata;
    }

    public int getFileSize(String fileId) {
        return hostingFileInfo.get(fileId).getSize();
    }

}
