package ssl;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class SslSender extends Ssl implements Runnable {

    private final SSLEngine engine;

    private SocketChannel channel;

    private final String host;

    private final int port;

    public SslSender(String protocol, String host, int port) {
        this.host = host;
        this.port = port;

        //initializeSslContext(protocol, "123456", "./src/main/resources/client.jks", "./src/main/resources/trustedCerts.jks");
        initializeSslContext(protocol, "123456", "./src/ssl/resources/client.keys", "./src/ssl/resources/truststore");

        engine = context.createSSLEngine(host, port);
        engine.setUseClientMode(true);

        // Obtain the currently empty SSLSession for the SSLEngine
        SSLSession session = engine.getSession();

        allocateData(session);
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
            while (!channel.finishConnect());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (handshake(channel, engine)) {
            System.out.println("[Client] Handshake successful");
        } else {
            System.out.println("[Client] Handshake error!");
        }
    }

    @Override
    public void run() {
        //TODO: send messages
    }

    public void write(String message) {
        try {
            System.out.println("[Client] writing...");
            write(message, channel, engine);
        } catch (IOException e) {
            System.out.println("Error writing message");
            e.printStackTrace();
        }
    }

    public void read() {
        try {
            System.out.println("[Client] reading...");
            read(channel, engine);
        } catch (IOException e) {
            System.out.println("Error Reading message");
            e.printStackTrace();
        }
    }

    public void shutdown() {

    }

    @Override
    public void logReceivedMessage(String message) {
        System.out.println("Server response: " + message);
    }

    @Override
    protected void logSentMessage(String message) {
        System.out.println("Sent message to server: " + message);
    }
}
