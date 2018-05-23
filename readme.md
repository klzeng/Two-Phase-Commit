For Two-Phase Commit protocol, see  [wikipedia](https://en.wikipedia.org/wiki/Two-phase_commit_protocol)

# Structure of the code:
-	*TPCNode* is the Remote interface for 2PC protocol and key/value store, mainly divided into 2 categories:
    -	2PC protocol interface:
   
            // participants to coordinator

            void handShaking(String hosts) throws RemoteException;

            void canCommit(int id, String op, String key, String value) throws RemoteException; //canCommit: 0 - yes, 1 - no

            void doCommit(int id, String op, String key, String value) throws RemoteException;

            void doAbort(int id, String op, String key, String value) throws RemoteException;

            String doGet(String key) throws RemoteException;


            // coordinator to participants

            void vote(int id, String op, String key, String value, int canCommit) throws RemoteException;

            String haveCommitted(int id, String op, String key, String value, String participant) throws RemoteException;
            
    -	key/store interface
            
            /*
             * key/value store to clients(exposed from coordinator)
             */

            int put(String key, String value) throws RemoteException;

            int del(String key) throws RemoteException;

            String get(String key) throws RemoteException;
            
-	*TPCServer* is the remote object implements TPCNode, each server (coordinator/participant) in the 2PC key/value store is a *TPCServer*.
-	*Coordinator* is a subclass of TPCServer. 

# Test
Two-Phase Commit works among a cluster of datastores, with one of them as master, so you should test it on a cluster. 
- kill any process occupying port 1100 and 1200
- issue ./coordinaotr.sh on one node
- issue ./participant.sh on other nodes you want replicas to run
- in the coodinator program, entre replicas address, for instance "replica1:port1 replica2:port2"
- in the nodes you want client to run: issue ./clientConcurrent.sh masterNode masterRMIPort
- corresponding log files will be:
  - for 2PC servers: nodeName_log.txt
  - for client: nodeName_dbSnapshot.txt
