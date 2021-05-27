package messages.protocol;

// <Version> REMOVED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
public class Deleted extends Message {

    public Deleted(Double version, Integer senderId, String fileId) {
        super(version, senderId, fileId);
    }
    public Deleted(String rcvd){
        super(rcvd);
    }
    @Override
    public String getMsgType() {
        return "DELETED";
    }

    @Override
    protected String getExtraString() {
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
