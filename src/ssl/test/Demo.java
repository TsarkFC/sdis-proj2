package ssl.test;

import ssl.SSlArgs;
import ssl.SSlReceiverTest;
import ssl.SslReceiver;
import ssl.SslSender;

import java.util.ArrayList;
import java.util.List;

public class Demo {

    SslReceiver server;

    public Demo() {
        server = new SSlReceiverTest("TLSv1.2", "./src/ssl/resources/server.keys", "./src/ssl/resources/truststore","123456");
        server.addServer("localhost",9222);
        new Thread(server).start();
    }

    public void runDemo() {

        /*System.setProperty("javax.net.debug", "all");
        SslSender.setProtocol("TLSv1.2");

        SslSender client = new SslSender("localhost", 9222);
        client.connect();
        client.write(("Hello! I am a client!").getBytes());
        client.read();
        client.shutdown();

        SslSender client2 = new SslSender("localhost", 9222);
        SslSender client3 = new SslSender("localhost", 9222);
        SslSender client4 = new SslSender("localhost", 9222);

        client2.connect();
        client2.write(("Hello! I am another client!").getBytes());
        client2.read();
        client2.shutdown();

        client3.connect();
        client4.connect();
        client3.write(("Hello from client3!!!").getBytes());
        client4.write(("Hello from client4!!!").getBytes());
        client3.read();
        client4.read();
        client3.shutdown();
        client4.shutdown();

        server.stop();*/
    }

    public void runTest(){
        System.setProperty("javax.net.debug", "all");
        SslSender.setProtocol("TLSv1.2");
        List<byte[]> messages = new ArrayList<>();
        messages.add("Hello I am client 1".getBytes());
        messages.add("Pleased to meet you".getBytes());

        SslSender client = new SslSender("localhost", 9222,messages);
        client.connect();
        client.writePeer();
        client.read();
        client.shutdown();

        server.stop();

    }

    public static void main(String[] args) throws Exception {
        Demo demo = new Demo();
        Thread.sleep(1000);	// Give the server some time to start.
        demo.runTest();
    }

}