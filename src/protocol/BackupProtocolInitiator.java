package protocol;

import messages.protocol.Removed;
import peer.Peer;
import peer.metadata.ChunkMetadata;
import filehandler.FileHandler;

import java.util.HashSet;
import java.util.Set;

public class BackupProtocolInitiator implements Runnable {
    Removed removed;
    private final ChunkMetadata chunkMetadata;
    private final Peer peer;
    /**
     * If during this delay, a peer receives a PUTCHUNK message for the same file chunk,
     * this map only has the fileId-chunkNo received before the reclaim initiated the protocol
     * If the chunk that is going to initiate backup is in the map, some peer already initiated the protocol
     */
    Set<String> receivedDuringReclaim = new HashSet<>();


    public BackupProtocolInitiator(Removed removed, ChunkMetadata chunkMetadata, Peer peer) {
        this.removed = removed;
        this.chunkMetadata = chunkMetadata;
        this.peer = peer;
    }

    public void run() {
        String path = FileHandler.getChunkPath(peer.getFileSystem(), removed.getFileId(), removed.getChunkNo());
        System.out.println("[BACKUP] Initiating backup protocol of path: " + path);
        //TODO ele agora ja nao precisa do if porque ele so envia o reclaim para um right?
        //E o rep degree e um pq ele so eliminou num file
        //if (!receivedDuringReclaim(removed.getFileId(), removed.getChunkNo())) {
        //TODO Ao fazer isto, ele tambem vai fazer backup dos chunks com menos rep degree, e preciso por uma condçao que caso
        //Os gajos ja tenham o backup feito, entao nao propaguem o ficheiro
        BackupProtocol backupProtocol = new BackupProtocol(path, peer, removed.getReplicationDeg());
        backupProtocol.backupChunk(removed.getFileId(), removed.getChunkNo());
        //}
        peer.getChannelCoordinator().setBackupInitiator(null);
    }

    public void setReceivedPutChunk(String fileId, int chunkNo) {
        receivedDuringReclaim.add(fileId + "-" + chunkNo);
    }

    public boolean receivedDuringReclaim(String fileId, int chunkNo) {
        return receivedDuringReclaim.contains(fileId + "-" + chunkNo);
    }
}