import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TPCNode extends Remote {

    /*
     * 2PC protocols
     */

    // participants to coordinator

    public void canCommit(int id, String op, String key, String value) throws RemoteException;

    public void doCommit(int id,String op, String key, String value) throws RemoteException;

    public void doAbort(int id, String op, String key, String value) throws RemoteException;

    public String doGet(String key) throws RemoteException;

    // coordinator to participants
    // canCommeit: 0 - yes, 1 - no
    public void vote(int id, String op, String key, String value, int canCommit) throws RemoteException;

    // coordinator to participants
    public String haveCommitted(int id,  String op, String key, String value, String participant) throws RemoteException;

    // to each other
    public String getDecision(int id, String op, String key, String value) throws RemoteException;


    /*
     * key/value store to clients(exposed from coordinator)
     * - for put/del, return value 0 means success, 1 means error
     * - for get, return null means failure
     */

    public int put(String key, String value) throws RemoteException;

    public int del(String key) throws RemoteException;

    public String get(String key) throws RemoteException;

    // util functions
    public void handShaking(String hosts) throws RemoteException;

    public void doWrite(String op, String key, String value) throws RemoteException;

}

