package ssl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SslReceiver extends Ssl {

    private SSLContext context;
    private SSLEngine engine;

    //It can be made more robust and scalable by using a Selector with the non-blocking SocketChannel
    private Selector selector;


    public SslReceiver(String protocol, String host, int port) {
        try {
            context = SSLContext.getInstance(protocol);
            context.init(createKeyManagers("./src/main/resources/client.jks", "123456", "123456"), createTrustManagers("./src/main/resources/trustedCerts.jks", "123456"), new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
            return;
        }
        engine = context.createSSLEngine(host, port);
        engine.setUseClientMode(false);

        SSLSession session = engine.getSession();
        decryptedData = ByteBuffer.allocate(session.getApplicationBufferSize());
        encryptedData = ByteBuffer.allocate(session.getPacketBufferSize());

        peerEncryptedData = ByteBuffer.allocate(session.getApplicationBufferSize());
        peerDecryptedData = ByteBuffer.allocate(session.getPacketBufferSize());

        session.invalidate();

        this.createServerSocketChannel(host, port);


    }

    public void createServerSocketChannel(String host, int port) {
        try {
            selector = SelectorProvider.provider().openSelector();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(host, port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Connection completed Successfully");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Starts listening to new connections
    //Run in a loop as long the server is active
    public void start() {

    }

    public void stop() {

    }

    public void accept() {

    }

    public void read() {

    }

    public void write() {

    }

    /*@Override
    public void run() {
        //TODO: receive and process requests -> implement when testing is complete
    }*/
}
