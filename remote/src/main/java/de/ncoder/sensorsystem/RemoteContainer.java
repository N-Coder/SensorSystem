package de.ncoder.sensorsystem;

import de.ncoder.typedmap.Key;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

public interface RemoteContainer extends Remote {
    <T extends Component> T get(Key<T> key) throws RemoteException;

    boolean isRegistered(Key<? extends Component> key) throws RemoteException;

    Collection<Key<? extends Component>> getKeys() throws RemoteException;
}
