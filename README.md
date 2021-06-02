# SDIS Project

SDIS Project for group T6G26.

Group members:

1. João Romão (up201806779@edu.fe.up.pt)
2. João Gonçalves (up201806796@edu.fe.up.pt)
3. Nuno Resende (up201806825@edu.fe.up.pt)
4. Tiago Alves (up201603820@edu.fe.up.pt)

All the commands are to be executed in the terminal.

## Running the peer

### Using the peer.sh script:
- The script is available in the folder scripts/
- Is supposed to be run on src/build/
- There are some extra commands needed in comparison with the script:
    - Start the RMI - rmiregistry &
    - Compile project - ../scripts/compile.sh from the src/ directory
    - Run initiator peer - ../../scripts/peer.sh <version> <peer_id> <svc_access_point> <ip_addr> <mc_port> <mdb_port> <mdr_port> <chord_port>
    - Run peer - ../../scripts/peer.sh <version> <peer_id> <svc_access_point> <ip_addr> <mc_port> <mdb_port> <mdr_port> <chord_port> <known_addr> <known_port>
      from the src/build/ directory

## Running the client:

### Using test.sh available in proj1/scripts:
- Is supposed to be run on src/build/
- Usage: ../../scripts/test.sh <peer_ap> <sub_protocol> <opnd_1> <opnd_2>
    - <sub_protocol>: BACKUP / RESTORE / DELETE / RECLAIM / ST
    - <opnd_1> and <opnd_2> are sub_protocol dependant:
      BACKUP <filepath> <replication_degree>
      RESTORE / DELETE <filepath_used_in_backup>
      RECLAIM <maximum_storage_space>
      STATE

### Using testApp.sh available in proj1/src/
- Cleans Classpath preventing common error.
- Has some common scenarios to demonstrate program capabilities.
- Runs test.sh with valid arguments and multiple scenarios.


















