package messages.protocol;

import utils.Utils;

// <Version> GETCHUNK <IPAddress> <Port> <FileID> <ChunkNo> <CRLF><CRLF>
public class GetChunk extends MsgWithChunk {

    public GetChunk(String ipAddress, Integer port, String fileId, Integer chunkNo) {
        super(ipAddress, port, fileId, chunkNo);
    }
    public GetChunk(String msg){
        super(msg);
    }

    @Override
    public String getMsgType() {
        return "GETCHUNK";
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
