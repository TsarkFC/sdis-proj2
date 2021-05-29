package chord;

public class ChordMessage {
    private String header;
    private Integer senderId;

    public ChordMessage(String header, Integer senderId) {
        this.header = header;
        this.senderId = senderId;
    }

    public String toString() {
        return header + senderId;
    }

    public ChordMessage parseMessage(String message) {
        String[] messageParts = message.split(" ");
        return new ChordMessage(messageParts[0], Integer.parseInt(messageParts[1]));
    }
}
