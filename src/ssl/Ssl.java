package ssl;

import javax.net.ssl.*;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult;
import java.io.FileInputStream;
import java.io.IOException;
import javax.net.ssl.TrustManager;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.security.SecureRandom;

public abstract class Ssl {

    /*Creation - ready to be configured.
    Initial handshaking - perform authentication and negotiate communication parameters.
    Application data - ready for application exchange.
    Rehandshaking - renegotiate communications parameters/authentication; handshaking data may be mixed with application data.
    Closure - ready to shut down connection.*/

    /**
     * Contains this peer decrypted/original application data
     */
    protected ByteBuffer decryptedData;

    /**
     * Contains this peer encrypted network data
     */
    protected ByteBuffer encryptedData;

    /**
     * Contains the other peer decrypted application data
     */
    protected ByteBuffer peerDecryptedData;

    /**
     * Contains the other peer encrypted network data
     */
    protected ByteBuffer peerEncryptedData;

    protected SSLContext context;

    protected boolean handshake(SocketChannel channel, SSLEngine engine) {
        HandshakeStatus status = engine.getHandshakeStatus();
        SSLEngineResult result;
        startBuffers(engine);

        while (!isFinished(status)) {
            switch (status) {
                case NEED_WRAP:
                    try {
                        encryptedData.clear();
                        result = engine.wrap(decryptedData, encryptedData);
                        if (!handleWrap(result, engine, channel)) {
                            System.out.println("Error during WRAP stage of handshake");
                            return false;
                        }
                    } catch (SSLException e) {
                        e.printStackTrace();
                        return false;
                    }

                    status = engine.getHandshakeStatus();
                    break;

                case NEED_UNWRAP:
                case NEED_UNWRAP_AGAIN:
                    // Receive handshaking data from peer
                    try {
                        System.out.println("Entrei no UNWRAP");

                        if (channel.read(peerEncryptedData) < 0) {
                            System.out.println("Entrei no UNWRAP READ");

                            if (engine.isOutboundDone() && engine.isInboundDone()) {
                                return false;
                            }

                            System.out.println("PASSEI O IF");
                            engine.closeInbound();
                            engine.closeOutbound();
                            status = engine.getHandshakeStatus();
                            System.out.println("FIM DO UNWRAP");
                            break;
                        }
                    } catch (IOException e) {
                        System.out.println("Estou a dar erro aqui 1");
                        e.printStackTrace();
                        return false;
                    }

                    // Process incoming handshaking data
                    try {
                        peerEncryptedData.flip();
                        result = engine.unwrap(peerEncryptedData, peerDecryptedData);
                        peerEncryptedData.compact();

                        if (!handleUnwrap(result, engine)) {
                            System.out.println("Error during UNWRAP stage of handshake");
                            return false;
                        }
                    } catch (SSLException e) {
                        e.printStackTrace();
                        return false;
                    }

                    status = engine.getHandshakeStatus();
                    break;

                case NEED_TASK:
                    break;

                case FINISHED:
                case NOT_HANDSHAKING:
                    break;
            }
        }

        return true;
    }

    private boolean isFinished(HandshakeStatus status) {
        return status == HandshakeStatus.FINISHED || status == HandshakeStatus.NOT_HANDSHAKING;
    }

    private void startBuffers(SSLEngine engine) {
        SSLSession session = engine.getSession();
        peerDecryptedData = ByteBuffer.allocate(session.getApplicationBufferSize());
        peerEncryptedData = ByteBuffer.allocate(session.getPacketBufferSize());
    }

    private boolean handleUnwrap(SSLEngineResult result, SSLEngine engine) {
        switch (result.getStatus()) {
            case OK:
                break;

            case CLOSED:
                engine.closeOutbound();
                break;

            case BUFFER_UNDERFLOW:
                // No data from peer or peerNetBuffer was too small
                if (engine.getSession().getPacketBufferSize() >= peerEncryptedData.limit()) {
                    ByteBuffer newPeerNetBuffer = enlargeBuffer(peerEncryptedData, engine.getSession().getPacketBufferSize());
                    peerEncryptedData.flip();
                    System.out.println("Estou a dar erro aqui");
                    newPeerNetBuffer.put(peerEncryptedData);
                    System.out.println("Aqui ja nao chego");
                    peerEncryptedData = newPeerNetBuffer;
                }
                break;

            case BUFFER_OVERFLOW:
                peerDecryptedData = enlargeBuffer(peerDecryptedData, engine.getSession().getApplicationBufferSize());
                break;
        }

        return true;
    }

