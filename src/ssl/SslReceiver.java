package ssl;

import javax.net.ssl.SSLEngine;
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

    /**
     * It can be made more robust and scalable by using a Selector with the non-blocking SocketChannel
     */
    private Selector selector;

    private boolean isActive = true;


    public SslReceiver(String protocol, String host, int port) {
        //TODO: Falta adicionar a outra password
        initializeSslContext(protocol, "123456", "./src/ssl/resources/server.keys", "./src/ssl/resources/truststore");

        SSLEngine engine = context.createSSLEngine(host, port);
        engine.setUseClientMode(false);

        SSLSession session = engine.getSession();
        allocateData(session);

        //session.invalidate();

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
                accept(key);
            } catch (IOException e) {
                System.out.println("Exception accepting connection");
                e.printStackTrace();
            }
        } else if (key.isReadable()) {
            SocketChannel channel = (SocketChannel) key.channel();
            SSLEngine engine = (SSLEngine) key.attachment();
            receive(channel, engine);
            send(channel, engine);
        }
    }

    public void accept(SelectionKey key) throws IOException {
        System.out.println("Accepting key");
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
        channel.configureBlocking(false);

        SSLEngine engine = context.createSSLEngine();
        engine.setUseClientMode(false);
        engine.beginHandshake();
        if (handshake(channel, engine)) {
            System.out.println("[Server] Handshake successful");
            channel.register(selector, SelectionKey.OP_READ, engine);
            allocateData(engine.getSession());
        } else {
            System.out.println("[Server] Closing socket channel due to bad handshake");
            channel.close();
        }
    }

    @Override
    protected void logReceivedMessage(String message) {
        System.out.println("Incoming message: " + message);

    }

    @Override
    protected void logSentMessage(String message) {
        System.out.println("Sent response: " + message);
    }

    public void send(SocketChannel channel, SSLEngine engine) {
        String response = "HeyHey";
        System.out.println("[Server] attempting to write...");
        try {
            System.out.println("[Server] writing...");
            write(response, channel, engine);
        } catch (IOException e) {
            System.out.println("Error trying to respond to client");
            e.printStackTrace();
        }
    }

    public void receive(SocketChannel channel, SSLEngine engine) {
        System.out.println("[Server] attempting to read...");
        try {
            System.out.println("[Server] reading...");
            System.out.println("[Server] read " + read(channel, engine) + " bytes");
        } catch (IOException e) {
            System.out.println("Error Reading message");
            e.printStackTrace();
        }
    }
}
