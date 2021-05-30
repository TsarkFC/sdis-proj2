package channels;

import peer.Peer;
import protocol.BackupProtocolInitiator;
import utils.AddressPortList;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChannelCoordinator {
    private final Peer peer;
    private BackupProtocolInitiator backupInitiator;

    public ChannelCoordinator(Peer peer) {
        this.peer = peer;
        AddressPortList addressPortList = peer.getArgs().getAddressPortList();
        //initializeReceiver(peer.getArgs().getSslInformation(),addressPortList);
        this.createMDBChannel(addressPortList);
        this.createMCChannel(addressPortList);
        this.createMDRChannel(addressPortList);
        this.createChordChannel(addressPortList);
    }

    public void createMDBChannel(AddressPortList addressPortList) {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        BackupChannel backupChannel = new BackupChannel(addressPortList, peer);
        //executor.execute(backupChannel);
    }

    public void createMCChannel(AddressPortList addressPortList) {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        ControlChannel controlChannel = new ControlChannel(addressPortList, peer);
        //executor.execute(controlChannel);
    }

    public void createMDRChannel(AddressPortList addressPortList) {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        RestoreChannel restoreChannel = new RestoreChannel(addressPortList, peer);
        //executor.execute(restoreChannel);
    }

    public void createChordChannel(AddressPortList addressPortList) {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        ChordChannel chordChannel = new ChordChannel(addressPortList, peer);
        //executor.execute(chordChannel);
    }

    public BackupProtocolInitiator getBackupInitiator() {
        return backupInitiator;
    }

    public void setBackupInitiator(BackupProtocolInitiator backupInitiator) {
        this.backupInitiator = backupInitiator;
    }
}
