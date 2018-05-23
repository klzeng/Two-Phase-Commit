import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/*
 * TPC Server implement TPCNode:
 * - both coordinator and participants are TPCServer, just the using different method subset.
 */

public class TPCServer implements TPCNode {
    static int decisionPort = 1200;
    String hostName;
    String hostIP;
    int RMIPort;

    String coordinator;
    LinkedList<String> participants;

    DatabaseOp dbOp;
    String logFile;
    AtomicInteger nextOpId;                     // each write op is assigned a different ipId
    HashMap<String, Integer> ongoingOps;        /* contains ongoing write operations <key, number>:
                                                 * - for participants:
                                                 *   - number is of no use
                                                 *   - if the key already in map, simply abort
                                                 * - for coordinator:
                                                 *   - number use to record corresponding vote
                                                 *   - not allow write op on 'key' if its already in map
                                                 */
    HashMap<Integer, Timer> timers;
    private class ListenDecisionRequest implements Runnable{
        public void run(){
            System.out.println("listening to getDecision request on port" + TPCServer.decisionPort);
            try {
                DatagramSocket socket = new DatagramSocket(TPCServer.decisionPort);
                while(true){
                    // wait for request
                    byte[] buf = new byte[256];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);

                    System.out.println();

                    String response = " ";
                    // set response
                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();
                    int requestedOpId = Integer.parseInt(new String(packet.getData(),0, packet.getLength()));

                    String record = getRecordFromLog(requestedOpId, null);
                    if(record == null)  response= " ";
                    else if( record.split(" ")[1].contentEquals("Global_Abort")) response ="Global_Abort";
                    else if(record.split(" ")[1].contentEquals("Global_Commit")) response = "Global_Commit";

                    buf = new byte[256];
                    buf = response.getBytes();
                    packet = new DatagramPacket(buf, buf.length, address, port);
                    socket.send(packet);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    // constuctor
    public TPCServer(int portNum){
        // initialization for RMI setup
        try {
            InetAddress host = InetAddress.getLocalHost();
            this.hostName = host.getHostName();
            this.hostIP = host.getHostAddress();
            this.RMIPort = portNum;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        // register coordinator
        try {
            TPCNode stub = (TPCNode) UnicastRemoteObject.exportObject(this, 0);
            Registry registry = LocateRegistry.createRegistry(portNum);
            registry.rebind("2PCServer", stub);
            System.out.println("2PC server started!\n- Host_Name: " + this.hostName+ "\n- Host_IP: " + this.hostIP);
            System.out.println("RMI port: " + this.RMIPort);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // initialization for 2PC

        this.dbOp = new DatabaseOp();
        try{
            this.logFile = this.hostName + "_log.txt";
            File f = new File(this.logFile);
            if(f.exists() && !f.isDirectory()){
                BufferedReader bf = new BufferedReader(new FileReader(f));
                String line = null;
                String preline = null;
                while ((line = bf.readLine()) != null ){
                    preline = line;
                }
                // preline is now the last line
                if(preline != null){
                    this.nextOpId = new AtomicInteger(Integer.parseInt(preline.split(" ")[0])+1);
                }else {
                    this.nextOpId = new AtomicInteger(0);
                }
            }else {
                f.createNewFile();
                this.nextOpId = new AtomicInteger(0);
            }
        }catch (Exception e){
            System.out.println("open/create log file failed.");
            e.printStackTrace();
            System.exit(1);
        }

        this.participants = new LinkedList<String>();
        this.ongoingOps = new HashMap<>();
        this.timers = new HashMap<>();
        this.dbOp.getConnection(this.hostName);
        new Thread(new ListenDecisionRequest()).start();
    }

    /*---------------------------------------------------------------------------------------
     * util functions, facilitate the remote 2PC calls
     */

    // each write operation should be assigned a unique operation id
    public int getOpId(){
        return this.nextOpId.getAndIncrement();
    }

    public boolean canOp(String key){
        synchronized (this.ongoingOps){
            if(this.ongoingOps.containsKey(key)){
                return false;
            }else {
                this.ongoingOps.put(key, 0);
                System.out.println("-> added " +key );
                return true;
            }
        }
    }

    public boolean opOngoing(String key){
        synchronized (this.ongoingOps){
            return this.ongoingOps.containsKey(key);
        }
    }

    public int getVote(String key){
        synchronized (this.ongoingOps){
            this.ongoingOps.put(key, this.ongoingOps.get(key)+1);
            return this.ongoingOps.get(key);
        }
    }

    public void delOp(String key){
        synchronized (this.ongoingOps){
            this.ongoingOps.remove(key);
            System.out.println("-> removed " + key);
        }
    }

    public void writeLog(String toWrite){
        synchronized (this){
            try{
                BufferedWriter bw = new BufferedWriter(new FileWriter(this.logFile, true));
                bw.write(toWrite);
                bw.close();
                toWrite = toWrite.substring(0, toWrite.length()-1);
                System.out.println("-> Wrote to log: " + toWrite);
            }catch (IOException e){
                System.out.println("ERROR writing to log: put operation.s");
            }
        }
    }

    public void wait4Vote(int opId, String op, String key, String value){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(opOngoing(key)){
                    // multicast decision
                    System.out.println("-> timeout waiting for vote, op " + opId);
                    multiCastDecison("abort", opId, op, key, value);
                }
            }
        }, 2000);

        this.timers.put(opId, timer);
    }

    public void wait4Decision(int opId, String op, String key, String value){
        Timer timer = new Timer();
        this.timers.put(opId, timer);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("-> timeout waiting for op " + opId);
                getDecision(opId, op, key, value);

            }
        }, 2000);
    }

    /*
     * may be called from doAbort and getDecisionRequest:
     * - doAbort: OpId = -1, key is the one to recover
     * - getDecisionRequset: key is null, OpId is the op looking for
     */
    public String getRecordFromLog(int OpId, String key){
        try{
            BufferedReader bf = new BufferedReader(new FileReader(this.logFile));
            LinkedList<String> lines = new LinkedList<>();
            String line;
            while ((line = bf.readLine()) != null ){
                lines.add(new String(line));
            }
            bf.close();
            for (int i=lines.size()-1; i>=0; i--){
                line = lines.get(i);
                String[] records = line.split(" ");
                if(OpId == -1){
                    if(records[1].contentEquals("Global_Commit") && records[2].contentEquals(key)){
                        return line;
                    }
                }else{
                    if(Integer.parseInt(records[0]) == OpId){
                        return line;
                    }
                }

            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    /*
     * for the Coordinator to involve the participants.
     * - input as String of host(name:port) separated by space, the first one is the coordinator.
     */
    @Override
    public void handShaking(String hosts) {
        String[] ptps = hosts.split(" ");
        this.coordinator = ptps[0];
        for(int i=1; i<ptps.length; i++) this.participants.add(ptps[i]);

        System.out.println("\nJoined!\n");
        System.out.println("- coordinator: " + this.coordinator);
        System.out.println("- participants:");
        for(String each : this.participants) System.out.println(each);
    }

    public TPCNode getRMIObect(String hostAddr){
        TPCNode server = null;

        String host = hostAddr.split(":")[0];
        int port = Integer.parseInt(hostAddr.split(":")[1]);
        try{
            Registry registry = LocateRegistry.getRegistry(host, port);
            server = (TPCNode) registry.lookup("2PCServer");
        }catch (RemoteException | NotBoundException e){
            System.out.println("Error looking up " + host);
            e.printStackTrace();
        }

        return server;
    }

    /*---------------------------------------------------------------------------------------
     * 2PC protocol functions
     */

    // calls participants exposed to coordinator

    /*
     * Upon receiving canCommit, the participant:
     * - check if the key is already in ongoingOps
     * - if not, added it, write the log, write to db, vote yes
     * - if yes, vote abort
     */
    @Override
    public void canCommit(int opId, String op, String key, String value) throws RemoteException {
        TPCNode coordnt = this.getRMIObect(this.coordinator);

        System.out.println("\n\n- got canComit from coordinator.");
        String toWrite;
        if(this.canOp(key)){
            toWrite = opId + " Vote_Commit " + op + " " + key + " " + value + "\n";
            this.writeLog(toWrite);

            if(op.contentEquals("put")) this.dbOp.put(opId, key, value);
            else if(op.contentEquals("del")) this.dbOp.del(opId, key);
            else {
                System.out.println("DB opeartion error in canCommit: unknown op " + op);
            }

            coordnt.vote(opId, op, key, value,0);

            // set up timer
            wait4Decision(opId, op, key, value);
        }else {
            toWrite = opId + " Vote_Abort " + op + " " + key + " " + value + "\n";
            this.writeLog(toWrite);
            coordnt.vote(opId, op, key, value,1);
        }
    }

    public void multiCastDecison(String decision, int opId, String op, String key, String value){

        if(decision.contentEquals("commit")){
            try{
                this.doCommit(opId, op, key, value);
                for(String each : this.participants){
                    TPCNode server =  this.getRMIObect(each);
                    server.doCommit(opId, op, key, value);
                }
            }catch (RemoteException e){
                e.printStackTrace();
            }
        }else if(decision.contentEquals("abort")){
            try{
                this.doAbort(opId, op, key, value);
                for(String each : this.participants){
                    TPCNode server =  this.getRMIObect(each);
                    server.doAbort(opId, op, key, value);
                }
            }catch (RemoteException e){
                e.printStackTrace();
            }
        }

    }

    // write log, and del thek ey in the onGoingOp map
    @Override
    public void doCommit(int opId, String op, String key, String value) throws RemoteException {
        try {
            this.timers.get(opId).cancel();
            this.timers.remove(opId);
            System.out.println("-> timer " + opId +" canceled");
        }catch (NullPointerException e){
            System.out.println("-> Error cancle timer");
        }
        String toWrite = opId + " Global_Commit " + op + " " + key + " " +value + "\n";
        this.writeLog(toWrite);
        this.delOp(key);

        TPCNode coordnt =  this.getRMIObect(this.coordinator);
        coordnt.haveCommitted(opId, op, key, value, this.hostName);
    }

    /*
     * when a participant receive doAbort:
     * - it has already commit its change to local db
     * - so it search for the log
     * - and roll back according the log
     * - del the key in onGoingOp map
     */
    @Override
    public void doAbort(int opId, String op, String key, String value) {
        try {
            this.timers.get(opId).cancel();
            this.timers.remove(opId);
            System.out.println("-> timer " + opId +" canceled");
        }catch (NullPointerException e){
            System.out.println("-> Error cancle timer");
        }

        // roll back according log file
        String log = this.getRecordFromLog(-1, key);
        System.out.println("\n- aborting " + opId);
        if(log == null){
            if(!op.contentEquals("del"))
                this.dbOp.del(opId,key);
        }else {
            String[] record = log.split(" ");
            String lastOp = record[2];
            String lastValue = record[4];
            if(lastOp.contentEquals("del")){
                System.out.println("-> should delete "+ key);
                if(op.contentEquals("del")){
                    System.out.println("-> no action needed");
                }else {
                    System.out.println("-> rolling back to: " + record[0] + " del " + key);
                    this.dbOp.del(opId,key);
                }
            }else if(lastOp.contentEquals("put")){
                System.out.println("-> rolling back to: " + record[0] + " put " + lastValue);
                this.dbOp.put(opId, key, lastValue);
            }
        }

        String toWrite = opId + " Global_Abort " + op + " " + key + " " + value + "\n";
        this.writeLog(toWrite);
        this.delOp(key);
    }

    // calls coordinator exposed to participants

    /*
     * both haveCommmitted and vote need to keep a counter: use hashmap
     * -
     */
    @Override
    public String haveCommitted(int id,  String op, String key, String value, String participant) {
        return null;
    }

    @Override
    public void vote(int opId, String op, String key, String value, int canCommit){
        if(!this.opOngoing(key)) return;

        if(canCommit == 0){
            int votes = this.getVote(key);
            if(votes == this.participants.size()+1){
                System.out.println("-> got enough vote for "+ opId);
                this.multiCastDecison("commit", opId, op, key, value);
            }
        }else {
            this.multiCastDecison("abort", opId, op, key, value);
        }
    }

    public String getDecision(int opId, String op, String key, String value) {
        try {
            String request = Integer.toString(opId);
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet;

            //  coordinator
            try{
                System.out.println("- Sending getDecision request to coordinator");
                InetAddress address = InetAddress.getByName(this.coordinator.split(":")[0]);
                packet = new DatagramPacket(request.getBytes(), request.length(), address, TPCServer.decisionPort);
                socket.send(packet);
            }catch (UnknownHostException e){
                e.printStackTrace();
            }

            // all the participants
            for(String each : this.participants){
                try{
                    System.out.println("- Sending getDecision request to " + each);
                    InetAddress address = InetAddress.getByName(each.split(":")[0]);
                    packet = new DatagramPacket(request.getBytes(), request.length(), address, TPCServer.decisionPort);
                    socket.send(packet);
                }catch (UnknownHostException e){
                    e.printStackTrace();
                }
            }

            byte[] buf = new byte[256];
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            for(int i=0; i<this.participants.size()+1; i++){
                String response = new String(packet.getData(),0, packet.getLength());
                if(response.contentEquals("Global_Commit")) {
                    this.doCommit(opId,op, key, value);
                    break;
                }
                else if(request.contentEquals("Global_Abort")){
                    this.doAbort(opId, op, key, value);
                    break;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }


    /*---------------------------------------------------------------------------------------
     * Key/Value store interfaces
     */

    /*
     * do write is called by coordinator when it received write request from client
     */
    public void doWrite(String op, String key, String value){

        System.out.println("\n\n- Request: " + op + " " + key + " " + value);
        if(this.canOp(key)){
            int opId = this.getOpId();
            String toWrite = opId + " START_2PC  " + op + " "+ key + " " + value +"\n";
            this.writeLog(toWrite);

            if(op.contentEquals("put")) this.dbOp.put(opId,key, value);
            else if(op.contentEquals("del")) this.dbOp.del(opId, key);
            else {
                System.out.println("DB opeartion error in canCommit: unknown op " + op);
            }

            // multicast canCommit to all participants
            for(String each : this.participants){
                TPCNode server = this.getRMIObect(each);
                try{
                    server.canCommit(opId, op, key, value);
                    System.out.println("-> sent canCommit to " + each +" :" + toWrite);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }

            this.vote(opId, op, key, value, 0);
            this.wait4Vote(opId, op, key, value);
        }else {
            System.out.println("can't perform request now!");
        }
    }

    @Override
    public String doGet(String key) {
        System.out.println("\n\n- Request: get " + key);
        return this.dbOp.get(key);
    }

    @Override
    public int put(String key, String value) {
        this.doWrite("put", key, value);
        return 0;
    }

    @Override
    public int del(String key) {
        this.doWrite("del", key, null);
        return 0;
    }

    @Override
    public String get(String key) throws RemoteException {
        System.out.println("\n\n- Request: get " + key);
        String ret = null;
        if(this.participants.size() != 0){
            String dup = this.participants.get(this.nextOpId.get() % this.participants.size());
            TPCNode ptcp = this.getRMIObect(dup);
            System.out.println("-> redirected to "+ dup);
            ret = ptcp.doGet(key);
        }else {
            ret = this.dbOp.get(key);
        }

        return ret;
    }

    @Override
    public String getAll() throws RemoteException{
        StringBuilder ret = new StringBuilder();
        ret.append("\n\n\nTimestamp " + System.currentTimeMillis() +"\n");
        ret.append(this.dbOp.selectAll());
        for(String each: this.participants){
            TPCNode server = this.getRMIObect(each);
            ret.append("\n"+server.doGetAll());
        }
        return ret.toString();
    }


    @Override
    public String doGetAll() {
        return this.dbOp.selectAll();
    }

    /*
     * a participant is simply a TPC server
     */
    public static void main(String[] args){
        if(args.length != 1){
            System.out.println("Usage: java -cp \".:sqlite-jdbc-3.21.0.jar\" -Djava.security.policy=server.policy TPCServer portNum");
            System.exit(0);
        }

        TPCServer participant = new TPCServer(Integer.parseInt(args[0]));
    }

}