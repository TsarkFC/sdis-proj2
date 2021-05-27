package ssl;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
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

        //initializeSslContext(protocol, "123456", "./src/main/resources/client.jks", "./src/main/resources/trustedCerts.jks");
        initializeSslContext(protocol, "123456", "./src/ssl/resources/client.keys", "./src/ssl/resources/truststore");

        engine = context.createSSLEngine(host, port);
        engine.setUseClientMode(true);

        //allocateData(engine.getSession());
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
            System.out.println("[Client] Handshake successful");
            //allocateData(engine.getSession());
        } else {
            System.out.println("[Client] Handshake error!");
        }
    }

    @Override
    public void run() {
        //TODO: send messages
    }

    public void write(String message) {
        /*SSLSession session = engine.getSession();
        byte[] msg = message.getBytes();
        int encryptedBufferSize = session.getPacketBufferSize();
        int decryptedBufferSize =Math.max(session.getApplicationBufferSize(), msg.length);

        ByteBuffers byteBuffers = new ByteBuffers(encryptedBufferSize,decryptedBufferSize,false);*/
        try {
            System.out.println("[Client] writing...");
            write(message, channel, engine);
        } catch (IOException e) {
            System.out.println("Error writing message");
            e.printStackTrace();
        }
    }

    public void read() {
        System.out.println("[Client] attempting to read...");
        int tries = 0;

        while (tries < 5) {
            tries++;
            try {
                System.out.println("[Client] reading...");
                int nBytes = read(channel, engine);
                System.out.println("[Client] read " + nBytes + " bytes");
                if (nBytes > 0) break;
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
    protected void logSentMessage(String message) {
        System.out.println("Sent message to server: " + message);
    }

    @Override
    public void handleSSlMsg(byte[] msg) {
        logReceivedMessage(msg);
    }
}
