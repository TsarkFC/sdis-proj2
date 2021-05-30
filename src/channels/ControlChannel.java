package channels;

import filehandler.FileHandler;
import messages.handlers.DeleteHandler;
import messages.handlers.GetChunkHandler;
import messages.protocol.*;
import peer.Peer;
import peer.metadata.FileMetadata;
import ssl.SslReceiver;
import utils.AddressPortList;
import utils.Utils;

import java.util.List;

public class ControlChannel extends Channel {

    public ControlChannel(AddressPortList addressPortList, Peer peer) {
        super(addressPortList, peer);
        super.currentAddr = addressPortList.getMcAddressPort();

        SslReceiver receiver = new SslReceiver(currentAddr.getAddress(), currentAddr.getPort(), this);
        new Thread(receiver).start();
    }

    @Override
    public byte[] handle(byte[] message) {
        return parseMsg(new String(message));
    }

    public byte[] parseMsg(String msgString) {
        String msgType = Message.getTypeStatic(msgString);
        switch (msgType) {
            case "STORED" -> handleBackup(msgString);
            case "DELETE" -> handleDelete(msgString);
            case "GETCHUNK" -> handleRestore(msgString);
            case "REMOVED" -> handleReclaim(msgString);
            case "DELETED" -> handleDeleted(msgString);
            case "STARTING" -> handleStart(msgString);
            default -> System.out.println("\nERROR NOT PARSING THAT MESSAGE " + msgType);
        }
        return Utils.discard();
    }

    public void handleBackup(String msgString) {
        System.out.println("[RECEIVED MESSAGE MC]: " + msgString.substring(0, msgString.length() - 4));
        Stored msg = new Stored(msgString);

        // TODO: check rep degree
        //peer.getMetadata().updateStoredInfo(msg.getFileId(), msg.getChunkNo(), msg.getSenderId(), peer);
    }

    public void handleDelete(String msgString) {
        Delete msg = new Delete(msgString);
        if (!msg.samePeerAndSender(peer)) {
            System.out.println("[RECEIVED MESSAGE MC]: " + msgString.substring(0, msgString.length() - 4));
            if (FileHandler.deleteFile(msg.getFileId(), peer.getFileSystem())) {
                peer.getMetadata().deleteFile(msg.getFileId());
                new DeleteHandler().sendDeletedMessage(peer, msg);
            }
        }
    }


    public void handleDeleted(String msgString) {
        Deleted msg = new Deleted(msgString);
        if (!msg.samePeerAndSender(peer) && !peer.isVanillaVersion()) {
            System.out.println("[RECEIVED MESSAGE MC]: " + msgString.substring(0, msgString.length() - 4));
            FileMetadata fileMetadata = peer.getMetadata().getFileMetadata(msg.getFileId());
            if (fileMetadata == null) return;
            //fileMetadata.removeID(msg.getSenderId());
            peer.getMetadata().writeMetadata();
            if (fileMetadata.deletedAllChunksAllPeers()) {
                System.out.println("[DELETE] Successfully removed all chunks from all peers of file " + msg.getFileId());
                peer.getMetadata().deleteFileHosting(msg.getFileId(), peer);
            }
        }
    }

    public void handleStart(String msgString) {
        Starting msg = new Starting(msgString);
        if (!msg.samePeerAndSender(peer) && !peer.isVanillaVersion()) {
            System.out.println("[RECEIVED MESSAGE MC]: " + msgString.substring(0, msgString.length() - 4));
            List<FileMetadata> almostDeletedFiles = peer.getMetadata().getAlmostDeletedFiles();
            for (FileMetadata almostDeletedFile : almostDeletedFiles) {
                System.out.println("[DELETE] Sending delete message of file " + almostDeletedFile.getId());
                new DeleteHandler().sendDeleteMessage(peer, almostDeletedFile.getId());
            }
        }
    }

    public void handleRestore(String msgString) {
        System.out.println("[RECEIVED MESSAGE MC]: " + msgString.substring(0, msgString.length() - 4));
        GetChunk msg = new GetChunk(msgString);
        peer.resetChunksReceived();
        new GetChunkHandler().handleGetChunkMsg(msg, peer);
    }

    public void handleReclaim(String msgString) {
        Removed removed = new Removed(msgString);
        if (removed.samePeerAndSender(peer)) return;
        System.out.println("[RECEIVED MESSAGE MC]: " + msgString.substring(0, msgString.length() - 4));

        //A peer that has a local copy of the chunk shall update its local count of this chunk
        //1- Check if chunk is stored
        /*StoredChunksMetadata storageMetadata = peer.getMetadata().getStoredChunksMetadata();
        int peerId = peer.getArgs().getPeerId();
        if (storageMetadata.chunkIsStored(removed.getFileId(), removed.getChunkNo()) && !removed.samePeerAndSender(peerId)) {
            //2- Update local count of its chunk
            ChunkMetadata chunkMetadata = storageMetadata.getChunk(removed.getFileId(), removed.getChunkNo());
            chunkMetadata.removePeer(removed.getSenderId());
            System.out.println("[RECLAIM]: Peer has Chunk" + chunkMetadata.getId() + " from " + removed.getFileId() + " stored");
            System.out.println("[RECLAIM]: Updated perceived degree of chunk to " + chunkMetadata.getPerceivedRepDgr());


            //If this count drops below the desired replication degree of that chunk, it shall initiate
            // the chunk backup subProtocol between 0 and 400 ms
            if (chunkMetadata.getPerceivedRepDgr() < chunkMetadata.getRepDgr()) {
                System.out.println("[RECLAIM]: Perceived Replication degree dropped below Replication Degree");
                BackupProtocolInitiator backupProtocolInitiator = new BackupProtocolInitiator(removed, chunkMetadata, peer);
                peer.getChannelCoordinator().setBackupInitiator(backupProtocolInitiator);
                new ScheduledThreadPoolExecutor(1).schedule(backupProtocolInitiator,
                        Utils.generateRandomDelay("[BACKUP] Starting backup of " + chunkMetadata.getId() + " after "), TimeUnit.MILLISECONDS);
            }
        } else System.out.println("[RECLAIM]: Peer does not have Chunk stored");*/
    }
}
