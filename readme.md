For Two-Phase Commit protocol, see  [wikipedia](https://en.wikipedia.org/wiki/Two-phase_commit_protocol)

#Structure of the code:
-	TPCNode is the Remote interface for 2PC protocol and key/value store, mainly divided into 2 categories:
  -	2PC protocol interface:
  -	key/store interface
-	TPCServer is the remote object implements TPCNode, each server (coordinator/participant) in the 2PC key/value store is a TPCServer.
-	Coordinator is a subclass of TPCServer. 

#Test
go to hydra cluster, directory: /scratch/kzeng3/src
- kill any process occupying port 1100 and 1200
- issue ./coordinaotr.sh on one node
- issue ./participant.sh on other nodes you want replicas to run
- in the coodinator program, entre replicas address, for instance "replica1:port1 replica2:port2"
- in the nodes you want client to run: issue ./clientConcurrent.sh masterNode masterRMIPort
- corresponding log files will be:
  - for 2PC servers: nodeName_log.txt
  - for client: nodeName_dbSnapshot.txt
