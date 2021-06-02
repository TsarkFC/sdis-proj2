package protocol;

import filehandler.FileHandler;
import messages.protocol.Delete;
import messages.protocol.PutChunk;
import peer.Peer;
import peer.metadata.FileMetadata;
import utils.AddressPort;
import messages.MessageSender;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class BackupProtocol extends Protocol {
    final int repDgr;
    int numOfChunks = 0;
    String fileId;

    public BackupProtocol(File file, Peer peer, int repDgr) {
        super(file, peer);
        this.repDgr = repDgr;
    }

    @Override
    public void initialize() {
        System.out.println("[BACKUP] Initializing Backup protocol of file " + file.getName() + " with size: " + file.length() / 1000.0 + "Kb");
        FileHandler fileHandler = new FileHandler(file);
        ConcurrentHashMap<Integer, byte[]> chunks = fileHandler.getFileChunks();
        fileId = fileHandler.createFileId();
        numOfChunks = chunks.size();
        AddressPort mcAddr = peer.getArgs().getAddressPortList().getMcAddressPort();

        if (peer.getMetadata().getHostingMetadata().hasFile(fileId)) {
            System.out.println("[BACKUP] File already backed up, aborting...");
            return;
        }

        // Updating a previously backed up file, delete previous one
        String previousFileId = peer.getMetadata().getFileIdFromPath(file.getPath());
        if (previousFileId != null) {
            int i = 0;
            for (ConcurrentHashMap.Entry<Integer, byte[]> chunk : chunks.entrySet()) {
                String chunkFileId = FileHandler.createChunkFileId(fileId, i++, repDgr);
                Delete msg = new Delete(previousFileId, chunkFileId);
                MessageSender.sendTCPMessageMC(chunkFileId, peer, msg.getBytes());
            }

            System.out.println("[BACKUP] Received new version of file. Deleted previous one!");
        }

        FileMetadata fileMetadata = new FileMetadata(file.getPath(), fileId, repDgr, (int) file.length(), numOfChunks);
        peer.getMetadata().addHostingEntry(fileMetadata);

        // message initialization
        int i = 0;
        for (ConcurrentHashMap.Entry<Integer, byte[]> chunk : chunks.entrySet()) {
            PutChunk backupMsg = new PutChunk(mcAddr.getAddress(), mcAddr.getPort(), fileId,
                    chunk.getKey(), repDgr, 0, chunk.getValue());
            byte[] message = backupMsg.getBytes();
            String chunkFileId = FileHandler.createChunkFileId(fileId, i++, repDgr);
            MessageSender.sendTCPMessageMDB(chunkFileId, peer, message);
        }
    }
}
