package protocol;

import filehandler.FileHandler;
import messages.Removed;
import peer.Peer;
import peer.PeerArgs;
import peer.metadata.ChunkMetadata;
import peer.metadata.StoredChunksMetadata;
import utils.ThreadHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
        double currentStoredSize =  FileHandler.getDirectoryKbSize(peer.getFileSystem());
        System.out.println(String.format("[RECLAIM] Peer %d has %f Kb allocated and a max size of %f",peerArgs.getPeerId(),currentStoredSize,maxDiskSpace));
        if(currentStoredSize > maxDiskSpace){
            reclaimSpace(maxDiskSpace,currentStoredSize);
        }

    }

    public void reclaimSpace(double maxDiskSpace, double currentSize){
        List<byte[]> messages = new ArrayList<>();
        File[] fileFolders = FileHandler.getDirectoryFiles(peer.getFileSystem());
        if (fileFolders != null) {

            System.out.println("[RECLAIM] Eliminating only chunks with Perceived Rep degree > Rep degree");
            for (File file : fileFolders) {
                if (currentSize <= maxDiskSpace) break;
                currentSize = reclaimFileSpace(file,currentSize,messages,true);
            }
            fileFolders = FileHandler.getDirectoryFiles(peer.getFileSystem());

            //Eliminate every file until it has size < maxSize
            if(currentSize > maxDiskSpace){
                System.out.println("[RECLAIM] Eliminating the ones with bigger rep degree than desired was not enough...");
                System.out.println("[RECLAIM] Eliminating other files");
                for (File file : fileFolders) {
                    if (currentSize <= maxDiskSpace) break;
                    currentSize = reclaimFileSpace(file,currentSize,messages,false);
                }
            }
            PeerArgs peerArgs = peer.getArgs();
            ThreadHandler.sendTCPMessage(peerArgs.getAddressList().getMcAddr().getAddress(),
                    peerArgs.getAddressList().getMcAddr().getPort(), messages);
        }else{
            System.out.println("[RECLAIM] The peer does not have any stored files");
        }
    }



    private double reclaimFileSpace(File fileId,double currentSize,List<byte[]> messages, boolean onlyBiggerPercDgr){
        StoredChunksMetadata storedChunksMetadata = peer.getMetadata().getStoredChunksMetadata();
        String fileName = fileId.getName();
        if(!fileName.equals("metadata") && !fileName.equals("restored")){
            System.out.println("[RECLAIM] Analysing file: " + fileName);
            File[] chunks = FileHandler.getDirectoryFiles(fileId.getPath());
            if (chunks!= null){
                for (File chunkFile : chunks){
                    ChunkMetadata chunkMetadata = storedChunksMetadata.getChunk(fileId.getName(), Integer.valueOf(chunkFile.getName()));
                    if(!onlyBiggerPercDgr || chunkMetadata.biggerThanDesiredRep()){
                            PeerArgs peerArgs = peer.getArgs();
                            int chunkNo = Integer.parseInt(chunkFile.getName());
                            double size = chunkFile.length() / 1000.0;
                            System.out.println("[RECLAIM] Eliminating chunk: " + chunkFile.getPath() + " size: " + size);
                            System.out.println("          With perceived dgr = " + chunkMetadata.getPerceivedRepDgr() + " and rep = "+chunkMetadata.getRepDgr());
                            if (FileHandler.deleteFile(chunkFile)) {
                                peer.getMetadata().getStoredChunksMetadata().deleteChunk(fileName,chunkNo);
                                peer.getMetadata().getStoredChunksMetadata().deleteReceivedChunk(fileName,chunkNo);
                                peer.getMetadata().writeMetadata();
                                Removed removedMsg = new Removed(peerArgs.getVersion(), peerArgs.getPeerId(), fileId.getName(), Integer.parseInt(chunkFile.getName()));
                                messages.add(removedMsg.getBytes());
                                currentSize -= size;
                                System.out.println("[RECLAIM] Current Size = " + currentSize);
                                if (currentSize <= maxDiskSpace) break;
                            }
                    }
                }
            }
        }

        return currentSize;
    }

}


