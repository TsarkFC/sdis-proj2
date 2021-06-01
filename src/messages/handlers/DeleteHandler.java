package messages.handlers;

import filehandler.FileHandler;
import messages.protocol.Delete;
import peer.Peer;
import peer.metadata.FileMetadata;
import utils.AddressPort;
import messages.MessageSender;

public class DeleteHandler {
    public void sendDeleteMessage(Peer peer, String fileId, FileMetadata fileMetadata) {
        Delete msg = new Delete(fileId,true);
        System.out.println("ZOS" + msg.getFileId());
        for (int i = 0; i < fileMetadata.getNumberChunks(); i++) {
            String chunkFileId = FileHandler.createChunkFileId(fileId, i,fileMetadata.getRepDgr());
            System.out.println("ZES" + chunkFileId);
            MessageSender.sendTCPMessageMC(chunkFileId,peer,msg.getBytes());
        }

    }
}
