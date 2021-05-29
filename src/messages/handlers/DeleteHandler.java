package messages.handlers;

import messages.protocol.Delete;
import messages.protocol.Deleted;
import peer.Peer;
import peer.PeerArgs;
import utils.AddressPortList;
import utils.ThreadHandler;

import java.util.ArrayList;
import java.util.List;

public class DeleteHandler {
    public void sendDeleteMessages(Peer peer, String fileId) {
        PeerArgs peerArgs = peer.getArgs();
        Delete msg = new Delete(peerArgs.getVersion(), peerArgs.getPeerId(), fileId);
        ThreadHandler.sendTCPMessage(peerArgs.getAddressPortList().getMcAddressPort().getAddress(),
                peerArgs.getAddressPortList().getMcAddressPort().getPort(), msg.getBytes());
    }

    public void sendDeletedMessage(Peer peer, Delete deleteMsg) {
        AddressPortList addrList = peer.getArgs().getAddressPortList();
        if (!peer.isVanillaVersion()) {
            Deleted msg = new Deleted(deleteMsg.getVersion(), peer.getArgs().getPeerId(), deleteMsg.getFileId());
            ThreadHandler.sendTCPMessage(addrList.getMcAddressPort().getAddress(), addrList.getMcAddressPort().getPort(), msg.getBytes());
        }
    }
}
