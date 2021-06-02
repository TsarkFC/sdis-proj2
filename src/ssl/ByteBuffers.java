package ssl;

import java.nio.ByteBuffer;

public class ByteBuffers {
    public ByteBuffer getDecryptedData() {
        return decryptedData;
    }

    public ByteBuffer getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(ByteBuffer encryptedData) {
        this.encryptedData = encryptedData;
    }

    public ByteBuffer getPeerDecryptedData() {
        return peerDecryptedData;
    }

    public void setPeerDecryptedData(ByteBuffer peerDecryptedData) {
        this.peerDecryptedData = peerDecryptedData;
    }

    public ByteBuffer getPeerEncryptedData() {
        return peerEncryptedData;
    }

    public void setPeerEncryptedData(ByteBuffer peerEncryptedData) {
        this.peerEncryptedData = peerEncryptedData;
    }

    /**
     * Contains this peer decrypted/original application data
     */
    public ByteBuffer decryptedData;

    /**
     * Contains this peer encrypted network data
     */
    public ByteBuffer encryptedData;

    /**
     * Contains the other peer decrypted application data
     */
    public ByteBuffer peerDecryptedData;

    /**
     * Contains the other peer encrypted network data
     */
    public ByteBuffer peerEncryptedData;

    public ByteBuffers(int bufferSize) {
        decryptedData = ByteBuffer.allocate(bufferSize);
        encryptedData = ByteBuffer.allocate(bufferSize);
        peerDecryptedData = ByteBuffer.allocate(bufferSize);
        peerEncryptedData = ByteBuffer.allocate(bufferSize);
    }

    public ByteBuffers(int encryptedSize, int decryptedSize, boolean isPeer) {
        if (isPeer) {
            peerDecryptedData = ByteBuffer.allocate(decryptedSize);
            peerEncryptedData = ByteBuffer.allocate(encryptedSize);
        } else {
            decryptedData = ByteBuffer.allocate(decryptedSize);
            encryptedData = ByteBuffer.allocate(encryptedSize);
        }
    }
}
