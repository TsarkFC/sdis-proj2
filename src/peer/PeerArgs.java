package peer;

import ssl.SSLInformation;
import ssl.SSlArgs;
import ssl.SslSender;
import utils.AddressList;
import utils.ChannelAddress;

public class PeerArgs {
    public static final Integer VERSION = 0;
    public static final Integer PEER_ID = 1;
    public static final Integer ACCESS_POINT = 2;
    public static final Integer MC_ADDR = 3;
    public static final Integer MC_PORT = 4;
    public static final Integer MDB_ADDR = 5;
    public static final Integer MDB_PORT = 6;
    public static final Integer MDR_ADDR = 7;
    public static final Integer MDR_PORT = 8;
    public static final Integer CHORD_PORT_IDX = 9;

    //java Peer <protocol_version> <peer_id> <service_access_point> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>
    final String version;
    final Integer peerId;
    final String accessPoint;
    final AddressList addressList;
    final String metadataPath;
    //TODO Receber isto como argumento e mudar valores
    final int chordPort = 1873;
    final String chordPeerIPAddr = "228.25.25.25";
    final SSLInformation sslInformation = new SSLInformation();



    public Double getVersion() {
        return Double.parseDouble(version);
    }

    public Integer getPeerId() {
        return peerId;
    }

    public String getAccessPoint() {
        return accessPoint;
    }

    public AddressList getAddressList() {
        return addressList;
    }

    public String getMetadataPath() {
        return metadataPath;
    }

    public int getChordPort() {return chordPort;}

    public String getChordPeerIpAddr(){return chordPeerIPAddr;}

    public SSLInformation getSslInformation() {
        return sslInformation;
    }





    public PeerArgs(String[] args) throws NumberFormatException{
        version = args[VERSION];
        peerId = Integer.parseInt(args[PEER_ID]);
        accessPoint = args[ACCESS_POINT];
        ChannelAddress mcAddr = new ChannelAddress(args[MC_ADDR], Integer.parseInt(args[MC_PORT]));
        ChannelAddress mdbAddr = new ChannelAddress(args[MDB_ADDR], Integer.parseInt(args[MDB_PORT]));
        ChannelAddress mdrAddr = new ChannelAddress(args[MDR_ADDR], Integer.parseInt(args[MDR_PORT]));
        addressList = new AddressList(mcAddr, mdbAddr, mdrAddr);
        metadataPath = "../filesystem/" + peerId + "/metadata";
        SslSender.setProtocol(sslInformation.getProtocol());

    }

}
