package messages.protocol;

// <Version> CHUNK <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>

public class Chunk extends MsgWithChunk {
    private final byte[] body;

    public Chunk(Double version, Integer senderId, String fileId, Integer chunkNo, byte[] body) {
        super(version, senderId, fileId, chunkNo);
        this.body = body;
    }

    public Chunk(String header, byte[] body) {
        super(header);
        this.body = body;
    }

    @Override
    public String getMsgType() {
        return "CHUNK";
    }

    @Override
    protected String getChildString() {
        return "";
    }

    @Override
    public int getNumberArguments() {
        return 6;
    }

    @Override
    public byte[] getBytes() {
        //<Version> <MessageType> <SenderId> <FileId> <ChunkNo> <CRLF>
        String header = String.format("%s %s %d %s %d", this.version, getMsgType(), this.senderId,
                this.fileId, this.chunkNo);
        return addBody(header.getBytes(), body);
    }

    public byte[] getBody() {
        return body;
    }
}
