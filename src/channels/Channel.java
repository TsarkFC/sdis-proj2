package channels;

import peer.Peer;
import utils.AddressPort;
import utils.AddressPortList;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class Channel {
    protected final AddressPortList addressPortList;
    protected AddressPort currentAddr;

    protected Peer peer;
    protected int numOfThreads = 20;
    protected ThreadPoolExecutor executor;
    private final double MAX_SIZE = Math.pow(2, 16);

    public Channel(AddressPortList addressPortList, Peer peer) {
        this.addressPortList = addressPortList;
        this.peer = peer;
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numOfThreads);
    }

    public AddressPortList getAddrList() {
        return addressPortList;
    }

    public void handleMsg(byte[] message) {
        handle(message);
    }

    public abstract byte[] handle(byte[] message);

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
