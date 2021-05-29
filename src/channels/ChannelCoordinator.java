package channels;

import peer.Peer;
import protocol.BackupProtocolInitiator;
import ssl.SSLInformation;
import utils.AddressList;
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
