package messages.protocol;

import utils.Utils;

// REMOVED <FileId> <ChunkNo> <CRLF><CRLF>
public class Removed extends MsgWithChunk {

    public Removed(String fileId, Integer chunkNo) {
        super( fileId, chunkNo);
    }
    public Removed(String rcvd){
        super(rcvd);
    }
    @Override
    public String getMsgType() {
        return "REMOVED";
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
}
