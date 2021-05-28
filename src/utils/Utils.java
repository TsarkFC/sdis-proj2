package utils;

public class Utils {
    public static int generateRandomDelay() {
        int random = (int) (Math.random() * (400 + 1));
        System.out.println("Random delay: " + random);
        return random;
    }

    public static int generateRandomDelay(String message) {
        int random = (int) (Math.random() * (400 + 1));
        System.out.println(message + random + "ms");
        return random;
    }

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
}
