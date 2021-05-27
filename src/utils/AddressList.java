package utils;

public class AddressList {
    final ChannelAddress mcAddr;
    final ChannelAddress mdbAddr;
    final ChannelAddress mdrAddr;

    public ChannelAddress getMcAddr() { return mcAddr; }
    public ChannelAddress getMdbAddr() { return mdbAddr; }
    public ChannelAddress getMdrAddr() { return mdrAddr; }

    public AddressList(ChannelAddress mcAddr, ChannelAddress mdbAddr, ChannelAddress mdrAddr) {
        this.mcAddr = mcAddr;
        this.mdbAddr = mdbAddr;
        this.mdrAddr = mdrAddr;
    }
}
