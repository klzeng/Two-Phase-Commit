import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TPCNode extends Remote {

    /*
     * 2PC protocols
     */

    // participants to coordinator

    void handShaking(String hosts) throws RemoteException;

    void canCommit(int id, String op, String key, String value) throws RemoteException; //canCommit: 0 - yes, 1 - no

    void doCommit(int id, String op, String key, String value) throws RemoteException;

    void doAbort(int id, String op, String key, String value) throws RemoteException;

    String doGet(String key) throws RemoteException;


    // coordinator to participants

    void vote(int id, String op, String key, String value, int canCommit) throws RemoteException;

    String haveCommitted(int id, String op, String key, String value, String participant) throws RemoteException;


    /*
     * key/value store to clients(exposed from coordinator)
     */

    int put(String key, String value) throws RemoteException;

    int del(String key) throws RemoteException;

    String get(String key) throws RemoteException;

    // following two for testing only
    String getAll() throws RemoteException;

    String doGetAll() throws RemoteException;
}

