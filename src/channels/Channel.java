package channels;

import peer.Peer;
import ssl.SSlArgs;
import ssl.SslReceiver;
import utils.AddressList;
import utils.ChannelAddress;
import utils.AddressPort;
import utils.AddressPortList;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class Channel extends SslReceiver {
    protected final AddressPortList addressPortList;
    protected AddressPort currentAddr;
    
    protected Peer peer;
    protected int numOfThreads = 20;
   protected ThreadPoolExecutor executor;
    private final double MAX_SIZE = Math.pow(2, 16);

    public Channel(AddressPortList addressPortList, Peer peer) {
        super(peer.getArgs().getSslInformation());
        this.addressPortList = addressPortList;
        this.peer = peer;
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numOfThreads);
    }
    public AddressPortList getAddrList() {
        return addressPortList;
    }

    public void handleMsg(byte[] message){
        handle(message);
    }

    public abstract void handle(DatagramPacket packet) throws IOException;

    public abstract void handle(byte[] message);

    /*@Override
    public void run() {
        /*try {
            InetAddress mcastAddr = InetAddress.getByName(this.currentAddr.getAddress());
            MulticastSocket mcastSocket;
            mcastSocket = new MulticastSocket(currentAddr.getPort());
            mcastSocket.joinGroup(mcastAddr);

            while (true) {
                byte[] rbuf = new byte[(int) MAX_SIZE];
                DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
                mcastSocket.receive(packet);
                executor.execute(() -> {
                    try {
                        handle(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    protected int getBodyStartPos(byte[] msg) {
        int crlf = 0;
        int CR = 0xD;
        int LF = 0xA;
        for (int i = 0; i < msg.length - 1; i++) {
            if (msg[i] == CR && msg[i + 1] == LF && crlf == 1) {
                return i + 2;
            } else if (msg[i] == CR && msg[i + 1] == LF) {
                crlf++;
                i++;
            } else crlf = 0;
        }
        return 0;
    }

    public AddressPortList getAddressPortList() {
        return addressPortList;
    }
}
