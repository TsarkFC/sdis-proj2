package utils;

import ssl.SslSender;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadHandler {
    public static void startMulticastThread(String mcastAddr, int mcastPort, List<byte[]> messages) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Multicast multicastThread = new Multicast(mcastPort, mcastAddr, messages);
        executor.schedule(multicastThread, 0, TimeUnit.SECONDS);
    }

    public static void sendTCPMessage(String ipAddress, int port, List<byte[]> messages){
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        SslSender tcpThread = new SslSender(ipAddress,port,messages);
        executor.schedule(tcpThread, 0, TimeUnit.SECONDS);
    }
}
