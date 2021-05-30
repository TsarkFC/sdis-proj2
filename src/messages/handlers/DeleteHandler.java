package messages.handlers;

import messages.protocol.Delete;
import peer.Peer;
import utils.AddressPort;
import utils.ThreadHandler;

public class DeleteHandler {
    public void sendDeleteMessage(Peer peer, String fileId) {
        AddressPort addressPortMc = peer.getArgs().getAddressPortList().getMcAddressPort();
        Delete msg = new Delete(addressPortMc.getAddress(), addressPortMc.getPort(), fileId);
        ThreadHandler.sendTCPMessage(addressPortMc.getAddress(), addressPortMc.getPort(), msg.getBytes());
    }
}
