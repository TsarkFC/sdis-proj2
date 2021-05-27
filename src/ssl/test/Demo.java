package ssl.test;

import ssl.SSlArgs;
import ssl.SSlReceiverTest;
import ssl.SslReceiver;
import ssl.SslSender;

public class Demo {

    SslReceiver server;

    public Demo() {
        server = new SSlReceiverTest("TLSv1.2", "./src/ssl/resources/server.keys", "./src/ssl/resources/truststore","123456");
        server.addServer("localhost",9222);
        new Thread(server).start();
    }

    public void runDemo() {

        System.setProperty("javax.net.debug", "all");

        SslSender client = new SslSender("TLSv1.2", "localhost", 9222);
        client.connect();
        client.write("Hello! I am a client!");
        client.read();
        client.shutdown();

        SslSender client2 = new SslSender("TLSv1.2", "localhost", 9222);
        SslSender client3 = new SslSender("TLSv1.2", "localhost", 9222);
        SslSender client4 = new SslSender("TLSv1.2", "localhost", 9222);

        client2.connect();
        client2.write("Hello! I am another client!");
        client2.read();
        client2.shutdown();

        client3.connect();
        client4.connect();
        client3.write("Hello from client3!!!");
        client4.write("Hello from client4!!!");
        client3.read();
        client4.read();
        client3.shutdown();
        client4.shutdown();

        server.stop();
    }

    public static void main(String[] args) throws Exception {
        Demo demo = new Demo();
        Thread.sleep(1000);	// Give the server some time to start.
        demo.runDemo();
    }

}