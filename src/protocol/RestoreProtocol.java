package protocol;

import filehandler.FileHandler;
import messages.protocol.GetChunk;
import peer.Peer;
import peer.PeerArgs;
import peer.metadata.Metadata;
import utils.AddressPortList;
import utils.ThreadHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RestoreProtocol extends Protocol {
    final int repsLimit = 5;
    int timeWait = 1;
    int reps = 1;
    final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    public RestoreProtocol(String path, Peer peer) {
        super(path, peer);
    }

    @Override
    public void initialize() {
        System.out.println("[RESTORE] Initializing Restore protocol");
        List<byte[]> messages = new ArrayList<>();
        Metadata metadata = peer.getMetadata();
        String fileId = metadata.getFileIdFromPath(path);
        PeerArgs peerArgs = peer.getArgs();
        peer.resetChunksReceived();

        if (!metadata.hasFile(fileId)) {
            System.out.println("[RESTORE] Peer has not hosted BACKUP to file");
            return;
        }

        peer.addRestoreEntry(fileId);
        int chunksNo = FileHandler.getNumberOfChunks(metadata.getFileSize(fileId));

        for (int i = 0; i < chunksNo; i++) {
            GetChunk getChunk = new GetChunk(peerArgs.getVersion(), peerArgs.getPeerId(), fileId, i);
            messages.add(getChunk.getBytes());
        }

        execute(messages, fileId);
    }

    private void execute(List<byte[]> messages, String fileId) {
        if (reps <= repsLimit) {
            AddressPortList addrList = peer.getArgs().getAddressPortList();
            ThreadHandler.sendTCPMessage(addrList.getMcAddressPort().getAddress(), addrList.getMcAddressPort().getPort(), messages);
            executor.schedule(() -> verify(messages, fileId), timeWait, TimeUnit.SECONDS);
            System.out.println("[RESTORE] Sent message, waiting " + timeWait + " seconds...");
        } else {
            System.out.println("[RESTORE] Reached resending limit of PUTCHUNK messages!");
        }
    }

    private void verify(List<byte[]> messages, String fileId) {
        if (peer.hasRestoreEntry(fileId)) {
            System.out.println("[RESTORE] Did not complete after " + timeWait + " seconds. Resending...");
            reps++;
            timeWait *= 2;
            execute(messages, fileId);
        }
    }
}
