package ssl;

import javax.net.ssl.*;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class Ssl {

    protected ByteBuffer myAppBuffer;

    protected ByteBuffer myNetBuffer;

    protected ByteBuffer peerAppBuffer;

    protected ByteBuffer peerNetBuffer;

    protected boolean handshake(SocketChannel channel, SSLEngine engine) {
        HandshakeStatus status = engine.getHandshakeStatus();
        SSLEngineResult result;

        startBuffers(engine);

        while (!isFinished(status)) {
            switch (status) {
                case NEED_WRAP:
                    try {
                        myNetBuffer.clear();
                        result = engine.wrap(myAppBuffer, myNetBuffer);
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
                        if (channel.read(peerNetBuffer) < 0) {
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
                        peerNetBuffer.flip();
                        result = engine.unwrap(peerNetBuffer, peerAppBuffer);
                        peerNetBuffer.compact();

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
        peerAppBuffer = ByteBuffer.allocate(session.getApplicationBufferSize());
        peerNetBuffer = ByteBuffer.allocate(session.getPacketBufferSize());
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
                if (engine.getSession().getPacketBufferSize() >= peerNetBuffer.limit()) {
                    ByteBuffer newPeerNetBuffer = enlargeBuffer(peerNetBuffer, engine.getSession().getPacketBufferSize());
                    peerNetBuffer.flip();
                    newPeerNetBuffer.put(peerNetBuffer);
                    peerNetBuffer = newPeerNetBuffer;
                }
                break;

            case BUFFER_OVERFLOW:
                peerAppBuffer = enlargeBuffer(peerAppBuffer, engine.getSession().getApplicationBufferSize());
                break;
        }

        return true;
    }

    private boolean handleWrap(SSLEngineResult result, SSLEngine engine, SocketChannel channel) {
        switch (result.getStatus()) {
            case OK, CLOSED -> {
                myNetBuffer.flip();

                // Send the handshaking data to peer
                while (myNetBuffer.hasRemaining()) {
                    try {
                        channel.write(myNetBuffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            case BUFFER_UNDERFLOW -> throw new IllegalStateException("Underflow after wrap occurred!");
            case BUFFER_OVERFLOW -> myNetBuffer = enlargeBuffer(myNetBuffer, engine.getSession().getPacketBufferSize());
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
