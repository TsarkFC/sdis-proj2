package ssl;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class SslSender extends Ssl implements Runnable {

    private final SSLEngine engine;

    private SocketChannel channel;

    private final String host;

    private final int port;

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    public SslSender(String protocol, String host, int port) {
        this.host = host;
        this.port = port;

        initializeSslContext(protocol, "../ssl/resources/client.keys");

        engine = context.createSSLEngine(host, port);
        engine.setUseClientMode(true);

        allocateData(engine.getSession());
    }

    public void connect() {
        try {
            engine.beginHandshake();
        } catch (SSLException e) {
            e.printStackTrace();
            return;
        }
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(host, port));
            while (!channel.finishConnect()) ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (handshake(channel, engine)) {
            //System.out.println("[Client] Handshake successful");
            allocateData(engine.getSession());
        } else {
            //System.out.println("[Client] Handshake error!");
        }
    }

    @Override
    public void run() {
        //TODO: send messages
    }

    public void write(byte[] message) {
        try {
            //System.out.println("[Client] writing...");
            write(message, channel, engine);
        } catch (IOException e) {
            System.out.println("Error writing message");
            e.printStackTrace();
        }
    }

    public byte[] read() {
        //System.out.println("[Client] attempting to read...");
        int tries = 0;

        while (tries < 15) {
            tries++;
            try {
                //System.out.println("[Client] reading...");
                byte[] message = read(channel, engine);
                //System.out.println("[Client] read " + (message == null ? "null" : message.length) + " bytes");
                if (message != null) return message;
            } catch (IOException e) {
                System.out.println("Error Reading message");
                e.printStackTrace();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("[Client] could not read! tries = " + tries);
        return null;
    }

    public void shutdown() {
        disconnect(channel, engine);
        executor.shutdown();
    }

    @Override
    public void logReceivedMessage(String message) {
        System.out.println("[Client] Server response: " + message);
    }

    @Override
    protected void logSentMessage(String message) {
        System.out.println("[Client] Sent message to server: " + message);
    }
}
