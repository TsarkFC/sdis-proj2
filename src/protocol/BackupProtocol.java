package protocol;

import chord.ChordPeer;
import messages.Delete;
import messages.PutChunk;
import peer.Peer;
import peer.PeerArgs;
import peer.metadata.FileMetadata;
import filehandler.FileHandler;
import utils.ThreadHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BackupProtocol extends Protocol {
    final int repDgr;
    final int repsLimit = 5;
    List<byte[]> messages;
    final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    int numOfChunks = 0;
    int timeWait = 1;
    int reps = 1;
    String fileId;

    public BackupProtocol(File file, ChordPeer peer, int repDgr) {
        super(file, peer);
        this.repDgr = repDgr;
    }

    public BackupProtocol(String path, ChordPeer peer, int repDgr) {
        super(path, peer);
        this.repDgr = repDgr;
    }


    @Override
    public void initialize()  {
        System.out.println("[BACKUP] Initializing Backup protocol of file " + file.getName() + " with size: " + file.length()/1000.0 + "Kb");
        messages = new ArrayList<>();
        FileHandler fileHandler = new FileHandler(file);
        ConcurrentHashMap<Integer, byte[]> chunks = fileHandler.getFileChunks();
        fileId = fileHandler.createFileId();
        numOfChunks = chunks.size();

        if (peer.getMetadata().hasFile(fileId)) {
            System.out.println("[BACKUP] File already backed up, aborting...");
            return;
        }

        // Updating a previously backed up file, delete previous one
        String previousFileId = peer.getMetadata().getFileIdFromPath(file.getPath());
        if (previousFileId != null) {
            PeerArgs peerArgs = peer.getArgs();
            Delete msg = new Delete(peerArgs.getVersion(), peerArgs.getPeerId(), previousFileId);
            List<byte[]> msgs = new ArrayList<>();
            msgs.add(msg.getBytes());
            //TODO Send message by internet
            

            System.out.println("[BACKUP] Received new version of file. Deleted previous one!");
        }

        FileMetadata fileMetadata = new FileMetadata(file.getPath(), fileId, repDgr, (int) file.length());
        peer.getMetadata().addHostingEntry(fileMetadata);

        // message initialization
        for (ConcurrentHashMap.Entry<Integer, byte[]> chunk : chunks.entrySet()) {
            PutChunk backupMsg = new PutChunk(peer.getArgs().getVersion(), peer.getArgs().getPeerId(), fileId,
                    chunk.getKey(), repDgr, chunk.getValue());
            messages.add(backupMsg.getBytes());
        }
    }

    public void backupChunk(String fileId, int chunkNo) {
        messages = new ArrayList<>();
        this.fileId = fileId;
        FileHandler fileHandler = new FileHandler(file);

        //FileMetadata fileMetadata = new FileMetadata(file.getPath(), fileId, repDgr, (int) file.length());
        //peer.getMetadata().addHostingEntry(fileMetadata);

        PutChunk backupMsg = new PutChunk(peer.getArgs().getVersion(), peer.getArgs().getPeerId(), fileId,
                chunkNo, repDgr, fileHandler.getChunkFileData());
        messages.add(backupMsg.getBytes());
    }
}
