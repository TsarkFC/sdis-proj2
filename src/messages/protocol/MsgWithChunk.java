package messages.protocol;

// STORED <IPAddress> <Port> <FileId> <ChunkNo> <CRLF><CRLF>
public abstract class MsgWithChunk extends Message {

    final int CHUNK_NO_IDX = 4;
    protected final Integer chunkNo;

    public MsgWithChunk(String ipAddress, Integer port, String fileId, Integer chunkNo) {
        super(ipAddress, port, fileId);
        this.chunkNo = chunkNo;
    }

    public MsgWithChunk(String message) {
        super(message);
        this.chunkNo = Integer.parseInt(tokens[CHUNK_NO_IDX]);
    }

    @Override
    public abstract String getMsgType();

    @Override
    protected String getExtraString() {
        return String.format("%d %s", this.chunkNo, getChildString());
    }

    protected abstract String getChildString();

    @Override
    public abstract int getNumberArguments();

    @Override
    public abstract byte[] getBytes();

    public void printMsg() {
        super.printMsg();
        System.out.println("Chunk No: " + this.chunkNo);
    }

    public Integer getChunkNo() {
        return chunkNo;
    }
}
