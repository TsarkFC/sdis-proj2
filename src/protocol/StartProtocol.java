package protocol;

import messages.Starting;
import peer.Peer;
import utils.AddressList;
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
            AddressList addrList = peer.getArgs().getAddressList();
            Starting msg = new Starting(peer.getArgs().getVersion(), peer.getArgs().getPeerId());
            List<byte[]> msgs = new ArrayList<>();
            msgs.add(msg.getBytes());
            ThreadHandler.startMulticastThread(addrList.getMcAddr().getAddress(), addrList.getMcAddr().getPort(), msgs);
        }
    }
}
