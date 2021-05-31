package channels;

import messages.protocol.Chunk;
import peer.Peer;
import ssl.SslReceiver;
import utils.AddressPortList;
import utils.Utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

import static filehandler.FileHandler.CHUNK_SIZE;

public class RestoreChannel extends Channel {

    public RestoreChannel(AddressPortList addressPortList, Peer peer) {
        super(addressPortList, peer);
        super.currentAddr = addressPortList.getMdrAddressPort();

        SslReceiver receiver = new SslReceiver(currentAddr.getAddress(), currentAddr.getPort(), this);
        new Thread(receiver).start();
    }

    @Override
    public byte[] handle(byte[] message) {
        int bodyStartPos = getBodyStartPos(message);
        byte[] header = Arrays.copyOfRange(message, 0, bodyStartPos - 4);
        byte[] bodyCrlf = Arrays.copyOfRange(message, bodyStartPos, message.length);
        byte[] body = Utils.readUntilCRLF(bodyCrlf);

        String headerString = new String(header);
        System.out.println("[RECEIVED MESSAGE MDR] " + headerString);

        Chunk msg = new Chunk(headerString, body);
        String chunkId = msg.getFileId() + "-" + msg.getChunkNo();
        peer.addChunkReceived(chunkId);
        handleChunkMsg(msg);
        return Utils.discard();
    }


    public void handleChunkMsg(Chunk rcvdMsg) {
        peer.addChunk(rcvdMsg.getFileId(), rcvdMsg.getChunkNo(), rcvdMsg.getBody());
    }
}
