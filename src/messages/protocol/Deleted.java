package messages.protocol;

import utils.Utils;

// <Version> REMOVED <IPAddress> <Port> <FileID> <CRLF><CRLF>
public class Deleted extends Message {

    public Deleted(String ipAddress, Integer port, String fileId) {
        super(ipAddress, port, fileId);
    }

    public Deleted(String rcvd) {
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
        return Utils.addCRLF(getMsgString().getBytes());
    }
}
