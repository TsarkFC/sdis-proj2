package channels;

import chord.ChordNode;
import constants.Constants;
import messages.Messages;
import peer.Peer;
import ssl.SslReceiver;
import utils.AddressPortList;
import utils.SerializeChordNode;
import utils.Utils;

import java.io.IOException;
import java.net.DatagramPacket;

public class ChordChannel extends Channel {
    private SerializeChordNode serializeChordNode = new SerializeChordNode();

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
        String[] messageInfo = new String(Utils.readUntilCRLF(message)).split(" ");

        switch (messageInfo[0]) {
            case Messages.JOIN -> {
                System.out.println("[ChordChannel] Got JOIN message from peer with id: " + messageInfo[1]);
                Integer chordNodeId = Integer.parseInt(messageInfo[1]);
                ChordNode node = peer.getChordNode();
                ChordNode successor = node.findSuccessor(chordNodeId);

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
