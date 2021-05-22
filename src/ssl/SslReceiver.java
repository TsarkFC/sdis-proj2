package ssl;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
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
        isActive = false;
        //executor.shutdown();
        selector.wakeup();
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
                read((SocketChannel) key.channel(), engine);
                handleMessage((SocketChannel) key.channel());
            } catch (IOException e) {
                System.out.println("Exception Reading");
                e.printStackTrace();
            }
        }
    }

    public String accept(SelectionKey key) throws IOException {
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
        return "not";
    }

    public void receiveMessage(String message) {
        System.out.println("Incoming message: " + message);

    }

    public void handleMessage(SocketChannel socketChannel) {
        String response = "HeyHey";
        try {
            write(socketChannel, response);
        } catch (IOException e) {
            System.out.println("Error trying to respond to client");
            e.printStackTrace();
        }
    }


    public void write(SocketChannel socketChannel, String message) throws IOException {
        peerDecryptedData.clear();
        peerDecryptedData.put(message.getBytes());
        peerDecryptedData.flip();

        // The loop has a meaning for (outgoing) messages larger than 16KB.
        // Every wrap call will remove 16KB from the original message and send it to the remote peer.
        while (peerDecryptedData.hasRemaining()) {

            peerEncryptedData.clear();
            SSLEngineResult result = engine.wrap(peerDecryptedData, peerEncryptedData);
            switch (result.getStatus()) {
                case OK:
                    peerEncryptedData.flip();
                    while (peerEncryptedData.hasRemaining()) {
                        socketChannel.write(peerEncryptedData);
                    }
                    break;
                case BUFFER_OVERFLOW:
                    System.out.println("Overflowing when writing");
                    break;
                case BUFFER_UNDERFLOW:
                    throw new SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here.");
                case CLOSED:
                    System.out.println("Close connection");
                    return;
                default:
                    throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
            }
        }

    }








    /*@Override
    public void run() {
        //TODO: receive and process requests -> implement when testing is complete
    }*/
}
