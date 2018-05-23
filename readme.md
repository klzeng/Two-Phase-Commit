For Two-Phase Commit protocol, see  [wikipedia](https://en.wikipedia.org/wiki/Two-phase_commit_protocol)

This project implements the two-phase commit protocol over a durable, distributed, replicated key/value store.

## Overview of this project:
**Part A: Durable Key/Value Store library:**

expose three methods to the client:
  - put(key, value): stores the value "value" with the key "key".
  - del(key): deletes any record associated with the key "key".
  - value = get(key): retrieves and returns the value associated with the key "key".
  
We build our key/value store on top of sqlite database library. 

**Part B: Two-Phase Commit:**

it consists of a single "master" process and multiple "replica" processes:
  - **_master_**: the "master" process expose an RPC interface to clients that contains three methods: get, put, and del. When the master process receives a state-changing operation (del or put), it uses two-phase commit to commit that state-changing operation to all replicas. When the master receives a "get" operation, it selects a replica at random to issue the request against.
  - **_replica_**: each replica wraps an instance of the key/value store, and it exposes an 2PC interface to the master to participate in two-phase commit.

Replicas are concurrent, for this project, the concurrency control scheme is very simple: if two concurrent operations manipulate different keys, they can safely proceed concurrently. If two concurrent operations manipulate the same key (e.g., two "put" operations to the same key show up at the same time), the first to arrive should be able to proceed while the second's two-phase commit should abort.

**Part C: Client:**

Client contact the master and issue commands. 

## Structure of the code:
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

## Test
Two-Phase Commit works among a cluster of datastores, with one of them as master, so you should test it on a cluster. 
- kill any process occupying port 1100 and 1200
- issue ./coordinaotr.sh on one node
- issue ./participant.sh on other nodes you want replicas to run
- in the coodinator program, entre replicas address, for instance "replica1:port1 replica2:port2"
- in the nodes you want client to run: issue ./clientConcurrent.sh masterNode masterRMIPort
- corresponding log files will be:
  - for 2PC servers: nodeName_log.txt
  - for client: nodeName_dbSnapshot.txt
