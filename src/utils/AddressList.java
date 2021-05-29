package utils;

public class AddressList {
    final AddressPort mcAddr;
    final AddressPort mdbAddr;
    final AddressPort mdrAddr;

    public AddressPort getMcAddr() { return mcAddr; }
    public AddressPort getMdbAddr() { return mdbAddr; }
    public AddressPort getMdrAddr() { return mdrAddr; }

    public AddressList(AddressPort mcAddr, AddressPort mdbAddr, AddressPort mdrAddr) {
        this.mcAddr = mcAddr;
        this.mdbAddr = mdbAddr;
        this.mdrAddr = mdrAddr;
    }
}
