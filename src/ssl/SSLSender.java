package ssl;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class SSLSender extends SSL implements Runnable {

    private final SSLEngine engine;

    private SocketChannel channel;

    private final String host;

    private final int port;

    private static String protocol;

    private final byte[] message;

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    public SSLSender(String host, int port, byte[] message) {
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
        SSLSender.protocol = protocol;
    }

    public boolean connect() {
        try {
            engine.beginHandshake();
        } catch (SSLException e) {
            System.out.println("[SSL CONNECT CLIENT] Could not start handshake state!");
            return false;
        }
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(host, port));
            while (!channel.finishConnect()) ;
        } catch (Exception e) {
            System.out.println("[SSL CONNECT CLIENT] Socket exception!");
            return false;
        }

        try {
            return handshake(channel, engine);
        } catch (Exception e) {
            System.out.println("[SSL CONNECT CLIENT] could not perform handshake!");
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
        int tries = 1;

        while (tries <= 50) {
            tries++;
            byte[] message = read(channel, engine);
            if (message != null) return message;
            try {
                Thread.sleep(Math.min(10L + 5L * tries, 500));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("[SSL CLIENT READ] could not read! tries = " + (tries - 1));
        try {
            engine.closeInbound();
        } catch (SSLException e) {
            System.out.println("[SSL CLIENT READ] Could not close inbound!");
        }
        shutdown();
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
