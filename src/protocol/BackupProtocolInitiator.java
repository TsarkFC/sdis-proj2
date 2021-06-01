package protocol;

import messages.protocol.MsgWithChunk;
import peer.Peer;
import peer.metadata.ChunkMetadata;
import filehandler.FileHandler;

import java.util.HashSet;
import java.util.Set;

public class BackupProtocolInitiator implements Runnable {
    MsgWithChunk msgWithChunk;
    private final ChunkMetadata chunkMetadata;
    private final Peer peer;
    /**
     * If during this delay, a peer receives a PUTCHUNK message for the same file chunk,
     * this map only has the fileId-chunkNo received before the reclaim initiated the protocol
     * If the chunk that is going to initiate backup is in the map, some peer already initiated the protocol
     */
    Set<String> receivedDuringReclaim = new HashSet<>();


    public BackupProtocolInitiator(MsgWithChunk msgWithChunk, ChunkMetadata chunkMetadata, Peer peer) {
        this.msgWithChunk = msgWithChunk;
        this.chunkMetadata = chunkMetadata;
        this.peer = peer;
    }

    public void run() {
        String path = FileHandler.getChunkPath(peer.getFileSystem(), msgWithChunk.getFileId(), msgWithChunk.getChunkNo());
        System.out.println("[BACKUP] Initiating backup protocol of path: " + path);
        //TODO ele agora ja nao precisa do if porque ele so envia o reclaim para um right?
        //E o rep degree e um pq ele so eliminou num file
        //if (!receivedDuringReclaim(msgWithChunk.getFileId(), msgWithChunk.getChunkNo())) {
        BackupProtocol backupProtocol = new BackupProtocol(path, peer, 1);
        backupProtocol.backupChunk(msgWithChunk.getFileId(), msgWithChunk.getChunkNo());
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