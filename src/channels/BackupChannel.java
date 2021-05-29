package channels;

import messages.protocol.PutChunk;
import messages.protocol.Stored;
import peer.Peer;
import protocol.BackupProtocolInitiator;
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
        addServer(currentAddr.getAddress(),currentAddr.getPort());
    }
    //TODO APAGAR ESTE
    @Override
    public void handle(DatagramPacket packet) {

        byte[] packetData = packet.getData();
        parseMsg(packetData);
    }

    @Override
    public void handle(byte[] message) {
        parseMsg(message);
    }

    public void parseMsg(byte[] packetData){
        int bodyStartPos = getBodyStartPos(packetData);
        byte[] header = Arrays.copyOfRange(packetData, 0, bodyStartPos - 4);
        byte[] body = Arrays.copyOfRange(packetData, bodyStartPos, packetData.length);

        String rcvd = new String(header, 0, header.length);
        System.out.println("[RECEIVED MESSAGE MDB] " + rcvd);
        PutChunk rcvdMsg = new PutChunk(rcvd, body);

        if (shouldSaveFile(rcvdMsg)) {
            String delayMsg;
            if (peer.isVanillaVersion()) {
                saveChunk(rcvdMsg);
                delayMsg = "[BACKUP] Sending stored messages after: ";
            } else delayMsg = "[BACKUP] Initiate Backup after: ";
            new ScheduledThreadPoolExecutor(1).schedule(() -> sendStored(rcvdMsg),
                    Utils.generateRandomDelay(delayMsg), TimeUnit.MILLISECONDS);
        } else {
            if (!peer.isVanillaVersion()) {
                peer.getMetadata().getStoredChunksMetadata().deleteChunk(rcvdMsg.getFileId(), rcvdMsg.getChunkNo());
                peer.getMetadata().getStoredChunksMetadata().receivedPutChunk(rcvdMsg.getFileId(), rcvdMsg.getChunkNo(), peer);
                peer.getMetadata().writeMetadata();
            }
        }

    }

    private boolean shouldSaveFile(PutChunk rcvdMsg) {
        boolean sameSenderPeer = rcvdMsg.getSenderId().equals(peer.getArgs().getPeerId());
        boolean hasSpace = peer.getMetadata().hasSpace(rcvdMsg.getBody().length / 1000.0);
        boolean isOriginalFileSender = peer.getMetadata().hasFile(rcvdMsg.getFileId());
        return !sameSenderPeer && hasSpace && !isOriginalFileSender;
    }

    private void saveStateMetadata(PutChunk rcvdMsg) {
        peer.getMetadata().updateStoredInfo(rcvdMsg.getFileId(), rcvdMsg.getChunkNo(), rcvdMsg.getReplicationDeg(),
                rcvdMsg.getBody().length / 1000.0, peer.getArgs().getPeerId());
    }

    public void saveChunk(PutChunk rcvdMsg) {
        System.out.println("[BACKUP] Backing up file " + rcvdMsg.getFileId() + "-" + rcvdMsg.getChunkNo() + "from " + rcvdMsg.getSenderId());
        preventReclaim(rcvdMsg);
        FileHandler.saveChunk(rcvdMsg, peer.getFileSystem());
        saveStateMetadata(rcvdMsg);
    }

    public void sendStored(PutChunk rcvdMsg) {
        if (!alreadyReachedRepDgr(rcvdMsg.getFileId(), rcvdMsg.getChunkNo(), rcvdMsg.getReplicationDeg())) {
            Stored confMsg = new Stored(rcvdMsg.getVersion(), peer.getArgs().getPeerId(), rcvdMsg.getFileId(), rcvdMsg.getChunkNo());
            sendStoredMsg(confMsg.getBytes());
            if (!peer.isVanillaVersion()) {
                //peer.getMetadata().getStoredChunksMetadata().deleteChunksSize0(rcvdMsg.getFileId(), rcvdMsg.getChunkNo());
                saveChunk(rcvdMsg);
            }

        } else {
            System.out.println("[BACKUP] Not backing up because reached perceived rep degree");
            if (!peer.isVanillaVersion()) {
                peer.getMetadata().getStoredChunksMetadata().deleteChunk(rcvdMsg.getFileId(), rcvdMsg.getChunkNo());
            }
        }
    }

    private void sendStoredMsg(byte[] msg) {
        List<byte[]> msgs = new ArrayList<>();
        msgs.add(msg);
        ThreadHandler.sendTCPMessage(addressPortList.getMcAddressPort().getAddress(), addressPortList.getMcAddressPort().getPort(), msgs);
    }

    private void preventReclaim(PutChunk rcvdMsg) {
        BackupProtocolInitiator backupProtocolInitiator = peer.getChannelCoordinator().getBackupInitiator();
        if (backupProtocolInitiator != null) {
            backupProtocolInitiator.setReceivedPutChunk(rcvdMsg.getFileId(), rcvdMsg.getChunkNo());
        }
    }

    private boolean alreadyReachedRepDgr(String fileId, int chunkNo, int repDgr) {
        if (peer.isVanillaVersion()) return false;
        int stored = peer.getMetadata().getStoredChunksMetadata().getStoredCount(fileId, chunkNo);
        System.out.println("[BACKUP] Replication Degree = " + repDgr + " and perceived = " + stored + " of file " + fileId);
        return (stored >= repDgr);

    }
}
