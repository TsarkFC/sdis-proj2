package messages.handlers;

import filehandler.FileHandler;
import messages.protocol.Chunk;
import messages.protocol.GetChunk;
import peer.Peer;
import messages.MessageSender;
import utils.Utils;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GetChunkHandler {
    public void handleGetChunkMsg(GetChunk rcvdMsg, Peer peer) {
        new ScheduledThreadPoolExecutor(1).
                schedule(() -> getAndSendChunk(rcvdMsg, peer), Utils.generateRandomDelay("[RESTORE] Send Chunk msg after "), TimeUnit.MILLISECONDS);
    }

    private void getAndSendChunk(GetChunk rcvdMsg, Peer peer) {
        byte[] chunk = FileHandler.getChunk(rcvdMsg, peer.getFileSystem());
        if (chunk == null){
            System.out.println("Chunk is null");
            return;
        }

        Chunk msg = new Chunk(rcvdMsg.getIpAddress(), peer.getArgs().getPeerId(), rcvdMsg.getFileId(),
                rcvdMsg.getChunkNo(), chunk);
        byte[] message = msg.getBytes();

        String chunkId = rcvdMsg.getFileId() + "-" + rcvdMsg.getChunkNo();
        if (peer.hasReceivedChunk(chunkId)) return;
        MessageSender.sendTCPMessage(rcvdMsg.getIpAddress(), rcvdMsg.getPort(), message);
    }
}
