All the commands are to be executed in the terminal.
Running the peer:
    - Using the python script:

        - This python script was used during the development of the project since it facilitates many steps:
            - Is able to run many peers at a time, by only specifying the wanted number.
            - Automatically starts and kills the RMI
            - Automatically chooses the channels addresses and ports (Easy to change inside the script).
            - Has option to redirect output to files or print every peer in terminal.
            - The python script runs the peer.sh to run the program.

        - In order to use the python script, the user should run python3 run_peers.py <NumPeers> <Version> <ShouldRedirect> in the src/ directory
           - NumPeers: Number of peers that the user wants to run.
           - Version: Version of the protocol that the user wants to run:
                - 1.0: Runs the most basic version of the protocol, without Enhancements.
                - 1.1 or other: Runs the project with all the Enhancements.
           - ShouldRedirect:
                - yes: The output of each peer is redirected to proj1/src/build/output/peer<id>.out
                - no: All Peers output to the terminal where the command is run.

    - Using the peer.sh script:
        - The script is available in the folder scripts/
        - Is supposed to be run on src/build/
        - There are some extra commands needed in comparison with the script:
            - Start the RMI - rmiregistry &
            - Compile project - ../scripts/compile.sh from the src/ directory
            - Run peer - ../../scripts/peer.sh <version> <peer_id> <svc_access_point> <mc_addr> <mc_port> <mdb_addr> <mdb_port> <mdr_addr> <mdr_port>
            from the src/build/ directory

Running the client:
	- Using test.sh available in proj1/scripts:
			- Is supposed to be run on src/build/
			- Usage: ./test.sh <peer_ap> <sub_protocol> <opnd_1> <opnd_2>
						- <sub_protocol>: BACKUP / RESTORE / DELETE / RECLAIM / ST
						- <opnd_1> and <opnd_2> are sub_protocol dependant:
									BACKUP <filepath> <replication_degree>
									RESTORE / DELETE <filepath_used_in_backup>
									RECLAIM <maximum_storage_space>
									STATE

    - Using testApp.sh available in proj1/src/
        - Cleans Classpath preventing common error.
        - Has some common scenarios to demonstrate program capabilities.
        - Runs test.sh with valid arguments and multiple scenarios.








