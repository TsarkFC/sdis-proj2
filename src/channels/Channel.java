package channels;

import chord.ChordNode;
import peer.Peer;
import utils.AddressPort;
import utils.AddressPortList;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class Channel {
    protected final AddressPortList addressPortList;
    protected AddressPort currentAddr;
    protected Peer peer;

    public Channel(AddressPortList addressPortList, Peer peer) {
        this.addressPortList = addressPortList;
        this.peer = peer;
    }

    public abstract byte[] handle(byte[] message);

    protected int getBodyStartPos(byte[] msg) {
        int CR = 0xD;
        int LF = 0xA;
        for (int i = 0; i < msg.length - 3; i++) {
            if (msg[i] == CR && msg[i + 1] == LF && msg[i + 2] == CR && msg[i + 3] == LF) {
                return i + 4;
            }
        }
        System.out.println("COULD NOT FIND <CRLF><CRLF>");
        return 0;
    }

    public AddressPortList getAddressPortList() {
        return addressPortList;
    }

    protected boolean shouldResend(String chunkFileId) {
        int fileChordID = peer.getChordNode().generateHash(chunkFileId);
        ChordNode node = peer.getChordNode();
        if (node.getId() == node.getSuccessor().getId()) return false;
        return !node.isInInterval(fileChordID, node.getId(), node.getSuccessor().getId());
    }
}
