package protocol;

import filehandler.FileHandler;
import messages.protocol.GetChunk;
import peer.Peer;
import peer.PeerArgs;
import peer.metadata.FileMetadata;
import peer.metadata.Metadata;
import utils.AddressPort;
import utils.AddressPortList;
import messages.MessageSender;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class RestoreProtocol extends Protocol {
    final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    public RestoreProtocol(String path, Peer peer) {
        super(path, peer);
    }

    @Override
    public void initialize() {
        System.out.println("[RESTORE] Initializing Restore protocol");
        Metadata metadata = peer.getMetadata();
        String fileId = metadata.getFileIdFromPath(path);
        PeerArgs peerArgs = peer.getArgs();
        peer.resetChunksReceived();

        if (!metadata.hasFile(fileId)) {
            System.out.println("[RESTORE] Peer has not hosted BACKUP to file");
            return;
        }

        peer.addRestoreEntry(fileId);
        FileMetadata fileMetadata = peer.getMetadata().getHostingFileMetadata(fileId);
        fileMetadata.print();

        System.out.println("[RESTORE] file has " + fileMetadata.getNumberChunks() + " number of chunks");
        for (int i = 0; i < fileMetadata.getNumberChunks(); i++) {
            String chunkFileId = FileHandler.createChunkFileId(fileId, i, fileMetadata.getRepDgr());
            AddressPort addressPort = peerArgs.getAddressPortList().getMdrAddressPort();
            GetChunk getChunk = new GetChunk(addressPort.getAddress(), addressPort.getPort(), fileId, i, fileMetadata.getRepDgr());
            System.out.println("[RESTORE] sent message " + getChunk.getMsgString());
            MessageSender.sendTCPMessageMC(chunkFileId, peer, getChunk.getBytes());
        }
    }
}
