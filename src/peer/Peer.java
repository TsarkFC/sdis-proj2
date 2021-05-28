package peer;

import channels.ChannelCoordinator;
import chord.ChordNode;
import filehandler.FileHandler;
import peer.metadata.Metadata;
import protocol.*;
import utils.AddressPort;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

//java peer.Peer <protocol_version> <peer_id> <service_access_point> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>
public class Peer implements RemoteObject {

    private ChannelCoordinator channelCoordinator;
    private PeerArgs peerArgs;
    private Metadata metadata;
    private String fileSystem;
    private Protocol protocol;
    private String restoreDir;
    private String filesDir;
    private ChordNode chordNode;

    private ConcurrentSkipListSet<String> chunksReceived = new ConcurrentSkipListSet<>();
    private final ConcurrentHashMap<String, ConcurrentHashMap<Integer, byte[]>> activeRestores = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        Peer peer = new Peer();
        peer.peerArgs = peer.createPeerArgs(args);
        if (peer.peerArgs == null) return;
        peer.startFileSystem();
        peer.createMetadata();
        if (!peer.connectToRmi()) return;
        peer.createChannels();
        peer.createChordNode();

        //StartProtocol startProtocol = new StartProtocol(peer);
        //startProtocol.sendStartingMessage();
    }

    @Override
    public void backup(File file, int repDegree) throws IOException {
        System.out.println("[BACKUP] Initiator peer received Backup");
        this.protocol = new BackupProtocol(file, this, repDegree);
        this.protocol.initialize();
    }

    @Override
    public void restore(String path) throws IOException {
        System.out.println("[RESTORE] Initiator peer received Restore");
        this.protocol = new RestoreProtocol(path, this);
        this.protocol.initialize();
    }

    @Override
    public void delete(String path) throws IOException {
        System.out.println("[DELETE] Initiator peer received Delete");
        this.protocol = new DeleteProtocol(path, this);
        this.protocol.initialize();
    }

    @Override
    public String state() throws RemoteException {
        System.out.println("[STATE] Initiator peer received State");
        return metadata.returnState();
    }

    @Override
    public void reclaim(double maxDiskSpace) throws IOException {
        System.out.println("[RECLAIM] Initiator peer received Reclaim");
        this.protocol = new ReclaimProtocol(maxDiskSpace, this);
        this.protocol.initialize();
    }

    public void addChunk(String fileId, Integer chunkNo, byte[] chunk) {
        ConcurrentHashMap<Integer, byte[]> restore = activeRestores.get(fileId);
        if (restore == null) {
            System.out.println("[RESTORE] Restore complete, discarding...");
            return;
        }
        restore.put(chunkNo, chunk);
        if (restore.size() >= FileHandler.getNumberOfChunks(metadata.getFileSize(fileId))) {
            Path restoreFilePath = Paths.get(metadata.getFileMetadata(fileId).getPathname());
            String filename = getRestoreDir() + "/" + restoreFilePath.getFileName();
            if (!hasRestoreEntry(fileId)) return;
            ConcurrentHashMap<Integer, byte[]> copy = new ConcurrentHashMap<>(restore);
            activeRestores.remove(fileId);
            FileHandler.restoreFile(filename, copy);
            System.out.println("[RESTORE] Completed Restore");
        }
    }

    public PeerArgs createPeerArgs(String[] args) {
        try {
            return new PeerArgs(args);
        } catch (NumberFormatException e) {
            System.out.println("Error parsing peer arguments");
            System.out.println("Expected Integer and found String");
            return null;
        }
    }

    public void createMetadata() {
        Metadata metadata = new Metadata(this.getArgs().getMetadataPath());
        this.setMetadata(metadata.readMetadata());
    }

    public void createChannels() {
        channelCoordinator = new ChannelCoordinator(this);
    }

    public void startFileSystem() {
        fileSystem = "../filesystem/" + peerArgs.getPeerId();
        filesDir = fileSystem + "/files";
        restoreDir = fileSystem + "/restored";
        try {
            Files.createDirectories(Paths.get(filesDir));
            Files.createDirectories(Paths.get(restoreDir));
        } catch (IOException e) {
            System.out.println("Error creating Directories");
        }
    }

    public boolean connectToRmi() {
        try {
            String remoteObjName = this.peerArgs.getAccessPoint();
            RemoteObject stub = (RemoteObject) UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(remoteObjName, stub);
            System.err.println("Peer with name: " + remoteObjName + " ready");
        } catch (Exception e) {
            System.out.println("Error creating peer and connecting to RMI: " + e);
            return false;
        }
        return true;
    }

    public void createChordNode() {
        this.chordNode = new ChordNode(this);
        if (this.peerArgs.isBoot) this.chordNode.create();
        else {
            AddressPort addressPort = this.peerArgs.getOtherPeerAddressPort();
            this.chordNode.join(addressPort.getAddress(), addressPort.getPort());
        }
    }

    public String getFileSystem() {
        return fileSystem;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public PeerArgs getArgs() {
        return peerArgs;
    }

    public String getRestoreDir() {
        return restoreDir;
    }

    public ChannelCoordinator getChannelCoordinator() {
        return channelCoordinator;
    }

    public boolean hasReceivedChunk(String chunkId) {
        return chunksReceived.contains(chunkId);
    }

    public void addChunkReceived(String chunkId) {
        this.chunksReceived.add(chunkId);
    }

    public void resetChunksReceived() {
        this.chunksReceived = new ConcurrentSkipListSet<>();
    }

    public void addRestoreEntry(String fileId) {
        activeRestores.put(fileId, new ConcurrentHashMap<>());
    }

    public boolean hasRestoreEntry(String fileId) {
        return activeRestores.get(fileId) != null;
    }

    public ChordNode getChordNode() {
        return chordNode;
    }

    //TODO: remove
    public boolean isVanillaVersion() {
        return true;
    }

}