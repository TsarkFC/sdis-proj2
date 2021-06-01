package messages.protocol;

// STORED <IPAddress> <Port> <FileId> <ChunkNo> <CRLF><CRLF>
public abstract class MsgWithChunk extends Message {

    final int CHUNK_NO_IDX = 2;
    protected final Integer chunkNo;

    public MsgWithChunk(String fileId, Integer chunkNo) {
        super(fileId,true);
        this.chunkNo = chunkNo;
    }

    public MsgWithChunk(String message) {
        super(message,false);
        this.chunkNo = Integer.parseInt(tokens[CHUNK_NO_IDX]);
    }

    @Override
    public abstract String getMsgType();

    @Override
    public abstract String getMsgString();


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
