package messages.protocol;

import utils.Utils;

// DELETE  <FileID> <CRLF><CRLF>
public class Delete extends Message {

    public Delete(String fileId,boolean isFileID) {
        super(fileId,isFileID);
    }

    @Override
    public String getMsgString() {
        return String.format("%s %s", getMsgType(), this.fileId);

    }

    @Override
    public String getMsgType() {
        return "DELETE";
    }

    @Override
    public int getNumberArguments() {
        return 3;
    }

    @Override
    public byte[] getBytes() {
        return Utils.addCRLF(getMsgString().getBytes());
    }
}
