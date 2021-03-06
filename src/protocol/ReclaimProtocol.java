package protocol;

import chord.ChordNode;
import chord.ChordNodeData;
import filehandler.FileHandler;
import filehandler.FileReader;
import messages.protocol.PutChunk;

import peer.Peer;
import peer.PeerArgs;
import peer.metadata.ChunkMetadata;
import peer.metadata.FileMetadata;
import peer.metadata.StoredChunksMetadata;
import utils.AddressPort;
import messages.MessageSender;

import java.io.File;

public class ReclaimProtocol extends Protocol {
    private final Double maxDiskSpace;

    public ReclaimProtocol(Double maxDiskSpace, Peer peer) {
        super((File) null, peer);
        this.maxDiskSpace = maxDiskSpace;
    }

    @Override
    public void initialize() {
        System.out.println("[RECLAIM] Initializing Reclaim protocol");
        PeerArgs peerArgs = peer.getArgs();
        peer.getMetadata().setMaxSpace(maxDiskSpace);
        double currentStoredSize = FileHandler.getDirectoryKbSize(peer.getFileSystem());
        System.out.println(String.format("[RECLAIM] Peer %d has %f Kb allocated and a max size of %f", peerArgs.getPeerId(), currentStoredSize, maxDiskSpace));
        if (currentStoredSize > maxDiskSpace) {
            reclaimSpace(maxDiskSpace, currentStoredSize);
        }

    }

    public void reclaimSpace(double maxDiskSpace, double currentSize) {
        File[] fileFolders = FileHandler.getDirectoryFiles(peer.getFileSystem());
        if (fileFolders != null) {
            //Eliminate every file until it has size < maxSize
            if (currentSize > maxDiskSpace) {
                System.out.println("[RECLAIM] Eliminating files until desired storage size");
                for (File file : fileFolders) {
                    if (currentSize <= maxDiskSpace) break;
                    currentSize = reclaimFileSpace(file, currentSize);
                }
            }
        } else {
            System.out.println("[RECLAIM] The peer does not have any stored files");
        }
    }

    //Reclaim quando apaga faz backup outra vez do chunk
    private double reclaimFileSpace(File fileId, double currentSize) {
        StoredChunksMetadata storedChunksMetadata = peer.getMetadata().getStoredChunksMetadata();
        String fileIdName = fileId.getName();
        if (!fileIdName.equals("metadata") && !fileIdName.equals("restored")) {
            System.out.println("[RECLAIM] Analysing file: " + fileIdName);
            File[] chunks = FileHandler.getDirectoryFiles(fileId.getPath());
            if (chunks != null) {
                for (File chunkFile : chunks) {
                    ChunkMetadata chunkMetadata = storedChunksMetadata.getChunk(fileId.getName(), Integer.valueOf(chunkFile.getName()));
                    int chunkNo = Integer.parseInt(chunkFile.getName());
                    double size = chunkFile.length() / 1000.0;
                    System.out.println("[RECLAIM] Eliminating chunk: " + chunkFile.getPath() + " size: " + size);

                    AddressPort mcAddr = peer.getArgs().getAddressPortList().getMcAddressPort();
                    FileHandler fileHandler = new FileHandler(chunkFile);
                    PutChunk putChunk = new PutChunk(mcAddr.getAddress(), mcAddr.getPort(), fileIdName,
                            Integer.parseInt(chunkFile.getName()), chunkMetadata.getRepDgr(), 0, fileHandler.getChunkFileData());

                    System.out.println("[RECLAIM] Sent message " + putChunk.getMsgString());
                    MessageSender.sendTCPMessageMDBSuccessor(peer, putChunk.getBytes());

                    if (FileHandler.deleteFile(chunkFile)) {
                        peer.getMetadata().getStoredChunksMetadata().deleteChunk(fileIdName, chunkNo);
                        peer.getMetadata().getStoredChunksMetadata().deleteReceivedChunk(fileIdName, chunkNo);
                        peer.getMetadata().writeMetadata();
                        currentSize -= size;

                        System.out.println("[RECLAIM] Current Size = " + currentSize);
                        if (currentSize <= maxDiskSpace) break;
                    }

                }
            }
        }

        return currentSize;
    }


}


