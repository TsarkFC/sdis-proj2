package ssl;

import utils.Utils;

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
     * Context used to create the SSLEngine
     */
    protected SSLContext context;

    /**
     * Executes tasks in NEED_TASK stage of handshake
     */
    private final ExecutorService taskExecutor = Executors.newSingleThreadExecutor();

    /**
     *
     */
    private static final int MESSAGE_SIZE = 64500;

    protected void initializeSslContext(String protocol, String filePathKeys) {
        char[] passphrase = SSLInformation.password.toCharArray();

        KeyManagerFactory kmf;
        try {
            kmf = createKeyManagerFactory(passphrase, filePathKeys);
            TrustManagerFactory tmf = createTrustManagerFactory(passphrase, SSLInformation.trustStore);

            context = SSLContext.getInstance(protocol);
            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        } catch (Exception e) {
            System.out.println("Error Initializing SslContext");
            e.printStackTrace();
        }
    }

    protected boolean handshake(SocketChannel channel, SSLEngine engine) {
        HandshakeStatus status = engine.getHandshakeStatus();
        SSLEngineResult result;
        final int additionalSpace = 50;
        int appBufferSize = engine.getSession().getApplicationBufferSize();
        int finalBufferSize = appBufferSize + additionalSpace;
        ByteBuffers byteBuffers = new ByteBuffers(finalBufferSize);

        while (!isFinished(status)) {
            switch (status) {
                case NEED_WRAP -> {
                    try {
                        byteBuffers.getEncryptedData().clear();
                        result = engine.wrap(byteBuffers.getDecryptedData(), byteBuffers.getEncryptedData());
                        if (!handleWrapResult(result, engine, channel, byteBuffers)) {
                            System.out.println("[Handshake] Error during wrap");
                            return false;
                        }
                    } catch (SSLException e) {
                        System.out.println("[Handshake] Error during wrap");
                        return false;
                    }
                }
                case NEED_UNWRAP, NEED_UNWRAP_AGAIN -> {
                    // Receive handshaking data from peer
                    try {
                        if (channel.read(byteBuffers.getPeerEncryptedData()) < 0) {
                            if (engine.isOutboundDone() && engine.isInboundDone()) {
                                return false;
                            }
                            engine.closeInbound();
                            engine.closeOutbound();
                            break;
                        }
                    } catch (Exception e) {
                        System.out.println("[Handshake] Error during unwrap");
                        return false;
                    }

                    // Process incoming handshaking data
                    try {
                        byteBuffers.getPeerEncryptedData().flip();
                        result = engine.unwrap(byteBuffers.getPeerEncryptedData(), byteBuffers.getPeerDecryptedData());
                        byteBuffers.getPeerEncryptedData().compact();

                        if (!handleUnwrapResult(result, engine, byteBuffers)) {
                            System.out.println("[Handshake] Error during unwrap");
                            return false;
                        }
                    } catch (Exception e) {
                        System.out.println("[Handshake] Error during unwrap");
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

    private boolean handleUnwrapResult(SSLEngineResult result, SSLEngine engine, ByteBuffers byteBuffers) {
        switch (result.getStatus()) {
            case OK:
                break;
            case CLOSED:
                engine.closeOutbound();
                break;
            case BUFFER_UNDERFLOW:
                // No data from peer or peerNetBuffer was too small
                byteBuffers.setPeerEncryptedData(handleUnderflow(byteBuffers.getPeerEncryptedData(), engine.getSession().getPacketBufferSize()));
                break;
            case BUFFER_OVERFLOW:
                byteBuffers.setPeerDecryptedData(handleOverflow(byteBuffers.getPeerDecryptedData(), engine.getSession().getApplicationBufferSize()));
                break;
        }

        return true;
    }

    private boolean handleWrapResult(SSLEngineResult result, SSLEngine engine, SocketChannel channel, ByteBuffers byteBuffers) {
        switch (result.getStatus()) {
            case OK, CLOSED -> {
                byteBuffers.getEncryptedData().flip();

                // Send the handshaking data to peer
                while (byteBuffers.getEncryptedData().hasRemaining()) {
                    try {
                        channel.write(byteBuffers.getEncryptedData());
                    } catch (Exception e) {
                        return false;
                    }
                }
            }
            case BUFFER_UNDERFLOW -> throw new IllegalStateException("Underflow after wrap occurred!");
            case BUFFER_OVERFLOW -> byteBuffers.setEncryptedData(handleOverflow(byteBuffers.getEncryptedData(), engine.getSession().getPacketBufferSize()));
        }

        return true;
    }

    protected abstract void logReceivedMessage(byte[] message);

    protected abstract void logSentMessage(byte[] message);

    protected byte[] read(SocketChannel channel, SSLEngine engine) {

        SSLSession session = engine.getSession();
        int peerDecryptedSize = Math.max(session.getApplicationBufferSize(), MESSAGE_SIZE) + 500;
        int peerEncryptedSize = Math.max(session.getPacketBufferSize(), MESSAGE_SIZE) + 500;
        ByteBuffers byteBuffers = new ByteBuffers(peerEncryptedSize, peerDecryptedSize, true);
        byteBuffers.getPeerEncryptedData().clear();
        byte[] readResult = null;

        // Read SSL/TLS encoded data from peer
        try {
            int num = channel.read(byteBuffers.getPeerEncryptedData());
            if (num < 0) {
                engine.closeInbound();
                disconnect(channel, engine);
            } else {
                // Process incoming data
                byteBuffers.getPeerEncryptedData().flip();
                while (byteBuffers.getPeerEncryptedData().hasRemaining()) {
                    byteBuffers.getPeerDecryptedData().clear();
                    SSLEngineResult res = engine.unwrap(byteBuffers.getPeerEncryptedData(), byteBuffers.getPeerDecryptedData());
                    switch (res.getStatus()) {
                        case OK -> {
                            byteBuffers.getPeerDecryptedData().flip();
                            byte[] msg = byteBuffers.getPeerDecryptedData().array();
                            readResult = Utils.concatBuffer(readResult, msg);
                        }
                        case BUFFER_OVERFLOW -> byteBuffers.setPeerDecryptedData(handleOverflow(byteBuffers.getPeerDecryptedData(), engine.getSession().getApplicationBufferSize()));
                        case BUFFER_UNDERFLOW -> byteBuffers.setPeerEncryptedData(handleUnderflow(byteBuffers.getPeerEncryptedData(), engine.getSession().getPacketBufferSize()));
                        case CLOSED -> {
                            disconnect(channel, engine);
                            return null;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Could not read!!");
            disconnect(channel, engine);
        }
        return readResult;
    }

    protected void write(byte[] message, SocketChannel channel, SSLEngine engine) {

        SSLSession session = engine.getSession();
        int encryptedBufferSize = session.getPacketBufferSize();
        int decryptedBufferSize = Math.max(session.getApplicationBufferSize(), message.length);
        ByteBuffers byteBuffers = new ByteBuffers(encryptedBufferSize, decryptedBufferSize, false);

        byteBuffers.getDecryptedData().clear();
        byteBuffers.getDecryptedData().put(message);
        byteBuffers.getDecryptedData().flip();

        try {
            while (byteBuffers.getDecryptedData().hasRemaining()) {
                byteBuffers.getEncryptedData().clear();
                SSLEngineResult res = engine.wrap(byteBuffers.getDecryptedData(), byteBuffers.getEncryptedData());

                // Process status of call
                switch (res.getStatus()) {
                    case OK -> {
                        byteBuffers.getEncryptedData().flip();

                        // Send SSL/TLS encoded data to peer
                        while (byteBuffers.getEncryptedData().hasRemaining()) {
                            int num = channel.write(byteBuffers.getEncryptedData());
                            //System.out.println("Wrote " + num + " bytes");
                            if (num < 0) {
                                engine.closeInbound();
                                disconnect(channel, engine);
                            } else if (num > 0) {
                                //logSentMessage(new String(message));
                            }
                        }
                    }
                    case BUFFER_OVERFLOW -> byteBuffers.setEncryptedData(handleOverflow(byteBuffers.getEncryptedData(), engine.getSession().getPacketBufferSize()));
                    case BUFFER_UNDERFLOW -> throw new IllegalStateException("Underflow after wrap occurred!");
                    case CLOSED -> {
                        disconnect(channel, engine);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Could not write!!");
            disconnect(channel, engine);
        }
    }

    protected void disconnect(SocketChannel channel, SSLEngine engine) {
        try {
            engine.closeOutbound();
            handshake(channel, engine);
            channel.close();
        } catch (Exception e) {
            System.out.println("[Disconnect] Error!");
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
        if (size >= buffer.limit()) {
            ByteBuffer newBuffer = enlargeBuffer(buffer, size);
            buffer.flip();
            newBuffer.put(buffer);
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
