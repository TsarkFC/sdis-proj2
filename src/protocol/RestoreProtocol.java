package protocol;

import filehandler.FileHandler;
import messages.protocol.GetChunk;
import peer.Peer;
import peer.PeerArgs;
import peer.metadata.Metadata;
import utils.AddressPort;
import utils.AddressPortList;
import messages.MessageSender;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

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
            AddressPort addressPort = peerArgs.getAddressPortList().getMdrAddressPort();
            GetChunk getChunk = new GetChunk(addressPort.getAddress(), addressPort.getPort(), fileId, i);
            messages.add(getChunk.getBytes());
        }

        execute(messages,fileId);
    }

    private void execute(List<byte[]> messages,String fileId) {
        AddressPortList addrList = peer.getArgs().getAddressPortList();
        for (byte[] message : messages) {
            //ThreadHandler.sendTCPMessage(addrList.getMcAddressPort().getAddress(), addrList.getMcAddressPort().getPort(), message);
            File f = new File(path);
            System.out.println("Trying to restore file with name: " + f.getName());
            MessageSender.sendTCPMessageMC(fileId,peer,message);
        }
    }

    private void verify(List<byte[]> messages, String fileId) {
        if (peer.hasRestoreEntry(fileId)) {
            System.out.println("[RESTORE] Did not complete after " + timeWait + " seconds. Resending...");
            reps++;
            timeWait *= 2;
            execute(messages,fileId);
        }
    }
}
