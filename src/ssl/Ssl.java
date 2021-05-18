package ssl;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.TrustManager;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class Ssl {

    /*Creation - ready to be configured.
    Initial handshaking - perform authentication and negotiate communication parameters.
    Application data - ready for application exchange.
    Rehandshaking - renegotiate communications parameters/authentication; handshaking data may be mixed with application data.
    Closure - ready to shut down connection.*/

    protected ByteBuffer applicationBuffer;

    protected ByteBuffer packetBuffer;

    //Contains the other peer decrypted application data
    protected ByteBuffer peerDecryptedData;

    //Contein other peer encryptedData
    protected ByteBuffer peerEncryptedData;

    protected void handshake(SocketChannel channel, SSLEngine engine) {
        HandshakeStatus status = engine.getHandshakeStatus();

        while (!isFinished(status))
        switch (status) {
            case NEED_TASK:
                break;
            case NEED_UNWRAP:
                break;
            case NEED_UNWRAP_AGAIN:
                break;
            case NEED_WRAP:
                break;
        }
    }

    private boolean isFinished(HandshakeStatus status) {
        return status == HandshakeStatus.FINISHED || status == HandshakeStatus.NOT_HANDSHAKING;
    }

    protected KeyManager[] createKeyManagers(String filepath, String keystorePassword, String keyPassword) {
        //TODO
        return null;
    }

    protected TrustManager[] createTrustManagers(String filepath, String keystorePassword) {
        //TODO
        return null;
    }
}
