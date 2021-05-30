package messages.protocol;

import utils.Utils;

// <Version> STORED <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
public class Stored extends MsgWithChunk {

    public Stored(String ipAddress, Integer port, String fileId, Integer chunkNo) {
        super(ipAddress, port, fileId, chunkNo);
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
        return Utils.addCRLF(getMsgString().getBytes());
    }

    public void printMsg() {
        super.printMsg();
    }
}
