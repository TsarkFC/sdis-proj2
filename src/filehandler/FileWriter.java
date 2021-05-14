package filehandler;

import messages.PutChunk;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static filehandler.FileHandler.CHUNK_SIZE;
import static filehandler.FileHandler.getFilePath;

public class FileWriter {

    public static void saveChunk(PutChunk message, String peerDir) {
        // create directory if it does not exist
        String dirPath = getFilePath(peerDir, message);
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdir();

        Path chunkPath = Paths.get(dirPath + message.getChunkNo());
        byte[] chunkBody = message.getBody();
        ByteBuffer buffer = ByteBuffer.allocate(chunkBody.length);
        buffer.put(chunkBody);
        buffer.rewind();

        AsynchronousFileChannel writeFileChannel;
        try {
            writeFileChannel = AsynchronousFileChannel.open(chunkPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        writeFileChannel.write(buffer, 0);
    }

    public static void restoreFile(String path, ConcurrentHashMap<Integer, byte[]> content) {
        Path filePath = Paths.get(path);
        AsynchronousFileChannel writeFileChannel;
        try {
            writeFileChannel = AsynchronousFileChannel.open(filePath, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        List<Integer> keys = new ArrayList<>(content.keySet());
        Collections.sort(keys);

        for (Integer key : keys) {
            byte[] chunk = content.get(key);
            ByteBuffer buffer = ByteBuffer.allocate(chunk.length);
            buffer.put(chunk);
            buffer.rewind();
            writeFileChannel.write(buffer, key * CHUNK_SIZE);
        }
    }
}
