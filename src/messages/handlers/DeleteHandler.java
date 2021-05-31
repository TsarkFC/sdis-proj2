package messages.handlers;

import messages.protocol.Delete;
import peer.Peer;
import utils.AddressPort;
import messages.MessageSender;

public class DeleteHandler {
    public void sendDeleteMessage(Peer peer, String fileId) {
        Delete msg = new Delete(fileId,true);
        MessageSender.sendTCPMessageMC(fileId,peer,msg.getBytes());
    }
}
