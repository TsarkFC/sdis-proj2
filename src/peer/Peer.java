package peer;

import channels.ChannelCoordinator;
import chord.ChordPeer;
import filehandler.FileHandler;
import peer.metadata.Metadata;
import protocol.*;

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
    private ChordPeer chordPeer;

    private ConcurrentSkipListSet<String> chunksReceived = new ConcurrentSkipListSet<>();
    private final ConcurrentHashMap<String, ConcurrentHashMap<Integer, byte[]>> activeRestores = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        if (args.length != 9) {
            System.out.println("Usage: java Peer <protocol_version> <peer_id> <service_access_point> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>");
            return;
        }
        // Peer creation
        Peer peer = new Peer();
        peer.peerArgs = peer.createPeerArgs(args);
        if (peer.peerArgs == null) return;
        peer.startFileSystem();
        peer.createMetadata();
        peer.connectToRmi();
        peer.createChordPeer();

    }

    // Estas sao as funçoes do Initiator peer, entao na classe Protocol e que ele vai começar o RING right?
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

    public boolean isVanillaVersion() {
        return peerArgs.getVersion() == 1.0;
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

    public void connectToRmi() {
        // RMI connection

        try {
            String remoteObjName = this.peerArgs.getAccessPoint();
            RemoteObject stub = (RemoteObject) UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(remoteObjName, stub);
            System.err.println("Peer with name: " + remoteObjName + " ready");
            this.createChannels();
            StartProtocol startProtocol = new StartProtocol(this);
            startProtocol.sendStartingMessage();
        } catch (Exception e) {
            System.out.println("Error creating peer and connecting to RMI: " + e);
        }
    }

    public void createChordPeer(){
        //TODO sera que ele aqui ja devia ver se ja existe ou nao?
        //Se criarmos no protocol temos a certeza que ele e o primeiro right?
        this.chordPeer = new ChordPeer(this);
        boolean isBoot = true;
        if (isBoot) this.chordPeer.create();
        else this.chordPeer.join(this.peerArgs.chordPeerIPAddr,this.peerArgs.getChordPort());

    }
}