package messages.protocol;

//CHUNK  <FileId> <ChunkNo> <CRLF><CRLF><Body>

public class Chunk extends MsgWithChunk {
    private final byte[] body;

    public Chunk(String fileId, Integer chunkNo, byte[] body) {
        super(fileId, chunkNo);
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
    public String getMsgString() {
        return String.format("%s %s %d", getMsgType(), this.fileId, this.chunkNo);

    }


    @Override
    public int getNumberArguments() {
        return 4;
    }

    @Override
    public byte[] getBytes() {
        //<MessageType> <IPAddress> <Port> <FileId> <ChunkNo> <CRLF>
        String header = String.format("%s %s %d", getMsgType(), this.fileId, this.chunkNo);
        return addBody(header.getBytes(), body);
    }

    public byte[] getBody() {
        return body;
    }
}