    private boolean handleWrap(SSLEngineResult result, SSLEngine engine, SocketChannel channel) {
        switch (result.getStatus()) {
            case OK, CLOSED -> {
                encryptedData.flip();

                // Send the handshaking data to peer
                while (encryptedData.hasRemaining()) {
                    try {
                        channel.write(encryptedData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            case BUFFER_UNDERFLOW -> throw new IllegalStateException("Underflow after wrap occurred!");
            case BUFFER_OVERFLOW -> encryptedData = enlargeBuffer(encryptedData, engine.getSession().getPacketBufferSize());
        }

        return true;
    }

    protected ByteBuffer enlargeBuffer(ByteBuffer buffer, int size) {
        if (size > buffer.capacity()) buffer = ByteBuffer.allocate(size);
        else buffer = buffer.clear();
        return buffer;
    }




    public void initializeSslContext(String protocol, String keyStorePassword, String filePathKeys, String trustStorePath) {
        char[] passphrase = keyStorePassword.toCharArray();

        KeyManagerFactory kmf;
        try {
            kmf = createKeyManagerFactory(passphrase, filePathKeys);
            TrustManagerFactory tmf = createTrustManagerFactory(passphrase, trustStorePath);

            //context = SSLContext.getInstance("TLS");
            context = SSLContext.getInstance(protocol);
            context.init(
                    kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        } catch (Exception e) {
            System.out.println("Error Initializing SslContext");
            e.printStackTrace();
        }
    }

    /*public void initializeSSlContextSimple(String protocol) throws Exception {
        context = SSLContext.getInstance(protocol);
        context.init(createKeyManagers(
                "./src/main/resources/client.jks",
                "123456",
                "123456"),
                createTrustManagers(
                        "./src/main/resources/trustedCerts.jks",
                        "123456"),
                new SecureRandom());
    }*/

    // Determine the maximum buffer sizes for the application and network bytes that could be generated
    public void allocateData(SSLSession session){
        decryptedData = ByteBuffer.allocate(session.getApplicationBufferSize());
        encryptedData = ByteBuffer.allocate(session.getPacketBufferSize());

        peerEncryptedData = ByteBuffer.allocate(session.getApplicationBufferSize());
        peerDecryptedData = ByteBuffer.allocate(session.getPacketBufferSize());
    }

    public KeyManagerFactory createKeyManagerFactory(char[] passphrase, String keysFilePAth) throws Exception {
        // First initialize the key and trust material.
        KeyStore ksKeys = KeyStore.getInstance("JKS");
        ksKeys.load(new FileInputStream(keysFilePAth), passphrase);

        // KeyManager's decide which key material to use.
        KeyManagerFactory kmf =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ksKeys, passphrase);

        return kmf;
    }

    public TrustManagerFactory createTrustManagerFactory(char[] passphrase, String trustStorePath) throws Exception {
        KeyStore trustStoreKey = KeyStore.getInstance("JKS");
        trustStoreKey.load(new FileInputStream(trustStorePath), passphrase);

        // TrustManager's decide whether to allow connections.
        TrustManagerFactory tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStoreKey);

        return tmf;

    }

    public abstract void receiveMessage(String message);

    public void read(SocketChannel socketChannel, SSLEngine engine) throws IOException {
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
            while (peerEncryptedData.hasRemaining()) {
                SSLEngineResult res = engine.unwrap(peerEncryptedData, peerDecryptedData);
                switch (res.getStatus()) {
                    case OK:
                        peerDecryptedData.compact();
                        //Aqui em vez de compact talvez seja flip

                        receiveMessage(new String(decryptedData.array()));
                        //Use peer decrypted data
                        break;
                    // Handle other status:  BUFFER_OVERFLOW, BUFFER_UNDERFLOW, CLOSED
                    case BUFFER_OVERFLOW:
                        peerDecryptedData = enlargeBuffer(peerDecryptedData,engine.getSession().getApplicationBufferSize());
                        break;
                    case BUFFER_UNDERFLOW:
                        System.out.println("Buffer Underflow");
                        break;
                    case CLOSED:
                        System.out.println("Request to close connection");
                        break;
                    default:
                        break;

                }
            }
        }
    }
    public void write(String message,SSLEngine engine,SocketChannel channel) throws IOException {
        //TODO This is just pseudocode
        decryptedData.clear();
        decryptedData.put(message.getBytes());
        decryptedData.flip();

        while (decryptedData.hasRemaining()) {

            encryptedData.clear();
            // Generate SSL/TLS encoded data (handshake or application data)
            SSLEngineResult res = null;

            res = engine.wrap(decryptedData, encryptedData);

            // Process status of call
            switch (res.getStatus()) {
                case OK:

                    decryptedData.compact();

                    // Send SSL/TLS encoded data to peer
                    while (encryptedData.hasRemaining()) {
                        int num = channel.write(encryptedData);
                        if (num == -1) {
                            // handle closed channel
                        } else if (num == 0) {
                            // no bytes written; try again later
                        }else{
                            System.out.println("Success writing  message:");
                            System.out.println("/t" + message);
                        }
                    }
                    break;
                case BUFFER_OVERFLOW:
                    System.out.println("Overflow writing to server");
                    break;
                case BUFFER_UNDERFLOW:
                    System.out.println("Underflow writing to server");

                    break;
                case CLOSED:
                    System.out.println("Close connection");
                    break;
            }

            // Handle other status:  BUFFER_OVERFLOW, CLOSED

        }
    }



}
