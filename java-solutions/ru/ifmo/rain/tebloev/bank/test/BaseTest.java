package ru.ifmo.rain.tebloev.bank.test;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ru.ifmo.rain.tebloev.bank.common.Bank;
import ru.ifmo.rain.tebloev.bank.common.Util;
import ru.ifmo.rain.tebloev.bank.server.Server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.function.Consumer;

@RunWith(JUnit4.class)
public class BaseTest {
    protected static final List<String> STRINGS = List.of(
            "firstName", "last_name", "passpo9878rt",
            "имя", "фамилия", "ном981ер паспо491рта",
            "الاسم", "اللقب", "جواز الس91986فر",
            "{870", "#$%^&*()"
    );

    protected static final List<Integer> INTEGERS = List.of(
            456789, 56789324, -2342, 23424241, -234234,
            +55959, -234243, +788878
    );

    protected static Bank bank;
    private static Server server;
    private static Registry registry;
    @Rule
    public TestRule watcher = watcher(description -> System.err.println("=== Running " + description.getMethodName()));

    @BeforeClass
    public static void startServer() throws RemoteException {
        server = new Server();
        server.start(Util.DEFAULT_RMI_PORT);

        try {
            registry = LocateRegistry.getRegistry();
        } catch (RemoteException e) {
            Assert.fail("Registry reference could not be created");
        }

        try {
            bank = (Bank) registry.lookup("bank");
        } catch (NotBoundException e) {
            Assert.fail("Bank isn't bound");
        }
    }

    @AfterClass
    public static void closeServer() {
        server.close();
    }

    protected static TestWatcher watcher(final Consumer<Description> watcher) {
        return new TestWatcher() {
            @Override
            protected void starting(final Description description) {
                watcher.accept(description);
            }
        };
    }
}
