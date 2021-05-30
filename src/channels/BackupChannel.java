package channels;

import messages.protocol.PutChunk;
import messages.protocol.Stored;
import peer.Peer;
import protocol.BackupProtocolInitiator;
import ssl.SslReceiver;
import utils.AddressPort;
import utils.AddressPortList;
import filehandler.FileHandler;
import utils.ThreadHandler;
import utils.Utils;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BackupChannel extends Channel {

    public BackupChannel(AddressPortList addressPortList, Peer peer) {
        super(addressPortList, peer);
        super.currentAddr = addressPortList.getMdbAddressPort();

        SslReceiver receiver = new SslReceiver(currentAddr.getAddress(), currentAddr.getPort(), this);
        new Thread(receiver).start();
    }

    @Override
    public byte[] handle(byte[] message) {
        System.out.println("DATA");
        System.out.println(new String(message));
        System.out.println("######");
        int bodyStartPos = getBodyStartPos(message);
        System.out.println("###### BODY START POSITION: " + bodyStartPos);
        byte[] header = Arrays.copyOfRange(message, 0, bodyStartPos - 4);
        byte[] body = Arrays.copyOfRange(message, bodyStartPos, message.length);

        String rcvd = new String(header, 0, header.length);
        System.out.println("[RECEIVED MESSAGE MDB] " + rcvd);
        PutChunk rcvdMsg = new PutChunk(rcvd, body);

        if (shouldSaveFile(rcvdMsg)) {
            System.out.println("Should save file");
            String delayMsg;

            saveChunk(rcvdMsg);
            delayMsg = "[BACKUP] Sending stored messages after: ";

            new ScheduledThreadPoolExecutor(1).schedule(() -> sendStored(rcvdMsg),
                    Utils.generateRandomDelay(delayMsg), TimeUnit.MILLISECONDS);
        } else {
            System.out.println("Should not save file");
        }
        return Utils.discard();
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

    public void sendStored(PutChunk rcvdMsg) {

        AddressPort addressPortChord = peer.getArgs().getAddressPortList().getChordAddressPort();

        Stored message = new Stored(addressPortChord.getAddress(), addressPortChord.getPort(), rcvdMsg.getFileId(), rcvdMsg.getChunkNo());
        ThreadHandler.sendTCPMessage(rcvdMsg.getIpAddress(), rcvdMsg.getPort(), message.getBytes());

        //peer.getMetadata().getStoredChunksMetadata().deleteChunksSize0(rcvdMsg.getFileId(), rcvdMsg.getChunkNo());
        saveChunk(rcvdMsg);
    }

    private void preventReclaim(PutChunk rcvdMsg) {
        BackupProtocolInitiator backupProtocolInitiator = peer.getChannelCoordinator().getBackupInitiator();
        if (backupProtocolInitiator != null) {
            backupProtocolInitiator.setReceivedPutChunk(rcvdMsg.getFileId(), rcvdMsg.getChunkNo());
        }
    }


}
