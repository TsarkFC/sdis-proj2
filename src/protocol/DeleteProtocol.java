package protocol;

import filehandler.FileHandler;
import messages.MessageSender;
import messages.protocol.Delete;
import peer.Peer;
import peer.metadata.FileMetadata;
import peer.metadata.Metadata;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class DeleteProtocol extends Protocol {
    final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    public DeleteProtocol(String path, Peer peer) {
        super(path, peer);
    }

    @Override
    public void initialize() {
        System.out.println("[DELETE] Initializing Delete protocol");
        Metadata metadata = peer.getMetadata();
        String fileId = metadata.getFileIdFromPath(path);

        if (fileId == null) {
            System.out.println("[ERROR] Peer has not hosted BACKUP to file");
            return;
        }

        FileMetadata fileMetadata = peer.getMetadata().getHostingFileMetadata(fileId);
        fileMetadata.print();

        peer.getMetadata().getHostingFileMetadata(fileId).setDeleted(true);
        peer.getMetadata().deleteFile(fileId);

        sendDeleteMessage(peer, fileId, fileMetadata);
    }

    public void sendDeleteMessage(Peer peer, String fileId, FileMetadata fileMetadata) {
        Delete msg = new Delete(fileId, true);
        for (int i = 0; i < fileMetadata.getNumberChunks(); i++) {
            for (int repDgr = 1; repDgr <= fileMetadata.getRepDgr(); repDgr++) {
                String chunkFileId = FileHandler.createChunkFileId(fileId, i, repDgr);
                MessageSender.sendTCPMessageMC(chunkFileId, peer, msg.getBytes());
            }
        }
    }
}
