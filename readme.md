go to hydra cluster, directory: /scratch/kzeng3/src
- kill any process occupying port 1100 and 1200
- issue ./coordinaotr.sh on one node
- issue ./participant.sh on other nodes you want replicas to run
- in the coodinator program, entre replicas address, for instance "replica1:port1 replica2:port2"
- in the nodes you want client to run: issue ./clientConcurrent.sh masterNode masterRMIPort
- corresponding log files will be:
  - for 2PC servers: nodeName_log.txt
  - for client: nodeName_dbSnapshot.txt
