package de.ncoder.sensorsystem.events;

import de.ncoder.sensorsystem.events.event.Event;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface EventListener extends Remote {
    void handle(Event event) throws RemoteException;
}
