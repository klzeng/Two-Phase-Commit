import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TPCNode extends Remote {

    /*
     * 2PC protocols
     */

    // participants to coordinator
    public String canCommit(int id, String op, String key, String value) throws RemoteException;

    public void doCommit(int id) throws RemoteException;

    public void doAbort(int id) throws RemoteException;

    // coordinator to participants
    public String haveCommitted(int id, String participant) throws RemoteException;

    // to each other
    public String getDecision(int id) throws RemoteException;


    /*
     * key/value store to clients(exposed from coordinator)
     * - for put/del, return value 0 means success, 1 means error
     * - for get, return null means failure
     */

    public int put(String key, String value) throws RemoteException;

    public int del(String key) throws RemoteException;

    public String get(String key) throws RemoteException;

}

