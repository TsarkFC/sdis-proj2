package ssl;

import channels.ChordChannel;

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

public class SslReceiver extends Ssl implements Runnable {

    /**
     * It can be made more robust and scalable by using a Selector with the non-blocking SocketChannel
     */
    private Selector selector;

    /**
     * States whether the receiving thread is active or not
     */
    private boolean isActive = true;

    /**
     * The peer receiving the messages
     */
    private ChordChannel handlerChannel;

    public SslReceiver(String protocol, String host, int port, ChordChannel handlerChannel) {
        initializeSslContext(protocol, "../ssl/resources/server.keys");

        SSLEngine engine = context.createSSLEngine(host, port);
        engine.setUseClientMode(false);

        SSLSession session = engine.getSession();
        allocateData(session);

        this.createServerSocketChannel(host, port);

        this.handlerChannel = handlerChannel;
    }

    public SslReceiver(String protocol, String host, int port) {
        initializeSslContext(protocol, "../ssl/resources/server.keys");

        SSLEngine engine = context.createSSLEngine(host, port);
        engine.setUseClientMode(false);

        SSLSession session = engine.getSession();
        allocateData(session);

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
        //System.out.println("[Server] Initialized and waiting for new connections...");

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
            byte[] message = receive(channel, engine);

            if (message != null) {
                //System.out.println("[Server] sending...");
                send(channel, engine, handlerChannel.handle(message));
            }
        }
    }

    public void accept(SelectionKey key) throws IOException {
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
        channel.configureBlocking(false);

        SSLEngine engine = context.createSSLEngine();
        engine.setUseClientMode(false);
        engine.beginHandshake();
        if (handshake(channel, engine)) {
            //System.out.println("[Server] Handshake successful");
            channel.register(selector, SelectionKey.OP_READ, engine);
            allocateData(engine.getSession());
        } else {
            //System.out.println("[Server] Closing socket channel due to bad handshake");
            channel.close();
        }
    }

    public void send(SocketChannel channel, SSLEngine engine, byte[] response) {
        //System.out.println("[Server] attempting to write...");
        try {
            //System.out.println("[Server] writing...");
            write(response, channel, engine);
        } catch (IOException e) {
            System.out.println("Error trying to respond to client");
            e.printStackTrace();
        }
    }

    public byte[] receive(SocketChannel channel, SSLEngine engine) {
        //System.out.println("[Server] attempting to read...");
        try {
            //System.out.println("[Server] reading...");
            return read(channel, engine);
        } catch (IOException e) {
            System.out.println("Error Reading message");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void logReceivedMessage(String message) {
        System.out.println("Incoming message: " + message);
    }

    @Override
    protected void logSentMessage(String message) {
        System.out.println("Sent response: " + message);
    }

    @Override
    public void run() {
        try {
            start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
