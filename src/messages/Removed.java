package messages;

// <Version> REMOVED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
public class Removed extends MsgWithChunk {

    public Removed(Double version, Integer senderId, String fileId, Integer chunkNo) {
        super(version, senderId, fileId, chunkNo);
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
        return addCRLF(getMsgString().getBytes());
    }
}
