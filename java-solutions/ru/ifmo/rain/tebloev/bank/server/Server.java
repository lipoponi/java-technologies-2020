package ru.ifmo.rain.tebloev.bank.server;

import ru.ifmo.rain.tebloev.bank.common.Bank;
import ru.ifmo.rain.tebloev.bank.common.Util;

import java.io.Closeable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server implements Closeable {
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final Queue<Remote> exported = new ConcurrentLinkedQueue<>();
    private int port;

    public static void main(final String... args) {
        Server server = new Server();
        server.start(Util.DEFAULT_RMI_PORT);

        Util.log("Server started");

        Runtime.getRuntime().addShutdownHook(new Thread(server::close));
    }

    private void export(Remote obj) throws RemoteException {
        if (!started.get()) {
            throw new UnsupportedOperationException("Server needs to be started");
        }

        UnicastRemoteObject.exportObject(obj, port);
        exported.add(obj);
    }

    public synchronized void start(int port) {
        if (started.getAndSet(true)) {
            throw new UnsupportedOperationException("Server already started");
        }

        this.port = port;

        try {
            Registry registry = LocateRegistry.createRegistry(port);
            exported.add(registry);

            try {
                Bank bank = new RemoteBank(this::export);
                export(bank);

                registry.rebind("bank", bank);
            } catch (RemoteException e) {
                Util.handleException("Unable to create bank", e);
            }
        } catch (RemoteException e) {
            Util.handleException("Unable to create RMI registry", e);
        }
    }

    public synchronized void close() {
        if (!started.getAndSet(false)) {
            throw new UnsupportedOperationException("Server isn't running");
        }

        exported.forEach(Util::forcedUnexport);
        exported.clear();
    }
}
