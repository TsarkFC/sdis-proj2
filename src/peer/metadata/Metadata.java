package peer.metadata;

import messages.protocol.Message;
import peer.Peer;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class Metadata implements Serializable {


    /**
     * Maps fileId to FileMetadata
     */
    //ConcurrentHashMap<String, FileMetadata> hostingFileInfo = new ConcurrentHashMap<>();
    HostingMetadata hostingMetadata;

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
        hostingMetadata = new HostingMetadata();
    }

    /**
     * Updating information on initiator peer data
     */
    public void addHostingEntry(FileMetadata fileMetadata) {
        hostingMetadata.addHostingFileEntry(fileMetadata);
        writeMetadata();
    }


    public boolean hasFile(String fileId) {
        return hostingMetadata.hasFile(fileId);
    }

    public String getFileIdFromPath(String pathName) {
        for (Map.Entry<String, FileMetadata> entry : hostingMetadata.getFileInfo().entrySet()) {
            if (entry.getValue().getPathname().equals(pathName)) return entry.getKey();
        }
        return null;
    }

    public void deleteFile(String fileId) {
        hostingMetadata.deleteFile(fileId);
        storedChunksMetadata.deleteChunksFromFile(fileId);
        writeMetadata();
    }


    /**
     * Updating information on stored chunks data
     */
    public void updateStoredInfo(String fileId, Integer chunkNo, int repDgr, double chunkSize) {
        storedChunksMetadata.updateChunkInfo(fileId, chunkNo, repDgr, chunkSize);
        writeMetadata();
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
        state.append(hostingMetadata.getString());


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

        if (maxSpace == -1) return true;
        return maxSpace > finalSpace;
    }

    public String getPath() {
        return path;
    }

    public FileMetadata getHostingFileMetadata(String fileId) {
        return hostingMetadata.getFileMetadata(fileId);
    }

    public StoredChunksMetadata getStoredChunksMetadata() {
        return storedChunksMetadata;
    }

    public HostingMetadata getHostingMetadata() {
        return hostingMetadata;
    }

    public int getFileSize(String idFile) {
        return hostingMetadata.getFileMetadata(idFile).getSize();
    }

}
