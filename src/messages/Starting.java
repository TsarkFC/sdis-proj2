package messages;

// <Version> STARTING <CRLF><CRLF>
public class Starting extends Message {

    public Starting(Double version, Integer senderId) {
        super(version, senderId, "");
    }
    public Starting(String msg) {
        super(msg);
    }

    @Override
    public String getMsgType() {
        return "STARTING";
    }

    @Override
    protected String getExtraString() {
        return "";
    }

    @Override
    public int getNumberArguments() {
        return 4;
    }

    @Override
    public byte[] getBytes() {
        return addCRLF(getMsgString().getBytes());
    }
}
