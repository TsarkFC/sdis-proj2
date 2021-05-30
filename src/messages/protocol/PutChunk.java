package messages.protocol;

// PUTCHUNK <IPAddress> <Port> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
public class PutChunk extends MsgWithChunk {

    final int REP_DGR_IDX = 5;
    final int BODY_IDX = 6;
    private final Integer replicationDeg;
    private final byte[] body;

    public PutChunk(String ipAddress, Integer port, String fileId, Integer chunkNo,
                    Integer replicationDeg, byte[] body) {
        super(ipAddress, port, fileId, chunkNo);
        this.replicationDeg = replicationDeg;
        this.body = body;
    }

    public PutChunk(String header, byte[] body) {
        super(header);
        this.replicationDeg = Integer.parseInt(tokens[REP_DGR_IDX]);
        this.body = body;
    }

    @Override
    public String getMsgType() {
        return "PUTCHUNK";
    }

    @Override
    protected String getChildString() {
        return String.format("%d", this.replicationDeg);
    }

    @Override
    public int getNumberArguments() {
        return 7;
    }

    public void printMsg() {
        super.printMsg();
        System.out.println("Rep dgr: " + this.replicationDeg);
        System.out.println("Body: " + new String(this.body));
    }

    @Override
    public byte[] getBytes() {
        String header = String.format("%s %s %d %s %d %d", getMsgType(), this.ipAddress, this.port,
                this.fileId, this.chunkNo, this.replicationDeg);
        return addBody(header.getBytes(), body);
    }

    public Integer getReplicationDeg() {
        return replicationDeg;
    }

    public byte[] getBody() {
        return body;
    }

}
