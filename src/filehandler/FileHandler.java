package filehandler;

import messages.protocol.GetChunk;
import messages.protocol.Message;
import messages.protocol.PutChunk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class FileHandler {
    private final File file;
    public static final int CHUNK_SIZE = 15000;
    private final FileReader fileReader;

    public FileHandler(File file) {
        this.file = file;
        fileReader = new FileReader(file, getNumberOfChunks((int)file.length()));
    }

    public static File getFile(String path) {
        if (Files.exists(Paths.get(path))) {
            File file = new File(path);
            if (file.exists() && file.canRead()) return file;
        }
        System.out.println("[ERROR] " + path + " does not exist!");
        return null;
    }

    public String createFileId() {
        //Is not thread safe
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String id = file.getPath() + file.lastModified() + file.length();
        byte[] encodedHash = digest.digest(id.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte c : encodedHash) {
            sb.append(String.format("%02X", c));
        }
        return sb.toString();
    }

    public static String createChunkFileId(String fileId,int chunkNo){
        return fileId+chunkNo;
    }

    public static String getFilePath(String peerDir, Message message) {
        return peerDir.concat("/" + message.getFileId() + "/");
    }

    public static String getFilePath(String peerDir, String fileId) {
        return peerDir.concat("/" + fileId + "/");
    }

    public static String getChunkPath(String peerDir, String fileId, int chunkNo) {
        return peerDir.concat("/" + fileId + "/" + chunkNo);
    }

    public static boolean deleteFile(String fileId, String peerDir) {
        String dirPath = getFilePath(peerDir, fileId);
        File folder = new File(dirPath);
        if (!folder.exists()) System.out.println("[DELETE] Tried to delete directory that does not exist");
        else {
            if (FileHandler.deleteDirectory(folder)) {
                System.out.println("[DELETE] Deleted directory");
                return true;
            } else {
                System.out.println("[DELETE] Error deleting directory");
            }
        }
        return false;
    }

    public static boolean deleteFile(File myObj) {
        if (myObj.delete()) {
            System.out.println("[DELETE] Deleted the file: " + myObj.getName());
            return true;
        } else {
            System.out.println("[DELETE] Failed to delete the file.");
            return false;
        }
    }

    public static File[] getDirectoryFiles(String peerDir) {
        File folder = new File(peerDir);
        if (!folder.exists()) {
            System.out.println("[ERROR] Peer folder does not exist");
            return null;
        }
        return folder.listFiles();
    }

    public static double getDirectoryKbSize(String dirPath) {
        return FileHandler.getDirectorySize(dirPath) / 1000.0;
    }

    private static double getDirectorySize(String dirPath) {
        File folder = new File(dirPath);
        float length = 0;
        File[] files = folder.listFiles();

        for (File value : files) {
            if (value.isFile()) {
                length += value.length();
            } else {
                length += getDirectorySize(value.getPath());
            }
        }
        return length;
    }

    private static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                FileHandler.deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public static int getNumberOfChunks(int size) {
        if (size % CHUNK_SIZE == 0)
            return size / CHUNK_SIZE;
        return size / CHUNK_SIZE + 1;
    }

    public ConcurrentHashMap<Integer, byte[]> getFileChunks() {
        return fileReader.getFileChunks();
    }

    public static byte[] getChunk(GetChunk message, String peerDir) {
        return FileReader.getChunk(message, peerDir);
    }

    public static void saveChunk(PutChunk message, String peerDir) {
        FileWriter.saveChunk(message, peerDir);
    }

    public static void restoreFile(String path, ConcurrentHashMap<Integer, byte[]> content) {
        FileWriter.restoreFile(path, content);
    }

    public byte[] getChunkFileData() {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            byte[] chunk = new byte[CHUNK_SIZE];
            int read = inputStream.read(chunk);
            return Arrays.copyOf(chunk, read);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
