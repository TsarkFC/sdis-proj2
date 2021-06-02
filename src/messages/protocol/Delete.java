package messages.protocol;

import utils.AddressPort;
import utils.Utils;

// DELETE  <FileID> <ChunkFileId> <CRLF><CRLF>
public class Delete extends Message {
    private final String chunkFileId;
    private final int CHUNK_FILE_IDX = 2;

    public Delete(String fileId, String chunkFileId) {
        super(fileId, true);
        this.chunkFileId = chunkFileId;
    }

    public Delete(String message) {
        super(message, false);
        this.chunkFileId = tokens[CHUNK_FILE_IDX];

    }

    @Override
    public String getMsgString() {
        return String.format("%s %s %s", getMsgType(), this.fileId, this.chunkFileId);
    }

    @Override
    public String getMsgType() {
        return "DELETE";
    }

    @Override
    public int getNumberArguments() {
        return 4;
    }

    @Override
    public byte[] getBytes() {
        return Utils.addCRLF(getMsgString().getBytes());
    }

    public String getChunkFileId() {
        return chunkFileId;
    }
}
