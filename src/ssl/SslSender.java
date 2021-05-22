package ssl;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class SslSender extends Ssl implements Runnable {

    private SSLEngine engine;
    //SSLContext context;

    private SocketChannel channel;

    private String host;

    private int port;

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

    public void read() {
        try {
            read(channel, engine);
        } catch (IOException e) {
            System.out.println("Error Reading message");
            e.printStackTrace();
        }
    }

    public void write(String message) {
        try {
            write(message, engine, channel);
        } catch (IOException e) {
            System.out.println("Error writing message");
            e.printStackTrace();
        }
    }


    public void shutdown() {

    }

    @Override
    public void receiveMessage(String message) {
        System.out.println("Server response: " + message);
    }

    /*@Override
    public void run() {
        //TODO: send request -> implement when testing is complete
    }*/
}
