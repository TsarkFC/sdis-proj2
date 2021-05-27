package channels;

import peer.Peer;
import protocol.BackupProtocolInitiator;
import ssl.SSLInformation;
import ssl.SSlArgs;
import ssl.SslReceiver;
import utils.AddressList;

import java.util.concurrent.*;

public class ChannelCoordinator {
    private final Peer peer;
    private BackupProtocolInitiator backupInitiator;

    public ChannelCoordinator(Peer peer) {
        this.peer = peer;
        AddressList addressList = peer.getArgs().getAddressList();
        initializeReceiver(peer.getArgs().getSslInformation(),addressList);
        this.createMDBChannel(addressList);
        this.createMCChannel(addressList);
        this.createMDRChannel(addressList);
    }
    //TODO Alterar a classe Channel
    //TODO Acho que ja nao e preciso, esta a ser inicializado no channel right?
    public void initializeReceiver(SSLInformation sslInformation,AddressList addressList){
        /*SslReceiver receiverThread = new SslReceiver(peer.getArgs().getSslInformation());
        receiverThread.addServer(addressList.getMcAddr().getAddress(), addressList.getMcAddr().getPort());
        receiverThread.addServer(addressList.getMdbAddr().getAddress(), addressList.getMdbAddr().getPort());
        receiverThread.addServer(addressList.getMdrAddr().getAddress(), addressList.getMdrAddr().getPort());
        receiverThread.addServer(peer.getArgs().getChordPeerIpAddr(), peer.getArgs().getChordPort());
        new Thread(receiverThread).start();*/
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
