package channels;

import peer.Peer;
import utils.AddressPortList;

public class ChannelCoordinator {
    private final Peer peer;

    public ChannelCoordinator(Peer peer) {
        this.peer = peer;
        AddressPortList addressPortList = peer.getArgs().getAddressPortList();
        new BackupChannel(addressPortList, peer);
        new ControlChannel(addressPortList, peer);
        new RestoreChannel(addressPortList, peer);
        new ChordChannel(addressPortList, peer);
    }
}
