package messages.protocol;

import utils.Utils;

// STORED <FileId> <ChunkNo> <CRLF><CRLF>
public class Stored extends MsgWithChunk {

    public Stored(String fileId, Integer chunkNo) {
        super(fileId, chunkNo);
    }

    public Stored(String message) {
        super(message);
    }

    @Override
    public String getMsgType() {
        return "STORED";
    }

    @Override
    public String getMsgString() {
        return String.format("%s %s %d", getMsgType(), this.fileId, this.chunkNo);
    }

    @Override
    public int getNumberArguments() {
        return 4;
    }

    @Override
    public byte[] getBytes() {
        return Utils.addCRLF(getMsgString().getBytes());
    }

    public void printMsg() {
        super.printMsg();
    }
}
