package messages;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.List;

public class Sender implements Runnable {
    private final String ipAddr;
    private final int port;
    private final List<byte[]> messages;

    public Sender(String ipAddr, int port, List<byte[]> messages) {
        this.ipAddr = ipAddr;
        this.port = port;
        this.messages = messages;
    }

    @Override
    public void run() {
        MulticastSocket socket;
        try {
            socket = new MulticastSocket();
            InetAddress group = InetAddress.getByName(ipAddr);
            for (byte[] msg : messages) {
                DatagramPacket datagramPacket = new DatagramPacket(msg, msg.length, group, port);
                socket.send(datagramPacket);
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
