package ssl;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.TrustManager;
import java.nio.channels.SocketChannel;

public abstract class Ssl {

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
