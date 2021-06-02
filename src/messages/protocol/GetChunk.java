package messages.protocol;

import peer.Peer;
import utils.AddressPort;
import utils.AddressPortList;
import utils.Utils;

// GETCHUNK <FileID> <ChunkNo> <IPAddress> <Port> <REP_DGR> <CRLF><CRLF>
public class GetChunk extends MsgWithChunk {
    protected static final int ADDRESS_IDX = 3;
    protected static final int PORT_IDX = 4;
    protected static final int REP_DGR_IDX = 5;

    protected final AddressPort addressPort;
    private final int rep_dgr;

    public GetChunk(String ipAddress, Integer port, String fileId, Integer chunkNo,int rep_dgr) {
        super(fileId, chunkNo);
        this.addressPort = new AddressPort(ipAddress,port);
        this.rep_dgr = rep_dgr;
    }

    public GetChunk(String msg){
        super(msg);
        this.addressPort = new AddressPort(tokens[ADDRESS_IDX],Integer.parseInt(tokens[PORT_IDX]));
        this.rep_dgr = Integer.parseInt(tokens[REP_DGR_IDX]);
    }

    @Override
    public String getMsgType() {
        return "GETCHUNK";
    }

    @Override
    public String getMsgString() {
        return String.format("%s %s %d %s %d", getMsgType(), this.fileId, this.chunkNo,this.addressPort.getAddress(),this.addressPort.getPort());
    }

    public String getIpAddress() {
        return this.addressPort.getAddress();
    }

    public Integer getPort() {
        return this.addressPort.getPort();
    }

    public void printMsg() {
        super.printMsg();
        System.out.println("Ip Address: " + this.addressPort.getAddress());
        System.out.println("Port: " + this.addressPort.getPort());
    }


    @Override
    public int getNumberArguments() {
        return 6;
    }

    @Override
    public byte[] getBytes() {
        return Utils.addCRLF(getMsgString().getBytes());
    }

    public boolean samePeerAndSender(Peer peer){
        return this.addressPort.samePeerAndSender(peer);
    }


    public int getRep_dgr() {
        return rep_dgr;
    }
}
