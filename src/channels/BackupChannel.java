package channels;

import messages.protocol.PutChunk;
import messages.protocol.Stored;
import peer.Peer;
import protocol.BackupProtocolInitiator;
import ssl.SslReceiver;
import utils.AddressPort;
import utils.AddressPortList;
import filehandler.FileHandler;
import messages.MessageSender;
import utils.Utils;

import java.util.Arrays;

public class BackupChannel extends Channel {

    public BackupChannel(AddressPortList addressPortList, Peer peer) {
        super(addressPortList, peer);
        super.currentAddr = addressPortList.getMdbAddressPort();

        SslReceiver receiver = new SslReceiver(currentAddr.getAddress(), currentAddr.getPort(), this);
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
            System.out.println("Should save file");
            saveChunk(rcvdMsg);
            sendStored(rcvdMsg);
            resendFile(rcvdMsg);
        } else {
            System.out.println("Should not save file " + new String(header));
            int repDgr = peer.getMetadata().getFileMetadata(rcvdMsg.getFileId()).getRepDgr();
            if (repDgr == rcvdMsg.getReplicationDeg()) {
                System.out.println("Resent message");
                MessageSender.sendTCPMessageMDBSuccessor(rcvdMsg.getFileId(), peer, rcvdMsg.getBytes());
                return null;
            }
            System.out.println("Did not resend message!");
        }
        return null;
    }

    private boolean shouldSaveFile(PutChunk rcvdMsg) {
        boolean sameSenderPeer = rcvdMsg.samePeerAndSender(peer);
        boolean hasSpace = peer.getMetadata().hasSpace(rcvdMsg.getBody().length / 1000.0);
        boolean isOriginalFileSender = peer.getMetadata().hasFile(rcvdMsg.getFileId());
        return !sameSenderPeer && hasSpace && !isOriginalFileSender;
    }

    private void saveStateMetadata(PutChunk rcvdMsg) {
        peer.getMetadata().updateStoredInfo(rcvdMsg.getFileId(), rcvdMsg.getChunkNo(), rcvdMsg.getReplicationDeg(),
                rcvdMsg.getBody().length / 1000.0, peer.getArgs().getPeerId());
    }

    public void saveChunk(PutChunk rcvdMsg) {
        System.out.println("[BACKUP] Backing up file " + rcvdMsg.getFileId() + "-" + rcvdMsg.getChunkNo());
        preventReclaim(rcvdMsg);
        FileHandler.saveChunk(rcvdMsg, peer.getFileSystem());
        saveStateMetadata(rcvdMsg);
    }

    private void sendStored(PutChunk rcvdMsg) {
        AddressPort addressPortChord = peer.getArgs().getAddressPortList().getChordAddressPort();

        Stored message = new Stored(addressPortChord.getAddress(), addressPortChord.getPort(), rcvdMsg.getFileId(), rcvdMsg.getChunkNo());
        MessageSender.sendTCPMessage(rcvdMsg.getIpAddress(), rcvdMsg.getPort(), message.getBytes());

        //peer.getMetadata().getStoredChunksMetadata().deleteChunksSize(rcvdMsg.getFileId(), rcvdMsg.getChunkNo());
        saveChunk(rcvdMsg);
    }

    private void preventReclaim(PutChunk rcvdMsg) {
        BackupProtocolInitiator backupProtocolInitiator = peer.getChannelCoordinator().getBackupInitiator();
        if (backupProtocolInitiator != null) {
            backupProtocolInitiator.setReceivedPutChunk(rcvdMsg.getFileId(), rcvdMsg.getChunkNo());
        }
    }

    private boolean resendFile(PutChunk message) {
        if (message.getReplicationDeg() - 1 > 0) {
            PutChunk newPutChunk = new PutChunk(message.getIpAddress(), message.getPort(), message.getFileId(), message.getChunkNo(), message.getReplicationDeg() - 1, message.getBody());
            MessageSender.sendTCPMessageMDBSuccessor(message.getFileId(), peer, newPutChunk.getBytes());
            return true;
        } else {
            System.out.println("Completed Replication degree");
            return false;
        }
    }
}
