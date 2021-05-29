package utils;
import java.io.Serializable;


public class AddressPort implements Serializable {
    final String address;
    final Integer port;

    public AddressPort(String address, Integer port) {
        this.address = address;
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public Integer getPort() {
        return port;
    }
}
