package ssl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SslSender extends Ssl implements Runnable {

    private SSLEngine engine;

    private SocketChannel channel;

    private String host;

    private int port;

    public SslSender(String protocol, String host, int port) {
        this.host = host;
        this.port = port;

        // SSLEngine creation
        SSLContext context;
        try {
            context = SSLContext.getInstance(protocol);
            context.init(createKeyManagers("./src/main/resources/client.jks", "123456", "123456"), createTrustManagers("./src/main/resources/trustedCerts.jks", "123456"), new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
            return;
        }
        engine = context.createSSLEngine(host, port);
        engine.setUseClientMode(true);

        // Obtain the currently empty SSLSession for the SSLEngine
        SSLSession session = engine.getSession();

        // Determine the maximum buffer sizes for the application and network bytes that could be generated
        decryptedData = ByteBuffer.allocate(session.getApplicationBufferSize());
        encryptedData = ByteBuffer.allocate(session.getPacketBufferSize());
    }

    public void connect() {
        // Once you have configured the connection and the buffers, call the beginHandshake() method
        // which moves the SSLEngine into the initial handshaking state.
        try {
            engine.beginHandshake();
        } catch (SSLException e) {
            e.printStackTrace();
            return;
        }

        // Create the transport mechanism that the connection will use with (SocketChannel)
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(host, port));
            while (!channel.finishConnect()) ;
        } catch (IOException e) {
            e.printStackTrace();
        }

        handshake(channel, engine);
    }

    @Override
    public void run() {
        //TODO: send messages
    }

    public void write(String message) {

    }

    public void read() {

    }

    public void shutdown() {

    }

    /*@Override
    public void run() {
        //TODO: send request -> implement when testing is complete
    }*/
}
