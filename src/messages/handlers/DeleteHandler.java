package messages.handlers;

import messages.Delete;
import messages.Deleted;
import peer.Peer;
import peer.PeerArgs;
import utils.AddressList;
import utils.ThreadHandler;

import java.util.ArrayList;
import java.util.List;

public class DeleteHandler {
    public void sendDeleteMessages(Peer peer, String fileId) {
        PeerArgs peerArgs = peer.getArgs();
        List<byte[]> messages = new ArrayList<>();
        Delete msg = new Delete(peerArgs.getVersion(), peerArgs.getPeerId(), fileId);
        messages.add(msg.getBytes());
        ThreadHandler.sendTCPMessage(peerArgs.getAddressList().getMcAddr().getAddress(),
                peerArgs.getAddressList().getMcAddr().getPort(), messages);
    }

    public void sendDeletedMessage(Peer peer, Delete deleteMsg) {
        AddressList addrList = peer.getArgs().getAddressList();
        if (!peer.isVanillaVersion()) {
            Deleted msg = new Deleted(deleteMsg.getVersion(), peer.getArgs().getPeerId(), deleteMsg.getFileId());
            List<byte[]> msgs = new ArrayList<>();
            msgs.add(msg.getBytes());
            ThreadHandler.sendTCPMessage(addrList.getMcAddr().getAddress(), addrList.getMcAddr().getPort(), msgs);
        }
    }

}
