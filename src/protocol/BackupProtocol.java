package protocol;

import filehandler.FileHandler;
import messages.protocol.Delete;
import messages.protocol.PutChunk;
import peer.Peer;
import peer.metadata.FileMetadata;
import utils.AddressPort;
import utils.ThreadHandler;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BackupProtocol extends Protocol {
    final int repDgr;
    final int repsLimit = 5;
    byte[] message;
    final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    int numOfChunks = 0;
    int timeWait = 1;
    int reps = 1;
    String fileId;

    public BackupProtocol(File file, Peer peer, int repDgr) {
        super(file, peer);
        this.repDgr = repDgr;
    }

    public BackupProtocol(String path, Peer peer, int repDgr) {
        super(path, peer);
        this.repDgr = repDgr;
    }


    @Override
    public void initialize()  {
        System.out.println("[BACKUP] Initializing Backup protocol of file " + file.getName() + " with size: " + file.length()/1000.0 + "Kb");
        FileHandler fileHandler = new FileHandler(file);
        ConcurrentHashMap<Integer, byte[]> chunks = fileHandler.getFileChunks();
        fileId = fileHandler.createFileId();
        numOfChunks = chunks.size();
        AddressPort mcaddr = peer.getArgs().getAddressPortList().getMcAddressPort();
        AddressPort mdbaddr = peer.getArgs().getAddressPortList().getMdbAddressPort();


        if (peer.getMetadata().hasFile(fileId)) {
            System.out.println("[BACKUP] File already backed up, aborting...");
            return;
        }

        // Updating a previously backed up file, delete previous one
        String previousFileId = peer.getMetadata().getFileIdFromPath(file.getPath());
        if (previousFileId != null) {
            Delete msg = new Delete(mcaddr.getAddress(), mcaddr.getPort(), previousFileId);
            ThreadHandler.sendTCPMessageMC(file.getName(), peer, msg.getBytes());
            System.out.println("[BACKUP] Received new version of file. Deleted previous one!");
        }

        FileMetadata fileMetadata = new FileMetadata(file.getPath(), fileId, repDgr, (int) file.length());
        peer.getMetadata().addHostingEntry(fileMetadata);
    
        // message initialization
        for (ConcurrentHashMap.Entry<Integer, byte[]> chunk : chunks.entrySet()) {
            PutChunk backupMsg = new PutChunk(mdbaddr.getAddress(), mdbaddr.getPort(), fileId,
                    chunk.getKey(), repDgr, chunk.getValue());
            message = backupMsg.getBytes();
        }

        execute();
    }

    private void execute() {
        if (reps <= repsLimit) {
            ThreadHandler.sendTCPMessageMDB(file.getName(), peer, message);
            
            //TODO isto e para comentar right? executor.schedule(this::verify, timeWait, TimeUnit.SECONDS);
            System.out.println("[BACKUP] Sent message, waiting " + timeWait + " seconds...");
        } else {
            System.out.println("[BACKUP] Reached reached limit of PUTCHUNK messages!");
        }
    }

    private void verify() {
        if (!peer.getMetadata().verifyRepDgr(fileId, repDgr, numOfChunks)) {
            System.out.println("[BACKUP] Did not get expected replication degree after " + timeWait + " seconds. Resending...");
            reps++;
            timeWait *= 2;
            execute();
        } else {
            System.out.println("[BACKUP] Got expected replication degree!");
        }
    }

    public void backupChunk(String fileId, int chunkNo) {
        FileHandler fileHandler = new FileHandler(file);

        //FileMetadata fileMetadata = new FileMetadata(file.getPath(), fileId, repDgr, (int) file.length());
        //peer.getMetadata().addHostingEntry(fileMetadata);


        AddressPort addressPort = peer.getArgs().getAddressPortList().getMdbAddressPort();
        PutChunk backupMsg = new PutChunk(addressPort.getAddress(), addressPort.getPort(), fileId,
                chunkNo, repDgr, fileHandler.getChunkFileData());
        message = backupMsg.getBytes();

        execute();
    }
}
