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

    public static void sendTCPMessageMC(String fileName, Peer peer, byte[] msg) {
        int chordID = peer.getChordNode().generateHash(fileName);
        ChordNodeData chordNodeData = peer.getChordNode().findSuccessor(chordID);
        AddressPort addrPortMc = chordNodeData.getAddressPortList().getMcAddressPort();
        ThreadHandler.sendTCPMessage(addrPortMc.getAddress(),
                addrPortMc.getPort(), msg);
    }

    public static void sendTCPMessageMDB(String fileName, Peer peer, byte[] msg) {
        int chordID = peer.getChordNode().generateHash(fileName);
        ChordNodeData chordNodeData = peer.getChordNode().findSuccessor(chordID);
        AddressPort addrPortMc = chordNodeData.getAddressPortList().getMdbAddressPort();
        ThreadHandler.sendTCPMessage(addrPortMc.getAddress(),
                addrPortMc.getPort(), msg);
    }
}
