package filehandler;

import messages.GetChunk;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static filehandler.FileHandler.CHUNK_SIZE;

public class FileReader {
    private final File file;
    private final int numOfChunks;

    public FileReader(File file, int numOfChunks) {
        this.file = file;
        this.numOfChunks = numOfChunks;
    }

    public ConcurrentHashMap<Integer, byte[]> getFileChunks() {
        ConcurrentHashMap<Integer, byte[]> chunks = new ConcurrentHashMap<>();

        for (int chunkNo = 0; chunkNo < numOfChunks; chunkNo++) {
            asyncChunkRead(chunks, chunkNo);
        }
        return chunks;
    }

    private void asyncChunkRead(ConcurrentHashMap<Integer, byte[]> chunks, int chunkNo) {
        ByteBuffer buffer = ByteBuffer.allocate(CHUNK_SIZE);
        AsynchronousFileChannel readFileChannel;
        try {
            readFileChannel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        readFileChannel.read(buffer, (long) chunkNo * CHUNK_SIZE, buffer, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, ByteBuffer bufferRead) {
                buffer.rewind();
                byte[] chunk = new byte[result];
                buffer.get(chunk);
                chunks.put(chunkNo, chunk);
                bufferRead.clear();
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                System.out.println("Read operation failed: " + exc.getMessage());

            }
        });
    }

    public static byte[] getChunk(GetChunk message, String peerDir) {
        String chunkPath = FileHandler.getFilePath(peerDir, message) + message.getChunkNo();
        Path path = Paths.get(chunkPath);
        if (!Files.exists(path)) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.allocate(CHUNK_SIZE);
        AsynchronousFileChannel readFileChannel;
        try {
            readFileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Future<Integer> result = readFileChannel.read(buffer, 0);
        //while(!result.isDone());

        int chunkSize;
        try {
            chunkSize = result.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }

        byte[] chunk = new byte[chunkSize];
        buffer.rewind();
        buffer.get(chunk);
        buffer.clear();
        return chunk;
    }
}
