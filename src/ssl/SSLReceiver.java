package ssl;

import channels.Channel;

import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

public class SSLReceiver extends SSL implements Runnable {

    /**
     * It can be made more robust and scalable by using a Selector with the non-blocking SocketChannel
     */
    private Selector selector;

    /**
     * States whether the receiving thread is active or not
     */
    private boolean isActive = true;

    /**
     * Channel receiving messages
     */
    private Channel handlerChannel;

    public SSLReceiver(String host, Integer port, Channel handlerChannel) {
        initializeSslContext(SSLInformation.protocol, SSLInformation.serverKeys);
        try {
            selector = SelectorProvider.provider().openSelector();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.createServer(host, port);
        this.handlerChannel = handlerChannel;
    }

    public SSLReceiver(String protocol, String serverKeys, String trustStore, String password) {
        initializeSslContext(protocol, SSLInformation.serverKeys);
        try {
            selector = SelectorProvider.provider().openSelector();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createServer(String ipAddress, int port) {
        System.out.println("[SSL] Adding server in ip = " + ipAddress + "; port = " + port + ";");
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(ipAddress, port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            System.out.println("Error Adding server");
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

            byte[] message = read(channel, engine);
            if (message != null) {
                byte[] response = handlerChannel.handle(message);
                if (response != null) write(response, channel, engine);
            }
        }
    }

    public void accept(SelectionKey key) throws IOException {
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
        channel.configureBlocking(false);

        SSLEngine engine = context.createSSLEngine();
        engine.setUseClientMode(false);

        try {
            engine.beginHandshake();
            if (handshake(channel, engine))
                channel.register(selector, SelectionKey.OP_READ, engine);
            else channel.close();
        } catch (Exception e) {
            System.out.println("[Server] could not accept connection");
        }
    }

    @Override
    protected void logReceivedMessage(byte[] message) {
        System.out.println("Incoming message: " + new String(message));
    }

    @Override
    protected void logSentMessage(byte[] message) {
        System.out.println("Sent response: " + new String(message));
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
