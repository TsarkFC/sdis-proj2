package messages;

public abstract class Message {
    protected static final int MSG_TYPE_IDX = 0;
    protected static final int SENDER_ID_IDX = 1;
    protected static final int FILE_ID_IDX = 2;

    protected final Integer senderId;
    protected final String fileId;
    protected final int CR = 0xD;
    protected final int LF = 0xA;
    protected String[] tokens;

    public Message(Integer senderId, String fileId) {
        this.senderId = senderId;
        this.fileId = fileId;
    }

    public Message(String message) {
        tokens = message.split("\\s+", getNumberArguments());

        if (!tokens[MSG_TYPE_IDX].equals(getMsgType())) {
            System.out.println("ERROR: building " + tokens[MSG_TYPE_IDX] + " message with " + getMsgType() + " constructor!");
        }

        this.senderId = Integer.parseInt(tokens[SENDER_ID_IDX]);
        if(getNumberArguments() >= 5){
            this.fileId = tokens[FILE_ID_IDX];
        }else this.fileId="";
    }

    public String getMsgString() {
        return String.format("%s %d %s %s", getMsgType(), this.senderId,
                this.fileId, getExtraString());
    }

    public abstract String getMsgType();

    protected abstract String getExtraString();


    public abstract int getNumberArguments();

    public void printMsg() {
        System.out.println(getMsgType());
        System.out.println("Sender ID: " + this.senderId);
        System.out.println("File ID: " + this.fileId);
    }

    public abstract byte[] getBytes();

    public Integer getSenderId() {
        return senderId;
    }

    public String getFileId() {
        return fileId;
    }

    private byte[] getDoubleCRLF() {
        return new byte[]{(byte) CR, (byte) LF, (byte) CR, (byte) LF};
    }

    protected byte[] addCRLF(byte[] header) {
        byte[] crlf = getDoubleCRLF();

        byte[] msgBytes = new byte[header.length + crlf.length];
        System.arraycopy(header, 0, msgBytes, 0, header.length);
        System.arraycopy(crlf, 0, msgBytes, header.length, crlf.length);

        return msgBytes;
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

    public boolean samePeerAndSender(int peerId) {
        return senderId == peerId;
    }

    public boolean samePeerAndSender(Peer peer) {
        return senderId.equals(peer.getArgs().getPeerId());
    }
}

