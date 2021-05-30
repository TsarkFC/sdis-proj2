package messages.protocol;

import utils.Utils;

// <Version> DELETE <IPAddress> <Port> <FileID> <CRLF><CRLF>
public class Delete extends Message {

    public Delete(String ipAddress, Integer port, String fileId) {
        super(ipAddress, port, fileId);
    }

    public Delete(String msg) {
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
        return Utils.addCRLF(getMsgString().getBytes());
    }
}
