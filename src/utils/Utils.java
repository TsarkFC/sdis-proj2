package utils;

public class Utils {
    public static byte[] readUntilCRLF(byte[] message) {
        int length = 0;
        for (int i = 0; i < message.length; i++) {
            if (message[i] == 0xD && message[i + 1] == 0xA &&
                    message[i + 2] == 0xD && message[i + 3] == 0xA) break;
            length++;
        }

        byte[] messageBytes = new byte[length];
        System.arraycopy(message, 0, messageBytes, 0, length);
        return messageBytes;
    }

    public static byte[] addCRLF(byte[] message) {
        byte[] crlf = getDoubleCRLF();
        byte[] messageBytes = new byte[message.length + crlf.length];
        System.arraycopy(message, 0, messageBytes, 0, message.length);
        System.arraycopy(crlf, 0, messageBytes, message.length, crlf.length);
        return messageBytes;
    }

    public static byte[] getDoubleCRLF() {
        return new byte[]{(byte) 0xD, (byte) 0xA, (byte) 0xD, (byte) 0xA};
    }

    public static byte[] discard() {
        String discard = "DISCARD";
        byte[] toDiscard = discard.getBytes();
        return Utils.addCRLF(toDiscard);
    }

    public static byte[] concatBuffer(byte[] buffer1, byte[] buffer2) {
        if (buffer1 == null) return buffer2;
        byte[] buffer = new byte[buffer1.length + buffer2.length];
        System.arraycopy(buffer1, 0, buffer, 0, buffer1.length);
        System.arraycopy(buffer2, 0, buffer, buffer1.length, buffer2.length);
        return buffer;
    }
}
