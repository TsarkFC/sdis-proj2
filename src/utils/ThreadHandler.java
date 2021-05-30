package utils;

import chord.ChordNodeData;
import messages.protocol.Message;
import peer.Peer;
import ssl.SslSender;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadHandler {
    public static void sendTCPMessage(String ipAddress, int port, byte[] message) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        SslSender tcpThread = new SslSender(ipAddress, port, message);
        executor.schedule(tcpThread, 0, TimeUnit.SECONDS);
    }

    public static void sendTCPMessageMC(String fileId, Peer peer, byte[] msg) {
        int chordID = peer.getChordNode().generateHash(fileId);
        ChordNodeData chordNodeData = peer.getChordNode().findSuccessor(chordID);
        AddressPort addrPortMc = chordNodeData.getAddressPortList().getMcAddressPort();
        ThreadHandler.sendTCPMessage(addrPortMc.getAddress(),
                addrPortMc.getPort(), msg);
    }

    public static void sendTCPMessageMDB(String fileId, Peer peer, byte[] msg) {
        int chordID = peer.getChordNode().generateHash(fileId);
        ChordNodeData chordNodeData = peer.getChordNode().findSuccessor(chordID);
        AddressPort addrPortMdb = chordNodeData.getAddressPortList().getMdbAddressPort();
        ThreadHandler.sendTCPMessage(addrPortMdb.getAddress(),
                addrPortMdb.getPort(), msg);
    }

    public static void sendTCPMessageMDR(String fileId, Peer peer, byte[] msg) {
        int chordID = peer.getChordNode().generateHash(fileId);
        ChordNodeData chordNodeData = peer.getChordNode().findSuccessor(chordID);
        AddressPort addrPortMdr = chordNodeData.getAddressPortList().getMdrAddressPort();
        ThreadHandler.sendTCPMessage(addrPortMdr.getAddress(),
                addrPortMdr.getPort(), msg);
    }
}
