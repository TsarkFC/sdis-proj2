package protocol;

import messages.handlers.DeleteHandler;
import peer.Peer;
import peer.metadata.Metadata;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DeleteProtocol extends Protocol {
    final int repsLimit = 3;
    int reps = 1;
    final int timeWait = 1;
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
        peer.getMetadata().getFileMetadata(fileId).setDeleted(true);
        peer.getMetadata().deleteFile(fileId);

        execute(fileId);
    }

    private void execute(String fileId) {
        if (reps <= repsLimit) {
            new DeleteHandler().sendDeleteMessage(peer, fileId);
            executor.schedule(() -> {
                reps++;
                execute(fileId);
            }, timeWait, TimeUnit.SECONDS);
            System.out.println("[DELETE] Sent message, waiting " + timeWait + " seconds...");
        }
    }
}
