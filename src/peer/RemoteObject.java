package peer;

import java.io.File;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteObject extends Remote {
    void backup(File file, int repDegree) throws IOException;

    void restore(String path) throws IOException;

    void delete(String path) throws IOException;

    String state() throws RemoteException;

    void reclaim(double maxDiskSpace) throws IOException;
}
