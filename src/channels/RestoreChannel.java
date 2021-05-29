package channels;

import messages.protocol.Chunk;
import messages.protocol.ChunkEnhanced;
import peer.Peer;
import ssl.SslReceiver;
import utils.AddressPortList;
import utils.Utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
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
        return parseMsg(message);
    }

    public byte[] parseMsg(byte[] packetData) {
        int bodyStartPos = getBodyStartPos(packetData);
        byte[] header = Arrays.copyOfRange(packetData, 0, bodyStartPos - 4);
        //TODO sel alguma coisa der shit pode ser por estar a usar o packetData.length em vez de packet.getLength()
        //byte[] body = Arrays.copyOfRange(packetData, bodyStartPos, packet.getLength());
        byte[] body = Arrays.copyOfRange(packetData, bodyStartPos, packetData.length);

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

    public void handleChunkEnhancedMsg(ChunkEnhanced rcvdMsg) {

        if (!peer.hasRestoreEntry(rcvdMsg.getFileId())) return;

        int portNumber = rcvdMsg.getPortNumber();
        System.out.println("[TCP] Client port number: " + portNumber);

        try (Socket socket = new Socket("localhost", portNumber);
             BufferedInputStream in = new BufferedInputStream(socket.getInputStream())) {

            byte[] chunk = new byte[CHUNK_SIZE];
            int bytesRead = in.readNBytes(chunk, 0, CHUNK_SIZE);
            byte[] cleanChunk = Arrays.copyOf(chunk, bytesRead);
            in.close();
            System.out.println("[TCP] Read from TCP: " + bytesRead);
            socket.close();

            peer.addChunk(rcvdMsg.getFileId(), rcvdMsg.getChunkNo(), cleanChunk);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
