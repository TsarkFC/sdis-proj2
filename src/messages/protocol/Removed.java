package messages.protocol;

import utils.Utils;

// REMOVED <FileId> <ChunkNo> <repDgr> <CRLF><CRLF>
public class Removed extends MsgWithChunk {

    private final Integer replicationDeg;
    final int REP_DGR_IDX = 3;

    public Removed(String fileId, Integer chunkNo,Integer replicationDeg) {
        super( fileId, chunkNo);
        this.replicationDeg = replicationDeg;
    }
    public Removed(String rcvd){
        super(rcvd);
        this.replicationDeg = Integer.parseInt(tokens[REP_DGR_IDX]);

    }
    @Override
    public String getMsgType() {
        return "REMOVED";
    }

    @Override
    public String getMsgString() {
        return String.format("%s %s %d %d", getMsgType(), this.fileId, this.chunkNo,this.replicationDeg);
    }

    public Integer getReplicationDeg() {
        return replicationDeg;
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
