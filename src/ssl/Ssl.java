package ssl;

import javax.net.ssl.*;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Ssl {
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

    /**
     * Context used to create the SSLEngine
     */
    protected SSLContext context;

    /**
     * Executes tasks in NEED_TASK stage of handshake
     */
    private final ExecutorService taskExecutor = Executors.newSingleThreadExecutor();

    protected void initializeSslContext(String protocol, String keyStorePassword, String filePathKeys, String trustStorePath) {
        char[] passphrase = keyStorePassword.toCharArray();

        KeyManagerFactory kmf;
        try {
            kmf = createKeyManagerFactory(passphrase, filePathKeys);
            TrustManagerFactory tmf = createTrustManagerFactory(passphrase, trustStorePath);

            context = SSLContext.getInstance(protocol);
            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        } catch (Exception e) {
            System.out.println("Error Initializing SslContext");
            e.printStackTrace();
        }
    }

    // Determine the maximum buffer sizes for the application and network bytes that could be generated
    protected void allocateData(SSLSession session) {
        decryptedData = ByteBuffer.allocate(session.getApplicationBufferSize());
        encryptedData = ByteBuffer.allocate(session.getPacketBufferSize());

        peerEncryptedData = ByteBuffer.allocate(session.getApplicationBufferSize());
        peerDecryptedData = ByteBuffer.allocate(session.getPacketBufferSize());
    }

    protected boolean handshake(SocketChannel channel, SSLEngine engine) {
        HandshakeStatus status = engine.getHandshakeStatus();
        SSLEngineResult result;

        decryptedData.clear();
        encryptedData.clear();
        peerEncryptedData.clear();
        peerDecryptedData.clear();

        while (!isFinished(status)) {
            switch (status) {
                case NEED_WRAP -> {
                    try {
                        encryptedData.clear();
                        result = engine.wrap(decryptedData, encryptedData);
                        if (!handleWrapResult(result, engine, channel)) {
                            System.out.println("Error during WRAP stage of handshake");
                            return false;
                        }
                    } catch (SSLException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                case NEED_UNWRAP, NEED_UNWRAP_AGAIN -> {
                    // Receive handshaking data from peer
                    try {
                        if (channel.read(peerEncryptedData) < 0) {
                            if (engine.isOutboundDone() && engine.isInboundDone()) {
                                return false;
                            }
                            engine.closeInbound();
                            engine.closeOutbound();
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }

                    // Process incoming handshaking data
                    try {
                        peerEncryptedData.flip();
                        result = engine.unwrap(peerEncryptedData, peerDecryptedData);
                        peerEncryptedData.compact();

                        if (!handleUnwrapResult(result, engine)) {
                            System.out.println("Error during UNWRAP stage of handshake");
                            return false;
                        }
                    } catch (SSLException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                case NEED_TASK -> {
                    Runnable task;
                    while ((task = engine.getDelegatedTask()) != null) {
                        taskExecutor.execute(task);
                    }
                }
                case NOT_HANDSHAKING, FINISHED -> {
                }
            }
            status = engine.getHandshakeStatus();
        }

        return true;
    }

    private boolean handleUnwrapResult(SSLEngineResult result, SSLEngine engine) {
        switch (result.getStatus()) {
            case OK:
                break;

            case CLOSED:
                engine.closeOutbound();
                break;

            case BUFFER_UNDERFLOW:
                // No data from peer or peerNetBuffer was too small
                peerEncryptedData = handleUnderflow(peerEncryptedData, engine.getSession().getPacketBufferSize());
                break;

            case BUFFER_OVERFLOW:
                peerDecryptedData = handleOverflow(peerDecryptedData, engine.getSession().getApplicationBufferSize());
                break;
        }

        return true;
    }

    private boolean handleWrapResult(SSLEngineResult result, SSLEngine engine, SocketChannel channel) {
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
            case BUFFER_OVERFLOW -> encryptedData = handleOverflow(encryptedData, engine.getSession().getPacketBufferSize());
        }

        return true;
    }

    protected abstract void logReceivedMessage(String message);

    protected abstract void logSentMessage(String message);

    protected int read(SocketChannel channel, SSLEngine engine) throws IOException {

        //TODO CRIAR AQUI A SESSION E TAl
        peerEncryptedData.clear();

        // Read SSL/TLS encoded data from peer
        int num = channel.read(peerEncryptedData);
        if (num < 0) {
            engine.closeInbound();
            disconnect(channel, engine);
        } else {
            // Process incoming data
            peerEncryptedData.flip();
            while (peerEncryptedData.hasRemaining()) {
                peerDecryptedData.clear();
                SSLEngineResult res = engine.unwrap(peerEncryptedData, peerDecryptedData);
                switch (res.getStatus()) {
                    case OK -> {
                        peerDecryptedData.flip();
                        logReceivedMessage(new String(peerDecryptedData.array()));
                    }
                    case BUFFER_OVERFLOW -> peerDecryptedData = handleOverflow(peerDecryptedData, engine.getSession().getApplicationBufferSize());
                    case BUFFER_UNDERFLOW -> peerEncryptedData = handleUnderflow(peerEncryptedData, engine.getSession().getPacketBufferSize());
                    case CLOSED -> {
                        disconnect(channel, engine);
                        return 0;
                    }
                }
            }
        }
        return num;
    }

    protected void write(String message, SocketChannel channel, SSLEngine engine) throws IOException {
        //TODO CRIAR AQUI SESSION E TAL
        decryptedData.clear();
        decryptedData.put(message.getBytes());
        decryptedData.flip();

        while (decryptedData.hasRemaining()) {
            encryptedData.clear();
            SSLEngineResult res = engine.wrap(decryptedData, encryptedData);

            // Process status of call
            switch (res.getStatus()) {
                case OK -> {
                    encryptedData.flip();

                    // Send SSL/TLS encoded data to peer
                    while (encryptedData.hasRemaining()) {
                        int num = channel.write(encryptedData);
                        System.out.println("Wrote " + num + " bytes");
                        if (num < 0) {
                            engine.closeInbound();
                            disconnect(channel, engine);
                        } else if (num > 0) {
                            logSentMessage(message);
                        }
                    }
                }
                case BUFFER_OVERFLOW -> encryptedData = handleOverflow(encryptedData, engine.getSession().getPacketBufferSize());
                case BUFFER_UNDERFLOW -> throw new IllegalStateException("Underflow after wrap occurred!");
                case CLOSED -> {
                    disconnect(channel, engine);
                    return;
                }
            }
        }
    }

    protected void disconnect(SocketChannel channel, SSLEngine engine) {
        engine.closeOutbound();
        handshake(channel, engine);
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isFinished(HandshakeStatus status) {
        return status == HandshakeStatus.FINISHED || status == HandshakeStatus.NOT_HANDSHAKING;
    }

    protected ByteBuffer enlargeBuffer(ByteBuffer buffer, int size) {
        if (size <= buffer.capacity()) {
            buffer = ByteBuffer.allocate(buffer.capacity() * 2);
        } else {
            buffer = ByteBuffer.allocate(size);
        }
        return buffer;
    }

    protected ByteBuffer handleOverflow(ByteBuffer buffer, int size) {
        return enlargeBuffer(buffer, size);
    }

    protected ByteBuffer handleUnderflow(ByteBuffer buffer, int size) {
        if (size >= peerEncryptedData.limit()) {
            ByteBuffer newBuffer = enlargeBuffer(peerEncryptedData, size);
            peerEncryptedData.flip();
            newBuffer.put(peerEncryptedData);
            return newBuffer;
        }
        return buffer;
    }

    protected KeyManagerFactory createKeyManagerFactory(char[] passphrase, String keysFilePAth) throws Exception {
        // First initialize the key and trust material.
        KeyStore ksKeys = KeyStore.getInstance("JKS");
        ksKeys.load(new FileInputStream(keysFilePAth), passphrase);

        // KeyManager's decide which key material to use.
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ksKeys, passphrase);

        return kmf;
    }

    protected TrustManagerFactory createTrustManagerFactory(char[] passphrase, String trustStorePath) throws Exception {
        KeyStore trustStoreKey = KeyStore.getInstance("JKS");
        trustStoreKey.load(new FileInputStream(trustStorePath), passphrase);

        // TrustManager's decide whether to allow connections.
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStoreKey);

        return tmf;
    }

}
