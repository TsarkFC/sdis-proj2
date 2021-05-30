package messages.protocol;

import utils.Utils;

// <Version> STARTING <CRLF><CRLF>
public class Starting extends Message {

    public Starting(String ipAddress, Integer port) {
        super(ipAddress, port, "");
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
        return Utils.addCRLF(getMsgString().getBytes());
    }
}
