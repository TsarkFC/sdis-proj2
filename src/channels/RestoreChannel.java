package channels;

import messages.protocol.Chunk;
import peer.Peer;
import ssl.SslReceiver;
import utils.AddressPortList;
import utils.Utils;

import java.util.Arrays;

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
        peer.addChunk(msg.getFileId(), msg.getChunkNo(), msg.getBody());
        return null;
    }
}
