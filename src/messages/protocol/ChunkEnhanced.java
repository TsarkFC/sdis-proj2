package messages.protocol;

public class ChunkEnhanced extends MsgWithChunk {
    private final int portNumber;

    public ChunkEnhanced(String ipAddress, Integer port, String fileId, Integer chunkNo, int portNumber) {
        super(ipAddress, port, fileId, chunkNo);
        this.portNumber = portNumber;
    }

    public ChunkEnhanced(String header, byte[] body) {
        super(header);
        this.portNumber = Integer.parseInt(new String(body));
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
        //<MessageType> <IPAddress> <Port> <FileId> <ChunkNo> <PortNumber> <CRLF>
        String header = String.format("%s %s %d %s %d", getMsgType(), this.ipAddress, this.port,
                this.fileId, this.chunkNo);
        return addBody(header.getBytes(), String.valueOf(this.portNumber).getBytes());
    }

    public int getPortNumber() {
        return portNumber;
    }
}
