package channels;

import chord.ChordNodeData;
import constants.Constants;
import messages.Messages;
import peer.Peer;
import ssl.SslReceiver;
import utils.AddressPortList;
import utils.SerializeChordData;
import utils.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;

public class ChordChannel extends Channel {
    private SerializeChordData serializeChordNode = new SerializeChordData();

    public ChordChannel(AddressPortList addressPortList, Peer peer) {
        super(addressPortList, peer);

        SslReceiver receiver = new SslReceiver(Constants.sslProtocol, addressPortList.getChordAddressPort().getAddress(),
                addressPortList.getChordAddressPort().getPort(), this);

        new Thread(receiver).start();
    }

    @Override
    public void handle(DatagramPacket packet) throws IOException {

    }

    /**
     * Handles message received as parameter and returns the adequate response
     *
     * @param message
     */
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
            case Messages.JOIN -> {
                System.out.println("[ChordChannel] Got JOIN message from peer with id: " + new String(data));
                Integer chordNodeId = Integer.parseInt(new String(data));
                ChordNodeData successor = peer.getChordNode().findSuccessor(chordNodeId);
                byte[] serialized = serializeChordNode.serialize(successor);
                return Utils.addCRLF(serialized);
            }

            case Messages.NOTIFY -> {
                System.out.println("[ChordChannel] Got NOTIFY message from peer with id: " + new String(data));
                ChordNodeData x = new SerializeChordData().deserialize(data);
                peer.getChordNode().receiveNotify(x);
                String discard = "DISCARD";
                byte[] toDiscard = discard.getBytes();
                return Utils.addCRLF(toDiscard);
            }

            case Messages.GET_PREDECESSOR -> {
                System.out.println("[ChordChannel] Got GET_PREDECESSOR message");
                ChordNodeData predecessor = peer.getChordNode().getPredecessor();
                byte[] serialized = serializeChordNode.serialize(predecessor);
                return Utils.addCRLF(serialized);
            }

            case Messages.GET_SUCCESSOR -> {
                System.out.println("[ChordChannel] Got GET_SUCCESSOR message from peer with id: " + Arrays.toString(data));
                Integer chordNodeId = Integer.parseInt(Arrays.toString(data));
                ChordNodeData successor = peer.getChordNode().findSuccessor(chordNodeId);
                byte[] serialized = serializeChordNode.serialize(successor);
                return Utils.addCRLF(serialized);
            }

            default -> {
                System.out.println("Unrecognized message!");
                return null;
            }
        }
    }
}
