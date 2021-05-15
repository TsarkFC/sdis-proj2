package utils;

import java.io.IOException;
import java.net.*;
import java.util.List;

public class Multicast implements Runnable {

    private final String mcastAddr;
    private final int mcastPort;
    private final List<byte[]> messages;

    public Multicast(int mcastPort, String mcastAddr, List<byte[]> messages) {
        this.mcastAddr = mcastAddr;
        this.mcastPort = mcastPort;
        this.messages = messages;
    }

    @Override
    public void run() {
        MulticastSocket socket;
        try {
            socket = new MulticastSocket();
            InetAddress group = InetAddress.getByName(mcastAddr);
            for (byte[] msg : messages) {
                DatagramPacket datagramPacket = new DatagramPacket(msg, msg.length, group, mcastPort);
                socket.send(datagramPacket);
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
