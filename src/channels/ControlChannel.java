package channels;

import chord.ChordNode;
import filehandler.FileHandler;
import messages.MessageSender;
import messages.protocol.*;
import peer.Peer;
import ssl.SslReceiver;
import utils.AddressPortList;


public class ControlChannel extends Channel {

    public ControlChannel(AddressPortList addressPortList, Peer peer) {
        super(addressPortList, peer);
        super.currentAddr = addressPortList.getMcAddressPort();

        SslReceiver receiver = new SslReceiver(currentAddr.getAddress(), currentAddr.getPort(), this);
        new Thread(receiver).start();
    }

    @Override
    public byte[] handle(byte[] message) {
        return parseMsg(new String(message));
    }

    public byte[] parseMsg(String msgString) {
        String msgType = Message.getTypeStatic(msgString);
        switch (msgType) {
            case "DELETE" -> handleDelete(msgString);
            case "GETCHUNK" -> handleRestore(msgString);
            default -> System.out.println("\nERROR NOT PARSING THAT MESSAGE " + msgType);
        }
        return null;
    }


    public void handleDelete(String msgString) {
        Delete msg = new Delete(msgString);
        System.out.println("[RECEIVED MESSAGE MC]: " + msgString.substring(0, msgString.length() - 4));

        if(!FileHandler.folderExists(msg.getFileId(), peer)){
            if(shouldResend(msg.getChunkFileId())){
                MessageSender.sendTCPMessageMC(msg.getFileId(),peer, msg.getBytes());
            }else{
                System.out.println("It was not possible to delete the chunk");
            }
        }
        if (FileHandler.deleteFile(msg.getFileId(), peer.getFileSystem())) {
            peer.getMetadata().getHostingMetadata().deleteFile(msg.getFileId());
        }
    }

    public void handleRestore(String msgString) {
        System.out.println("[RECEIVED MESSAGE MC]: " + msgString.substring(0, msgString.length() - 4));
        GetChunk msg = new GetChunk(msgString);
        peer.resetChunksReceived();
        getAndSendChunk(msg, peer);
    }

    private void getAndSendChunk(GetChunk rcvdMsg, Peer peer) {
        byte[] chunk = FileHandler.getChunk(rcvdMsg, peer.getFileSystem());
        if (chunk == null) {
            String chunkFileID = FileHandler.createChunkFileId(rcvdMsg.getFileId(), rcvdMsg.getChunkNo(), rcvdMsg.getRep_dgr());
            if(shouldResend(chunkFileID)){
                MessageSender.sendTCPMessageMCSuccessor(peer, rcvdMsg.getBytes());
            }
            return;
        }

        Chunk msg = new Chunk(rcvdMsg.getFileId(), rcvdMsg.getChunkNo(), chunk);
        byte[] message = msg.getBytes();

        String chunkId = rcvdMsg.getFileId() + "-" + rcvdMsg.getChunkNo();
        if (peer.hasReceivedChunk(chunkId)) return;
        MessageSender.sendTCPMessage(rcvdMsg.getIpAddress(), rcvdMsg.getPort(), message);
    }








}
