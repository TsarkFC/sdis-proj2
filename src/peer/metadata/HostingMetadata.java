package peer.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class HostingMetadata extends Metadata {


    public ConcurrentHashMap<String, FileMetadata> getFileInfo() {
        return fileInfo;
    }

    //File id -> [rep Degree, numberChunks]
    ConcurrentHashMap<String, FileMetadata> fileInfo = new ConcurrentHashMap<>();

    public HostingMetadata(String path) {
        super(path);
    }

    public FileMetadata getFileMetadata(String fileId){
        return fileInfo.get(fileId);
    }

    public void addHostingEntry(FileMetadata fileMetadata) {

        fileInfo.put(fileMetadata.getId(),fileMetadata);
        writeMetadata();
    }

    public boolean hasFile(String fileId) {
        return fileInfo.size() > 0 && fileInfo.containsKey(fileId);
    }
    public void deleteFile(String fileId) {
        fileInfo.remove(fileId);
        storedChunksMetadata.deleteChunksFromFile(fileId);
        writeMetadata();
    }

}
