package ssl;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Iterator;

public class SslReceiver extends Ssl {


    private SSLEngine engine;

    //It can be made more robust and scalable by using a Selector with the non-blocking SocketChannel
    private Selector selector;

    private boolean isActive = true;


    public SslReceiver(String protocol, String host, int port) {
        //Falta adicionar a outra password
        initializeSslContext(protocol, "123456", "./src/ssl/resources/server.keys", "./src/ssl/resources/truststore");

        engine = context.createSSLEngine(host, port);
        engine.setUseClientMode(false);

        SSLSession session = engine.getSession();
        allocateData(session);

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

        System.out.println("Initialized and waiting for new connections...");

        while (isActive) {
            try {
                selector.select();
            } catch (IOException e) {
                System.out.println("Error selecting selector");
                e.printStackTrace();
            }

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                //System.out.println("Ele estar a chegar aqui quer dizer que o client ja se concetou direito acho");
                SelectionKey key = selectedKeys.next();
                selectedKeys.remove();
                handleKey(key);
            }
        }
        System.out.println("Goodbye!");

    }


    public void stop() {

    }

    public void handleKey(SelectionKey key) {

        if (!key.isValid()) return;
        if (key.isAcceptable()) {
            try {
                //Se nao for pelo handshake ele fica sempre a mostrar o accepting key
                accept(key);
            } catch (IOException e) {
                System.out.println("Exception accepting connection");
                e.printStackTrace();
            }
        } else if (key.isReadable()) {
            try {
                read((SocketChannel) key.channel(), (SSLEngine) key.attachment());
            } catch (IOException e) {
                System.out.println("Exception Reading");
                e.printStackTrace();
            }
        }
    }

    public void accept(SelectionKey key) throws IOException {
        System.out.println("Accepting key");
        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
        socketChannel.configureBlocking(false);

        SSLEngine sslEngine = context.createSSLEngine();
        sslEngine.setUseClientMode(false);
        sslEngine.beginHandshake();
        if (handshake(socketChannel, sslEngine)) {
            System.out.println("Handshake successful");
            socketChannel.register(selector, SelectionKey.OP_READ, sslEngine);
        } else {
            System.out.println("Closing socket channel due to bad handshake");
            socketChannel.close();
        }
    }

    public void read(SocketChannel socketChannel, SSLEngine sslEngine) throws IOException {
        System.out.println("Reading key");
        peerEncryptedData.clear();
        //TODO This is just pseudocode
        // Read SSL/TLS encoded data from peer
        int num = socketChannel.read(peerEncryptedData);
        if (num == -1) {
            // Handle closed channel
            System.out.println("Handle closed Channel");
        } else if (num == 0) {
            System.out.println("No bytes read, try again");
        } else {
            // Process incoming data
            peerEncryptedData.flip();
            while (peerDecryptedData.hasRemaining()) {
                SSLEngineResult res = engine.unwrap(peerEncryptedData, peerDecryptedData);
                switch (res.getStatus()) {
                    case OK:
                        peerEncryptedData.compact();
                        //Aqui em vez de compact talvez seja flip
                        System.out.println("Incoming message: " + new String(decryptedData.array()));
                        //Use peer decrypted data
                        break;
                    // Handle other status:  BUFFER_OVERFLOW, BUFFER_UNDERFLOW, CLOSED
                    case BUFFER_OVERFLOW:
                        break;
                    case BUFFER_UNDERFLOW:
                        break;
                    case CLOSED:
                        break;
                    default:
                        break;

                }
            }

        }
    }

    public void write() {

    }

    /*@Override
    public void run() {
        //TODO: receive and process requests -> implement when testing is complete
    }*/
}
