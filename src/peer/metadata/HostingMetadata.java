package peer.metadata;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class HostingMetadata implements Serializable  {

    //File id -> [rep Degree, numberChunks]
    ConcurrentHashMap<String, FileMetadata> fileInfo = new ConcurrentHashMap<>();


    public ConcurrentHashMap<String, FileMetadata> getFileInfo() {
        return fileInfo;
    }


    public FileMetadata getFileMetadata(String fileId){
        return fileInfo.get(fileId);
    }

    public void addHostingFileEntry(FileMetadata fileMetadata) {
        fileInfo.put(fileMetadata.getId(),fileMetadata);
    }

    public boolean hasFile(String fileId) {
        return fileInfo.size() > 0 && fileInfo.containsKey(fileId);
    }
    public void deleteFile(String fileId) {
        fileInfo.remove(fileId);
    }

}
