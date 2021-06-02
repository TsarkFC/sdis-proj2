package channels;

import chord.ChordNode;
import messages.protocol.PutChunk;
import messages.protocol.Stored;
import peer.Peer;
import ssl.SSLReceiver;
import utils.AddressPortList;
import filehandler.FileHandler;
import messages.MessageSender;
import utils.Utils;

import java.util.Arrays;

public class BackupChannel extends Channel {

    public BackupChannel(AddressPortList addressPortList, Peer peer) {
        super(addressPortList, peer);
        super.currentAddr = addressPortList.getMdbAddressPort();

        SSLReceiver receiver = new SSLReceiver(currentAddr.getAddress(), currentAddr.getPort(), this);
        new Thread(receiver).start();
    }

    @Override
    public byte[] handle(byte[] message) {
        int bodyStartPos = getBodyStartPos(message);
        byte[] header = Arrays.copyOfRange(message, 0, bodyStartPos - 4);
        byte[] bodyCrlf = Arrays.copyOfRange(message, bodyStartPos, message.length);
        byte[] body = Utils.readUntilCRLF(bodyCrlf);

        String rcvd = new String(header, 0, header.length);
        System.out.println("[RECEIVED MESSAGE MDB] " + rcvd);
        PutChunk rcvdMsg = new PutChunk(rcvd, body);

        if (shouldSaveFile(rcvdMsg)) {
            System.out.println("[BACKUP] Should save file");
            saveChunk(rcvdMsg);
            sendStored(rcvdMsg);
            resendFile(rcvdMsg);
        } else {
            System.out.println("[BACKUP] Should not save file " + new String(header));
            if (shouldResend(rcvdMsg)) {
                System.out.println("[BACKUP] Resent message");
                MessageSender.sendTCPMessageMDBSuccessor(peer, rcvdMsg.getBytes());
                return null;
            }
            System.out.println("[BACKUP] Did not resend message!");
        }
        return null;
    }

    private boolean shouldSaveFile(PutChunk rcvdMsg) {
        boolean sameSenderPeer = rcvdMsg.samePeerAndSender(peer);
        boolean hasSpace = peer.getMetadata().hasSpace(rcvdMsg.getBody().length / 1000.0);
        boolean isOriginalFileSender = peer.getMetadata().hasFile(rcvdMsg.getFileId());
        return !sameSenderPeer && hasSpace && !isOriginalFileSender;
    }

    public void saveChunk(PutChunk rcvdMsg) {
        System.out.println("[BACKUP] Backing up file " + rcvdMsg.getFileId() + "-" + rcvdMsg.getChunkNo());
        FileHandler.saveChunk(rcvdMsg, peer.getFileSystem());
        saveStateMetadata(rcvdMsg);
    }

    private void saveStateMetadata(PutChunk rcvdMsg) {
        peer.getMetadata().updateStoredInfo(rcvdMsg.getFileId(), rcvdMsg.getChunkNo(), rcvdMsg.getReplicationDeg(),
                rcvdMsg.getBody().length / 1000.0);
    }

    //TODO: remove
    private void sendStored(PutChunk rcvdMsg) {
        Stored message = new Stored(rcvdMsg.getFileId(), rcvdMsg.getChunkNo());
        MessageSender.sendTCPMessage(rcvdMsg.getIpAddress(), rcvdMsg.getPort(), message.getBytes());
    }

    private boolean resendFile(PutChunk message) {
        if (message.getReplicationDeg() - 1 > 0) {
            PutChunk newPutChunk = new PutChunk(message.getIpAddress(), message.getPort(), message.getFileId(), message.getChunkNo(), message.getReplicationDeg() - 1, message.getBody());
            MessageSender.sendTCPMessageMDBSuccessor(peer, newPutChunk.getBytes());
            return true;
        } else {
            System.out.println("[BACKUP] Completed replication degree, chunk stored");
            return false;
        }
    }

    private boolean shouldResend(PutChunk message) {
        String chunkFileId = FileHandler.createChunkFileId(message.getFileId(), message.getChunkNo(), message.getReplicationDeg());
        int fileChordID = peer.getChordNode().generateHash(chunkFileId);
        ChordNode node = peer.getChordNode();
        return !node.isInInterval(fileChordID, node.getId(), node.getSuccessor().getId());
    }
}
