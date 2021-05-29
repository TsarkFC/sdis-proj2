package messages.protocol;

// <Version> GETCHUNK <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
public class GetChunk extends MsgWithChunk {

    public GetChunk(Double version, Integer senderId, String fileId, Integer chunkNo) {
        super(version, senderId, fileId, chunkNo);
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
        return addCRLF(getMsgString().getBytes());
    }
}
