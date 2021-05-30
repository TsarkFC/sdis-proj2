package messages.protocol;

import peer.Peer;
import utils.AddressPortList;

public abstract class Message {
    protected static final int MSG_TYPE_IDX = 0;
    protected static final int ADDRESS_IDX = 1;
    protected static final int PORT_IDX = 2;
    protected static final int FILE_ID_IDX = 3;
    protected final String ipAddress;
    protected final Integer port;
    protected final String fileId;
    protected final int CR = 0xD;
    protected final int LF = 0xA;
    protected String[] tokens;

    public Message(String ipAddress, Integer port, String fileId) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.fileId = fileId;
    }

    public Message(String message) {
        tokens = message.split("\\s+", getNumberArguments());

        if (!tokens[MSG_TYPE_IDX].equals(getMsgType())) {
            System.out.println("ERROR: building " + tokens[MSG_TYPE_IDX] + " message with " + getMsgType() + " constructor!");
        }

        this.ipAddress = tokens[ADDRESS_IDX];
        this.port = Integer.parseInt(tokens[PORT_IDX]);
        if (getNumberArguments() >= 5) {
            this.fileId = tokens[FILE_ID_IDX];
        } else this.fileId = "";
    }

    public String getMsgString() {
        return String.format("%s %s %d %s %s", getMsgType(), this.ipAddress, this.port,
                this.fileId, getExtraString());
    }

    public abstract String getMsgType();

    protected abstract String getExtraString();

    public String getIpAddress() {
        return ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    public abstract int getNumberArguments();

    public void printMsg() {
        System.out.println(getMsgType());
        System.out.println("Ip Address: " + this.ipAddress);
        System.out.println("Port: " + this.port);
        System.out.println("File ID: " + this.fileId);
    }

    public abstract byte[] getBytes();

    public String getFileId() {
        return fileId;
    }

    private byte[] getDoubleCRLF() {
        return new byte[]{(byte) CR, (byte) LF, (byte) CR, (byte) LF};
    }

    protected byte[] addBody(byte[] header, byte[] body) {
        byte[] crlf = getDoubleCRLF();
        int headerCrlfSize = header.length + crlf.length;

        byte[] msgBytes = new byte[header.length + crlf.length + body.length];
        System.arraycopy(header, 0, msgBytes, 0, header.length);
        System.arraycopy(crlf, 0, msgBytes, header.length, crlf.length);
        System.arraycopy(body, 0, msgBytes, headerCrlfSize, body.length);

        return msgBytes;
    }

    public static String getTypeStatic(String msg) {
        String[] stringArr = msg.split("\\s+", 4);
        return stringArr[MSG_TYPE_IDX];
    }

    public boolean samePeerAndSender(Peer peer) {
        AddressPortList addressPortList = peer.getArgs().getAddressPortList();
        String ipAddress = addressPortList.getChordAddressPort().getAddress();

        if (!ipAddress.equals(this.ipAddress)) return false;
        return port.equals(addressPortList.getMcAddressPort().getPort()) ||
                port.equals(addressPortList.getMdbAddressPort().getPort()) ||
                port.equals(addressPortList.getMdrAddressPort().getPort()) ||
                port.equals(addressPortList.getChordAddressPort().getPort());
    }
}

