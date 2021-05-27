package ssl;

public class SSlArgs {
    public String getProtocol() {
        return protocol;
    }

    public String getKeys() {
        return keys;
    }

    public String getTruststore() {
        return truststore;
    }

    public String getPassword() {
        return password;
    }


    public final String protocol;
    public final String keys;
    public final String truststore;
    public final String password;

    public SSlArgs(String protocol, String keys, String truststore, String password) {
        this.protocol = protocol;
        this.keys = keys;
        this.truststore = truststore;
        this.password = password;
    }
}
