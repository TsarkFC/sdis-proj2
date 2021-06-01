package channels;

import chord.ChordNodeData;
import messages.Messages;
import peer.Peer;
import ssl.SslReceiver;
import utils.AddressPortList;
import utils.SerializeChordData;
import utils.Utils;

import java.util.Arrays;

public class ChordChannel extends Channel {
    private final SerializeChordData serializeChordNode = new SerializeChordData();

    public ChordChannel(AddressPortList addressPortList, Peer peer) {
        super(addressPortList, peer);
        super.currentAddr = addressPortList.getChordAddressPort();

        SslReceiver receiver = new SslReceiver(currentAddr.getAddress(), currentAddr.getPort(), this);
        new Thread(receiver).start();
    }


    /**
     * Handles message received as parameter and returns the adequate response
     *
     * @param message
     */
    @Override
    public byte[] handle(byte[] message) {
        byte[] parsedMessage = Utils.readUntilCRLF(message);
        int firstSpacePos = new String(parsedMessage).indexOf(" ");
        boolean hasSpace = true;

        if (firstSpacePos == -1) {
            firstSpacePos = parsedMessage.length;
            hasSpace = false;
        }

        String header = new String(Arrays.copyOfRange(parsedMessage, 0, firstSpacePos));
        byte[] data = null;
        if (hasSpace) data = Arrays.copyOfRange(parsedMessage, firstSpacePos + 1, parsedMessage.length);

        switch (header) {
            case Messages.JOIN, Messages.GET_SUCCESSOR -> {
                Integer chordNodeId = Integer.parseInt(new String(data));
                ChordNodeData successor = peer.getChordNode().findSuccessor(chordNodeId);
                byte[] serialized = serializeChordNode.serialize(successor);
                return Utils.addCRLF(serialized);
            }

            case Messages.NOTIFY -> {
                ChordNodeData x = new SerializeChordData().deserialize(data);
                peer.getChordNode().receiveNotify(x);
                return Utils.discard();
            }

            case Messages.GET_PREDECESSOR -> {
                ChordNodeData predecessor = peer.getChordNode().getPredecessor();
                byte[] serialized = serializeChordNode.serialize(predecessor);
                return Utils.addCRLF(serialized);
            }

            default -> {
                System.out.println("Unrecognized message!");
                return null;
            }
        }
    }
}
