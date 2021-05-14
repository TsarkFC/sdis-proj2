package channels;

import peer.Peer;
import protocol.BackupProtocolInitiator;
import utils.AddressList;

import java.util.concurrent.*;

public class ChannelCoordinator {
    private final Peer peer;
    private BackupProtocolInitiator backupInitiator;

    public ChannelCoordinator(Peer peer) {
        this.peer = peer;
        AddressList addressList = peer.getArgs().getAddressList();
        this.createMDBChannel(addressList);
        this.createMCChannel(addressList);
        this.createMDRChannel(addressList);
    }

    public void createMDBChannel(AddressList addressList) {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        BackupChannel backupChannel = new BackupChannel(addressList, peer);
        executor.execute(backupChannel);
    }

    public void createMCChannel(AddressList addressList) {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        ControlChannel controlChannel = new ControlChannel(addressList, peer);
        executor.execute(controlChannel);
    }

    public void createMDRChannel(AddressList addressList) {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        RestoreChannel restoreChannel = new RestoreChannel(addressList, peer);
        executor.execute(restoreChannel);
    }

    public BackupProtocolInitiator getBackupInitiator() {
        return backupInitiator;
    }

    public void setBackupInitiator(BackupProtocolInitiator backupInitiator) {
        this.backupInitiator = backupInitiator;
    }
}
