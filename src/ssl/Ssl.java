package ssl;

import javax.net.ssl.*;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult;
import java.io.IOException;
import javax.net.ssl.TrustManager;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

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
                        if (channel.read(peerEncryptedData) < 0) {
                            engine.closeInbound();
                            engine.closeOutbound();
                            status = engine.getHandshakeStatus();
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
                    newPeerNetBuffer.put(peerEncryptedData);
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

    private ByteBuffer enlargeBuffer(ByteBuffer buffer, int size) {
        if (size > buffer.capacity()) buffer = ByteBuffer.allocate(size);
        else buffer = buffer.clear();
        return buffer;
    }

    protected KeyManager[] createKeyManagers(String filepath, String keystorePassword, String keyPassword) {
        //TODO: check documentation code
        return null;
    }

    protected TrustManager[] createTrustManagers(String filepath, String keystorePassword) {
        //TODO: check documentation code
        return null;
    }
}
