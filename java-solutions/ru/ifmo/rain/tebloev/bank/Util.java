package ru.ifmo.rain.tebloev.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

class Util {
    static void forcedUnexport(Remote remote) {
        try {
            UnicastRemoteObject.unexportObject(remote, true);
        } catch (RemoteException ignored) {
        }
    }

    static void log(String msg) {
        System.out.println(String.format("[LOG] %s", msg));
    }

    static void handleException(String msg, Exception e) {
        System.err.println(String.format("%s%ncaused by %s", msg, e));
    }
}
