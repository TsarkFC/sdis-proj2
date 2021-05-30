package messages.protocol;

import utils.Utils;

// REMOVED <IPAddress> <Port> <FileId> <ChunkNo> <CRLF><CRLF>
public class Removed extends MsgWithChunk {

    public Removed(String ipAddress, Integer port, String fileId, Integer chunkNo) {
        super(ipAddress, port, fileId, chunkNo);
    }
    public Removed(String rcvd){
        super(rcvd);
    }
    @Override
    public String getMsgType() {
        return "REMOVED";
    }

    @Override
    protected String getChildString() {
        return "";
    }

    @Override
    public int getNumberArguments() {
        return 6;
    }

    @Override
    public byte[] getBytes() {
        return Utils.addCRLF(getMsgString().getBytes());
    }
}
