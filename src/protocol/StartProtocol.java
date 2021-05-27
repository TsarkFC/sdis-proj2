package protocol;

import messages.protocol.Starting;
import peer.Peer;
import utils.AddressPortList;
import utils.ThreadHandler;

import java.util.ArrayList;
import java.util.List;

public class StartProtocol {
    private final Peer peer;

    public StartProtocol(Peer peer) {
        this.peer = peer;
    }

    public void sendStartingMessage() {
        if (!peer.isVanillaVersion()) {
            AddressPortList addrList = peer.getArgs().getAddressPortList();
            Starting msg = new Starting(peer.getArgs().getVersion(), peer.getArgs().getPeerId());
            List<byte[]> msgs = new ArrayList<>();
            msgs.add(msg.getBytes());
            ThreadHandler.startMulticastThread(addrList.getMcAddressPort().getAddress(), addrList.getMcAddressPort().getPort(), msgs);
        }
    }
}
