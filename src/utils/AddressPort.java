package utils;
import peer.Peer;

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

    public boolean samePeerAndSender(Peer peer) {
        AddressPortList addressPortList = peer.getArgs().getAddressPortList();
        String ipAddress = addressPortList.getChordAddressPort().getAddress();

        if (!ipAddress.equals(this.address)) return false;

        return port.equals(addressPortList.getMcAddressPort().getPort()) ||
                port.equals(addressPortList.getMdbAddressPort().getPort()) ||
                port.equals(addressPortList.getMdrAddressPort().getPort()) ||
                port.equals(addressPortList.getChordAddressPort().getPort());
    }
}
