package messages;

// <Version> STORED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
public class Stored extends MsgWithChunk {

    public Stored(Double version, Integer senderId, String fileId, Integer chunkNo) {
        super(version,senderId,fileId,chunkNo);
    }

    public Stored(String message) {
        super(message);
    }

    @Override
    public String getMsgType() {
        return "STORED";
    }

    @Override
    protected String getChildString() {
        //Version> STORED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
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

    public void printMsg() {
        super.printMsg();
    }
}
