package messages.handlers;

import messages.protocol.Delete;
import peer.Peer;
import utils.AddressPort;
import utils.ThreadHandler;

public class DeleteHandler {
    public void sendDeleteMessage(Peer peer, String fileId) {
        //TODO AQUI ELE PRECISA DO FILENAME
        //quao rafado e gerar o chord id atraves do id do ficheiro?
        AddressPort addressPortMc = peer.getArgs().getAddressPortList().getChordAddressPort();
        Delete msg = new Delete(addressPortMc.getAddress(), addressPortMc.getPort(), fileId);
        ThreadHandler.sendTCPMessageMC(fileId,peer,msg.getBytes());
    }
}
