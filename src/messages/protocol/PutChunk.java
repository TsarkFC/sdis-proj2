package messages.protocol;

import peer.Peer;
import utils.AddressPort;
import utils.AddressPortList;

// PUTCHUNK <FileId> <ChunkNo> <IPAddress> <Port> <ReplicationDeg> <CRLF><CRLF><Body>
public class PutChunk extends MsgWithChunk {

    protected static final int ADDRESS_IDX = 3;
    protected static final int PORT_IDX = 4;
    final int REP_DGR_IDX = 5;
    final int SELF_RCV_IDX = 6;
    final int BODY_IDX = 7;

    protected final AddressPort addressPort;
    private final Integer replicationDeg;
    private Integer selfRcvCount;
    private final byte[] body;

    public PutChunk(String ipAddress, Integer port, String fileId, Integer chunkNo,
                    Integer replicationDeg, Integer selfRcvCount, byte[] body) {
        super(fileId, chunkNo);
        this.replicationDeg = replicationDeg;
        this.body = body;
        this.addressPort = new AddressPort(ipAddress, port);
        this.selfRcvCount = selfRcvCount;
    }

    public PutChunk(String header, byte[] body) {
        super(header);
        this.addressPort = new AddressPort(tokens[ADDRESS_IDX], Integer.parseInt(tokens[PORT_IDX]));
        this.replicationDeg = Integer.parseInt(tokens[REP_DGR_IDX]);
        this.selfRcvCount = Integer.parseInt(tokens[SELF_RCV_IDX]);
        this.body = body;
    }

    @Override
    public String getMsgType() {
        return "PUTCHUNK";
    }


    @Override
    public String getMsgString() {
        return String.format("%s %s %d %s %d %d %d", getMsgType(), this.fileId, this.chunkNo,
                this.addressPort.getAddress(), this.addressPort.getPort(), this.replicationDeg, this.selfRcvCount);
    }

    public String getIpAddress() {
        return addressPort.getAddress();
    }

    public Integer getPort() {
        return addressPort.getPort();
    }

    @Override
    public int getNumberArguments() {
        return 8;
    }

    public void printMsg() {
        super.printMsg();
        System.out.println("Ip Address: " + this.addressPort.getAddress());
        System.out.println("Port: " + this.addressPort.getPort());
        System.out.println("Rep dgr: " + this.replicationDeg);
        System.out.println("Body: " + new String(this.body));
    }

    @Override
    public byte[] getBytes() {
        String header = getMsgString();
        return addBody(header.getBytes(), body);
    }

    public Integer getReplicationDeg() {
        return replicationDeg;
    }

    public byte[] getBody() {
        return body;
    }

    public boolean samePeerAndSender(Peer peer) {
        return this.addressPort.samePeerAndSender(peer);
    }

    public Integer getSelfRcvCount() {
        return selfRcvCount;
    }

    public void incrementSelfRcvCount() {
        selfRcvCount++;
    }
}
