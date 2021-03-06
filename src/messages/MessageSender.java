package messages;

import chord.ChordNodeData;
import peer.Peer;
import ssl.SSLSender;
import utils.AddressPort;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MessageSender {
    public static void sendTCPMessage(String ipAddress, int port, byte[] message) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        SSLSender tcpThread = new SSLSender(ipAddress, port, message);
        executor.schedule(tcpThread, 0, TimeUnit.SECONDS);
    }

    public static void sendTCPMessageMC(String fileId, Peer peer, byte[] msg) {
        int chordID = peer.getChordNode().generateHash(fileId);
        ChordNodeData chordNodeData = peer.getChordNode().findSuccessor(chordID);
        AddressPort addrPortMc = chordNodeData.getAddressPortList().getMcAddressPort();
        MessageSender.sendTCPMessage(addrPortMc.getAddress(),
                addrPortMc.getPort(), msg);
    }

    public static void sendTCPMessageMDB(String fileId, Peer peer, byte[] msg) {
        int chordID = peer.getChordNode().generateHash(fileId);
        ChordNodeData chordNodeData = peer.getChordNode().findSuccessor(chordID);
        AddressPort addrPortMdb = chordNodeData.getAddressPortList().getMdbAddressPort();
        MessageSender.sendTCPMessage(addrPortMdb.getAddress(),
                addrPortMdb.getPort(), msg);
    }


    public static void sendTCPMessageMDBSuccessor(Peer peer, byte[] msg) {
        ChordNodeData chordNodeData = peer.getChordNode().getSuccessor();
        AddressPort addrPortMc = chordNodeData.getAddressPortList().getMdbAddressPort();
        MessageSender.sendTCPMessage(addrPortMc.getAddress(),
                addrPortMc.getPort(), msg);
    }

    public static void sendTCPMessageMCSuccessor(Peer peer, byte[] msg) {
        ChordNodeData chordNodeData = peer.getChordNode().getSuccessor();
        AddressPort addrPortMc = chordNodeData.getAddressPortList().getMcAddressPort();
        MessageSender.sendTCPMessage(addrPortMc.getAddress(),
                addrPortMc.getPort(), msg);
    }
}
