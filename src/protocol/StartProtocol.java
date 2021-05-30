package protocol;

import messages.protocol.Starting;
import peer.Peer;
import utils.AddressPort;
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
            AddressPortList addrPortList = peer.getArgs().getAddressPortList();
            //TODO: REMOVE
            Starting msg = new Starting("delete", peer.getArgs().getPeerId());
            ThreadHandler.sendTCPMessage(addrPortList.getMcAddressPort().getAddress(), addrPortList.getMcAddressPort().getPort(), msg.getBytes());
        }
    }
}
