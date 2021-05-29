package peer;

import constants.Constants;
import ssl.SSLInformation;
import ssl.SslSender;
import utils.AddressPort;
import utils.AddressPortList;

public class PeerArgs {
    public static final Integer PEER_ID = 0;
    public static final Integer ACCESS_POINT = 1;
    public static final Integer ADDRESS = 2;
    public static final Integer CHORD_PORT = 3;
    public static final Integer MC_PORT = 4;
    public static final Integer MDB_PORT = 5;
    public static final Integer MDR_PORT = 6;
    public static final Integer OTHER_ADDRESS = 7;
    public static final Integer OTHER_PORT = 8;

    //BOOT PEER: java Peer <peerId> <accessPoint> <host> <chordPort> <mcPort> <mdbPort> <mdrPort>
    //PEER: java Peer <peerId> <accessPoint> <host> <chordPort> <mcPort> <mdbPort> <mdrPort> <otherAddress> <otherPort>
    final boolean isBoot;
    final Integer peerId;
    final String accessPoint;
    final String metadataPath;
    final AddressPortList addressPortList;
    AddressPort otherPeerAddressPort;
    final SSLInformation sslInformation = new SSLInformation();

    public PeerArgs(String[] args) throws NumberFormatException {
        peerId = Integer.parseInt(args[PEER_ID]);
        accessPoint = args[ACCESS_POINT];

        AddressPort chordAddressPort = new AddressPort(args[ADDRESS], Integer.parseInt(args[CHORD_PORT]));
        AddressPort mcAddressPort = new AddressPort(args[ADDRESS], Integer.parseInt(args[MC_PORT]));
        AddressPort mdbAddressPort = new AddressPort(args[ADDRESS], Integer.parseInt(args[MDB_PORT]));
        AddressPort mdrAddressPort = new AddressPort(args[ADDRESS], Integer.parseInt(args[MDR_PORT]));
        addressPortList = new AddressPortList(chordAddressPort, mcAddressPort, mdbAddressPort, mdrAddressPort);

        if (args.length == Constants.isBootArgsLen) {
            isBoot = true;
            otherPeerAddressPort = null;
        } else {
            isBoot = false;
            otherPeerAddressPort = new AddressPort(args[OTHER_ADDRESS], Integer.parseInt(args[OTHER_PORT]));
        }
        metadataPath = "../filesystem/" + peerId + "/metadata";
        SslSender.setProtocol(SSLInformation.protocol);
    }

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

    public AddressPortList getAddressPortList() { return addressPortList; }
    
    public AddressPort getOtherPeerAddressPort() {
        return otherPeerAddressPort;
    }

    //TODO: remove
    public Double getVersion() { return 0.0; }

    public SSLInformation getSslInformation() {
        return sslInformation;
    }
}
