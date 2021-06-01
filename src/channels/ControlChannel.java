package channels;

import filehandler.FileHandler;
import messages.handlers.GetChunkHandler;
import messages.protocol.*;
import peer.Peer;
import peer.metadata.ChunkMetadata;
import peer.metadata.StoredChunksMetadata;
import protocol.BackupProtocolInitiator;
import ssl.SslReceiver;
import utils.AddressPortList;
import utils.Utils;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
            default -> System.out.println("\nERROR NOT PARSING THAT MESSAGE " + msgType);
        }
        return null;
    }

    public void handleBackup(String msgString) {
        System.out.println("[RECEIVED MESSAGE MC]: " + msgString.substring(0, msgString.length() - 4));
        Stored msg = new Stored(msgString);

        //peer.getMetadata().updateStoredInfo(msg.getFileId(),msg.getChunkNo(),msg.get);

        // TODO: check rep degree
        //peer.getMetadata().updateStoredInfo(msg.getFileId(), msg.getChunkNo(), msg.getSenderId(), peer);
    }

    public void handleDelete(String msgString) {
        Delete msg = new Delete(msgString,false);
        System.out.println("[RECEIVED MESSAGE MC]: " + msgString.substring(0, msgString.length() - 4));
        System.out.println("ZAS " + msg.getFileId());
        if (FileHandler.deleteFile(msg.getFileId(), peer.getFileSystem())) {
            peer.getMetadata().getHostingMetadata().deleteFile(msg.getFileId());
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
        System.out.println("[RECEIVED MESSAGE MC]: " + msgString.substring(0, msgString.length() - 4));

        //A peer that has a local copy of the chunk shall update its local count of this chunk
        //1- Check if chunk is stored
        StoredChunksMetadata storageMetadata = peer.getMetadata().getStoredChunksMetadata();
        int peerId = peer.getArgs().getPeerId();
        if (storageMetadata.chunkIsStored(removed.getFileId(), removed.getChunkNo())) {
            //2- Update local count of its chunk
            ChunkMetadata chunkMetadata = storageMetadata.getChunk(removed.getFileId(), removed.getChunkNo());
            //TODO Ele agora ja nao tem acesso ao sender id
            //Mas tambem nao faz sentido ter no chunkMetadata qual é o rep degree
            //Acho que mais vale simpesmente quando eliminar fazer backup noutro.
            //chunkMetadata.removePeer(removed.getSenderId());
            System.out.println("[RECLAIM]: Peer has Chunk" + chunkMetadata.getId() + " from " + removed.getFileId() + " stored");

            BackupProtocolInitiator backupProtocolInitiator = new BackupProtocolInitiator(removed, chunkMetadata, peer);
            peer.getChannelCoordinator().setBackupInitiator(backupProtocolInitiator);
            new ScheduledThreadPoolExecutor(1).schedule(backupProtocolInitiator, 0, TimeUnit.MILLISECONDS);
            //}
        } else {
            System.out.println("Tenho que o propagar ate ele encontrar o chunk right?");

            System.out.println("[RECLAIM]: Peer does not have Chunk stored");
        }
    }
}
