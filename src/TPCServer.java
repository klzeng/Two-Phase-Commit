import java.rmi.RemoteException;

public class TPCServer implements TPCNode{
    @Override
    public String canCommit(int id, String op, String key, String value) throws RemoteException {
        return null;
    }

    @Override
    public void doCommit(int id) throws RemoteException {

    }

    @Override
    public void doAbort(int id) throws RemoteException {

    }

    @Override
    public String haveCommitted(int id, String participant) throws RemoteException {
        return null;
    }

    @Override
    public String getDecision(int id) throws RemoteException {
        return null;
    }

    @Override
    public int put(String key, String value) throws RemoteException {
        return 0;
    }

    @Override
    public int del(String key) throws RemoteException {
        return 0;
    }

    @Override
    public String get(String key) throws RemoteException {
        return null;
    }
}