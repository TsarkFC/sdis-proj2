package messages;

// <Version> DELETE <SenderId> <FileId> <CRLF><CRLF>
public class Delete extends Message {

    public Delete(Double version, Integer senderId, String fileId) {
        super(version,senderId,fileId);
    }
    public Delete(String msg){
        super(msg);
    }

    @Override
    public String getMsgType() {
        return "DELETE";
    }

    @Override
    protected String getExtraString() {
        return "";
    }

    @Override
    public int getNumberArguments() {
        return 5;
    }

    @Override
    public byte[] getBytes() {
        return addCRLF(getMsgString().getBytes());
    }
}
