import peer.RemoteObject;
import filehandler.FileHandler;
import protocols.Protocols;
import rmi.SubProtocol;

import java.io.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


// java Client <host_name> <remote_object_name> <oper> <opnd>*
// java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>
public class TestApp {

    private final int PEER_APP_IDX = 0;
    private final int SUB_PROTOCOL_IDX = 1;
    private final int PATH_IDX = 2;
    private final int DISK_SPACE_IDX = 2;
    private final int REPLICATION_DEGREE_IDX = 3;

    private String peerAp;
    private SubProtocol subProtocol;
    private String path;
    private double diskSpace; //RECLAIM
    private int replicationDegree; //Backup protocol

    private Protocols stub;

    public static void main(String[] args) {
        TestApp testApp = new TestApp();
        if (!testApp.parseArguments(args)) return;
        if (!testApp.connectRmi()) return;

        try {
            if (testApp.path != null) {
                testApp.processRequest(testApp.subProtocol, testApp.path);
            } else testApp.processRequest(testApp.subProtocol);
        } catch (IOException | InterruptedException e) {
            System.out.println("Error: Failed to process Client Request");
        }
    }

    private boolean parseArguments(String[] args) {

        if (args.length < 2) {
            System.out.println("Usage: <peer_ap> <sub_protocol> [<opnd_1>] [<opnd_2>]");
            return false;
        }
        this.peerAp = args[this.PEER_APP_IDX];
        this.subProtocol = SubProtocol.valueOf(args[this.SUB_PROTOCOL_IDX]);

        switch (this.subProtocol) {
            case BACKUP: {
                if (args.length != 4) {
                    System.out.println("Usage: <peer_ap> BACKUP <path_name> <replication_degree>");
                    return false;
                }
                this.replicationDegree = Integer.parseInt(args[this.REPLICATION_DEGREE_IDX]);
                if (this.replicationDegree > 9) {
                    System.out.println("Replication degree must be one digit!");
                }
                this.path = args[this.PATH_IDX];
                break;
            }
            case RESTORE: {
                if (args.length != 3) {
                    System.out.println("Usage: <peer_ap> RESTORE <path_name>");
                    return false;
                }
                this.path = args[this.PATH_IDX];
            }
            case DELETE: {
                if (args.length != 3) {
                    System.out.println("Usage: <peer_ap> DELETE <path_name>");
                    return false;
                }
                this.path = args[this.PATH_IDX];
                break;
            }
            case RECLAIM: {
                if (args.length != 3) {
                    System.out.println("Usage: <peer_ap> RECLAIM <path_name>");
                    return false;
                }
                diskSpace = Double.parseDouble(args[this.DISK_SPACE_IDX]);
                if ((int) diskSpace < -1) {
                    System.out.println("Usage: <peer_ap> RECLAIM <path_name> (disk space cannot be smaller that -1)");
                    return false;
                }
                break;
            }
            case STATE: {
                if (args.length != 2) {
                    System.out.println("Usage: <peer_ap> STATE");
                    return false;
                }
                break;
            }
            default: {
                System.out.println("Usage: <peer_ap> <sub_protocol> [<opnd_1>] [<opnd_2>]");
                return false;
            }
        }
        return true;
    }

    private boolean connectRmi() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            this.stub = (Protocols) registry.lookup(this.peerAp);
            System.out.println("Connected!");
            return true;
        } catch (Exception e) {
            System.err.println("Error connecting to RMI: " + e.getMessage());
            //e.printStackTrace();
        }
        return false;
    }

    private void processRequest(SubProtocol protocol, String path) throws IOException, InterruptedException {
        File file = FileHandler.getFile(path);
        if (file == null) return;
        switch (protocol) {
            case BACKUP -> {
                System.out.println("Initiating Backup Protocol");
                stub.backup(file, replicationDegree);
            }
            case DELETE -> {
                System.out.println("Initiating Delete Protocol");
                stub.delete(path);
            }
            case RESTORE -> {
                System.out.println("Initiating Restore Protocol");
                stub.restore(path);
            }
            default -> {
                System.out.println("[ERROR] File was null");
            }
        }
    }

    private void processRequest(SubProtocol protocol) throws IOException {
        String result = "";
        switch (protocol) {
            case RECLAIM: {
                System.out.println("Initiating Reclaim Protocol");
                stub.reclaim(diskSpace);
                break;
            }
            case STATE: {
                System.out.println("Initiating State Protocol");
                result = stub.state();
                break;
            }
            default: {
                System.out.println("Error processing request");
                break;
            }
        }
        System.out.println(result);
    }


}