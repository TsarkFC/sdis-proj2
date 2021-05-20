package ssl;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

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

        //context.init(createKeyManagers("./src/main/resources/client.jks", "123456", "123456"), createTrustManagers("./src/main/resources/trustedCerts.jks", "123456"), new SecureRandom());

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

    public void write(String message) throws IOException {
        //TODO This is just pseudocode

        decryptedData.put(message.getBytes());
        decryptedData.flip();

        while (decryptedData.hasRemaining()) {
            // Generate SSL/TLS encoded data (handshake or application data)
            SSLEngineResult res = null;

            res = engine.wrap(decryptedData, encryptedData);

            // Process status of call
            if (res.getStatus() == SSLEngineResult.Status.OK) {
                decryptedData.compact();

                // Send SSL/TLS encoded data to peer
                while (encryptedData.hasRemaining()) {
                    int num = channel.write(encryptedData);
                    if (num == -1) {
                        // handle closed channel
                    } else if (num == 0) {
                        // no bytes written; try again later
                    }
                }
            }

            // Handle other status:  BUFFER_OVERFLOW, CLOSED

        }
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
