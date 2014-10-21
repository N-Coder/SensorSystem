package de.ncoder.sensorsystem.events;

import de.ncoder.sensorsystem.events.event.Event;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteEventManager extends Remote {
    void subscribe(EventListener listener) throws RemoteException;

    void unsubscribe(EventListener listener) throws RemoteException;

    void publish(Event event) throws RemoteException;
}
