package utils;

import ssl.SslSender;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadHandler {
    public static void sendTCPMessage(String ipAddress, int port, byte[] message) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        SslSender tcpThread = new SslSender(ipAddress, port, message);
        executor.schedule(tcpThread, 0, TimeUnit.SECONDS);
    }
}
