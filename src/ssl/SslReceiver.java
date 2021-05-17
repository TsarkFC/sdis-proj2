package ssl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SslReceiver extends Ssl implements Runnable {

    private SSLContext context;
    private SSLEngine engine;

    public SslReceiver(String protocol, String host, int port) {
        try {
            context = SSLContext.getInstance(protocol);
            context.init(createKeyManagers("./src/main/resources/client.jks", "123456", "123456"), createTrustManagers("./src/main/resources/trustedCerts.jks", "123456"), new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
            return;
        }
        engine = context.createSSLEngine(host, port);
        engine.setUseClientMode(false);
    }

    @Override
    public void run() {
        //TODO: receive and process requests
    }
}
