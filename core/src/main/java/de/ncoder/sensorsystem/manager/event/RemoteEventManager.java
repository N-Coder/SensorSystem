package de.ncoder.sensorsystem.manager.event;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteEventManager extends Remote {
    void subscribe(EventManager.Listener listener) throws RemoteException;

    void unsubscribe(EventManager.Listener listener) throws RemoteException;

    void publish(Event event) throws RemoteException;
}
