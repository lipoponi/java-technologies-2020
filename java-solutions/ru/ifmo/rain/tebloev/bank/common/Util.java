package ru.ifmo.rain.tebloev.bank.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Util {
    public static final int DEFAULT_RMI_PORT = 1099;

    public static void forcedUnexport(Remote remote) {
        try {
            UnicastRemoteObject.unexportObject(remote, true);
        } catch (RemoteException ignored) {
        }
    }

    public static void log(String msg) {
        System.out.println(String.format("[LOG] %s", msg));
    }

    public static void handleException(String msg) {
        System.err.println(String.format("[ERR] %s", msg));
    }

    public static void handleException(String msg, Exception e) {
        handleException(String.format("%s%ncaused by %s", msg, e));
    }
}
