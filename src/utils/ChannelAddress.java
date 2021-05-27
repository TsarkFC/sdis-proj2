package utils;

public class ChannelAddress {
    public String getAddress() {
        return address;
    }

    public Integer getPort() {
        return port;
    }

    final String address;
    final Integer port;

    public ChannelAddress(String address, Integer port) {
        this.address = address;
        this.port = port;
    }
}
