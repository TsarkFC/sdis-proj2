package protocol;

import messages.handlers.DeleteHandler;
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


        new DeleteHandler().sendDeleteMessage(peer, fileId,fileMetadata);
    }

}
