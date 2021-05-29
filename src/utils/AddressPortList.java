package utils;

import java.io.Serializable;

public class AddressPortList implements Serializable {
    final AddressPort mcAddressPort;
    final AddressPort mdbAddressPort;
    final AddressPort mdrAddressPort;
    final AddressPort chordAddressPort;

    public AddressPortList(AddressPort mcAddressPort, AddressPort mdbAddressPort, AddressPort mdrAddressPort, AddressPort chordAddressPort) {
        this.mcAddressPort = mcAddressPort;
        this.mdbAddressPort = mdbAddressPort;
        this.mdrAddressPort = mdrAddressPort;
        this.chordAddressPort = chordAddressPort;
    }

    public AddressPort getMcAddressPort() { return mcAddressPort; }
    public AddressPort getMdbAddressPort() { return mdbAddressPort; }
    public AddressPort getMdrAddressPort() { return mdrAddressPort; }
    public AddressPort getChordAddressPort() { return chordAddressPort; }

}
