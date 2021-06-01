package ssl;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class SslSender extends Ssl implements Runnable {

    private final SSLEngine engine;

    private SocketChannel channel;

    private final String host;

    private final int port;

    private static String protocol;

    private final byte[] message;

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    public SslSender(String host, int port, byte[] message) {
        this.host = host;
        this.port = port;
        this.message = message;

        initializeSslContext(protocol, SSLInformation.clientKeys);
        engine = context.createSSLEngine(host, port);
        engine.setUseClientMode(true);
    }

    public static String getProtocol() {
        return protocol;
    }

    public static void setProtocol(String protocol) {
        SslSender.protocol = protocol;
    }

    public boolean connect() {
        try {
            engine.beginHandshake();
        } catch (SSLException e) {
            System.out.println("Could not start handshake state!");
            return false;
        }
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(host, port));
            while (!channel.finishConnect()) ;
        } catch (Exception e) {
            System.out.println("Socket exception!");
            return false;
        }

        try {
            return handshake(channel, engine);
        } catch (Exception e) {
            System.out.println("[Client] could not perform handshake!");
            return false;
        }
    }

    public void start() {
        connect();
        write(message);
    }

    @Override
    public void run() {
        start();
    }


    public void write(byte[] message) {
        write(message, channel, engine);
    }

    public byte[] read() {
        //System.out.println("[Client] attempting to read...");
        int tries = 1;

        while (tries <= 15) {
            tries++;
            //System.out.println("[Client] reading...");
            byte[] message = read(channel, engine);
            //System.out.println("[Client] read " + (message == null ? "null" : message.length) + " bytes");
            if (message != null) return message;
            try {
                Thread.sleep(10L * tries);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("[Client] could not read! tries = " + (tries - 1));
        return null;
    }

    public void shutdown() {
        disconnect(channel, engine);
        executor.shutdown();
    }

    @Override
    public void logReceivedMessage(byte[] message) {
        System.out.println("Server response: " + new String(message));
    }

    @Override
    protected void logSentMessage(byte[] message) {
        System.out.println("Sent message to server: " + new String(message));
    }
}
