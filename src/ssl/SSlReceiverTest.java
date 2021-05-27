package ssl;

public class SSlReceiverTest extends SslReceiver {

    public SSlReceiverTest(String protocol, String serverKeys, String trustStore, String password) {
        super(protocol, serverKeys, trustStore, password);
    }

    @Override
    public void handleMsg(byte[] message) {
        System.out.println("Testing sslReceiver " + new String(message));
    }

}
