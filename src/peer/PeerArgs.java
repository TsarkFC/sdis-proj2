package peer;

import ssl.SSLInformation;
import ssl.SSlArgs;
import ssl.SslSender;
import utils.AddressList;
import utils.ChannelAddress;

public class PeerArgs {
    public static final Integer PEER_ID = 0;
    public static final Integer ACCESS_POINT = 1;
    public static final Integer ADDRESS = 2;
    public static final Integer PORT = 3;
    public static final Integer BOOT_ADDRESS = 4;
    public static final Integer BOOT_PORT = 5;

    //BOOT PEER: java Peer <peer_id> <service_access_point> <address> <port> <boot_address> <boot_port>
    //PEER: java Peer <peer_id> <service_access_point> <address> <port>
    final boolean isBoot;
    final Integer peerId;
    final String accessPoint;
    final String metadataPath;
    final String address;
    final Integer port;
    final String bootAddress;
    final Integer bootPort;
    final SSLInformation sslInformation = new SSLInformation();

    public PeerArgs(String[] args) throws NumberFormatException {
        peerId = Integer.parseInt(args[PEER_ID]);
        accessPoint = args[ACCESS_POINT];
        address = args[ADDRESS];
        port = Integer.parseInt(args[PORT]);
        if (args.length > 4) {
            isBoot = true;
            bootAddress = null;
            bootPort = null;
        } else {
            isBoot = false;
            bootAddress = args[BOOT_ADDRESS];
            bootPort = Integer.parseInt(args[BOOT_PORT]);
        }
        metadataPath = "../filesystem/" + peerId + "/metadata";
        SslSender.setProtocol(sslInformation.getProtocol());

    }

    /*public PeerArgs(String[] args) throws NumberFormatException{
        version = args[VERSION];
        peerId = Integer.parseInt(args[PEER_ID]);
        accessPoint = args[ACCESS_POINT];
        ChannelAddress mcAddr = new ChannelAddress(args[MC_ADDR], Integer.parseInt(args[MC_PORT]));
        ChannelAddress mdbAddr = new ChannelAddress(args[MDB_ADDR], Integer.parseInt(args[MDB_PORT]));
        ChannelAddress mdrAddr = new ChannelAddress(args[MDR_ADDR], Integer.parseInt(args[MDR_PORT]));
        addressList = new AddressList(mcAddr, mdbAddr, mdrAddr);
        metadataPath = "../filesystem/" + peerId + "/metadata";
        SslSender.setProtocol(sslInformation.getProtocol());
    }*/

    public boolean isBoot() {
        return isBoot;
    }

    public Integer getPeerId() {
        return peerId;
    }

    public String getAccessPoint() {
        return accessPoint;
    }

    public String getMetadataPath() {
        return metadataPath;
    }

    public int getPort() {
        return port;
    }
    public SSLInformation getSslInformation() {
        return sslInformation;
    }

    public String getAddress() {
        return address;
    }

    

    public int getBootPort() {
        return port;
    }

    public String getBootAddress() {
        return address;
    }
}
