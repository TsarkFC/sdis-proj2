package ssl;

public class SSLInformation {
    final String ipAddress = "localhost";
    final String protocol = "TLSv1.2";
    final String serverKeys = "../ssl/resources/server.keys";
    final String clientKeys = "../ssl/resources/client.keys";
    final String trustStore = "../ssl/resources/truststore";
    final String password = "123456";
    final String otherPeerIp = "localhost";
    final int otherPeerPort = 9222;


    public String getIpAddress() {
        return ipAddress;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getServerKeys() {
        return serverKeys;
    }

    public String getClientKeys() {
        return clientKeys;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public String getPassword() {
        return password;
    }

    public String getOtherPeerIp() {
        return otherPeerIp;
    }

    public int getOtherPeerPort() {
        return otherPeerPort;
    }
}
