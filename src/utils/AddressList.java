package utils;

public class AddressList {
    final MulticastAddress mcAddr;
    final MulticastAddress mdbAddr;
    final MulticastAddress mdrAddr;

    public MulticastAddress getMcAddr() { return mcAddr; }
    public MulticastAddress getMdbAddr() { return mdbAddr; }
    public MulticastAddress getMdrAddr() { return mdrAddr; }

    public AddressList(MulticastAddress mcAddr, MulticastAddress mdbAddr, MulticastAddress mdrAddr) {
        this.mcAddr = mcAddr;
        this.mdbAddr = mdbAddr;
        this.mdrAddr = mdrAddr;
    }
}
