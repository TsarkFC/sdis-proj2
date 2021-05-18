package ssl;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SslReceiver extends Ssl {

    private SSLContext context;
    private SSLEngine engine;

    //It can be made more robust and scalable by using a Selector with the non-blocking SocketChannel
    private Selector selector;


    public SslReceiver(String protocol, String host, int port) {
        //Falta adicionar a outra password
        initializeSslContext(protocol,"123456","./src/ssl/resources/client.keys","./src/ssl/resources/truststore");

        engine = context.createSSLEngine(host, port);
        engine.setUseClientMode(false);

        SSLSession session = engine.getSession();
        decryptedData = ByteBuffer.allocate(session.getApplicationBufferSize());
        encryptedData = ByteBuffer.allocate(session.getPacketBufferSize());

        peerEncryptedData = ByteBuffer.allocate(session.getApplicationBufferSize());
        peerDecryptedData = ByteBuffer.allocate(session.getPacketBufferSize());

        session.invalidate();

        this.createServerSocketChannel(host,port);


    }



    public KeyManagerFactory createKeyManagerFactory(char[] passphrase,String keysFilePAth) throws  Exception{
        // First initialize the key and trust material.
        KeyStore ksKeys = KeyStore.getInstance("JKS");
        ksKeys.load(new FileInputStream(keysFilePAth), passphrase);

        // KeyManager's decide which key material to use.
        KeyManagerFactory kmf =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ksKeys, passphrase);

        return kmf;
    }

    public TrustManagerFactory createTrustManagerFactory(char[] passphrase, String trustStorePath) throws Exception{
        KeyStore trustStoreKey = KeyStore.getInstance("JKS");
        trustStoreKey.load(new FileInputStream(trustStorePath), passphrase);

        // TrustManager's decide whether to allow connections.
        TrustManagerFactory tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStoreKey);

        return tmf;

    }

    public void initializeSslContext(String protocol,String keyStorePassword,String filePathKeys,String trustStorePath){
        char[] passphrase = keyStorePassword.toCharArray();

        KeyManagerFactory kmf;
        try {
            kmf = createKeyManagerFactory(passphrase,filePathKeys);
            TrustManagerFactory tmf = createTrustManagerFactory(passphrase,trustStorePath);

            //context = SSLContext.getInstance("TLS");
            context = SSLContext.getInstance(protocol);
            context.init(
                    kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        } catch (Exception e) {
            System.out.println("Error Initializing SslContext");
            e.printStackTrace();
        }

    }

    public void initializeSSlContextSimple(String protocol) throws Exception{
        context = SSLContext.getInstance(protocol);
        context.init(createKeyManagers(
                "./src/main/resources/client.jks",
                "123456",
                "123456"),
                createTrustManagers(
                        "./src/main/resources/trustedCerts.jks",
                        "123456"),
                new SecureRandom());
    }

    public void createServerSocketChannel(String host, int port) {
        try {
            selector = SelectorProvider.provider().openSelector();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(host, port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Connection completed Successfully");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Starts listening to new connections
    //Run in a loop as long the server is active
    public void start() {

    }

    public void stop() {

    }

    public void accept() {

    }

    public void read() {

    }

    public void write() {

    }

    /*@Override
    public void run() {
        //TODO: receive and process requests -> implement when testing is complete
    }*/
}
