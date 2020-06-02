import org.junit.*;
import org.junit.runners.MethodSorters;
import ru.ifmo.rain.tebloev.bank.Account;
import ru.ifmo.rain.tebloev.bank.Bank;
import ru.ifmo.rain.tebloev.bank.Person;
import ru.ifmo.rain.tebloev.bank.Server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServerTest extends BaseTest {
    private static final List<String> STRINGS = List.of(
            "firstName", "last_name", "passpo9878rt",
            "имя", "фамилия", "ном981ер паспо491рта",
            "الاسم", "اللقب", "جواز الس91986فر",
            "{870", "#$%^&*()"
    );

    private static final List<Integer> INTEGERS = List.of(
            456789, 56789324, -2342, 23424241, -234234,
            +55959, -234243, +788878
    );

    private static final int DEFAULT_PORT = 1099;
    private static Server server;
    private static Registry registry;
    private static Bank bank;

    @BeforeClass
    public static void startServer() throws RemoteException {
        server = new Server();
        server.start(DEFAULT_PORT);

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

    @Test
    public void test01_accounts() throws RemoteException {
        for (String id : STRINGS) {
            Account account = bank.createAccount(id);
            Assert.assertEquals(account.getAmount(), 0);
            Assert.assertEquals(account.getId(), id);
            Assert.assertEquals(account, bank.getAccount(id));
        }
    }

    @Test
    public void test02_persons() throws RemoteException {
        for (int i = 0; i < (STRINGS.size() + 2) / 3; i++) {
            String firstName = STRINGS.get((i * 3) % STRINGS.size());
            String lastName = STRINGS.get((i * 3 + 1) % STRINGS.size());
            String passport = STRINGS.get((i * 3 + 2) % STRINGS.size());

            Person person = bank.createPerson(firstName, lastName, passport);
            Assert.assertEquals(person, bank.getPerson(passport, true));
            Person localPerson = bank.getPerson(passport, false);

            Assert.assertEquals(person.getFirstName(), localPerson.getFirstName());
            Assert.assertEquals(person.getLastName(), localPerson.getLastName());
            Assert.assertEquals(person.getPassport(), localPerson.getPassport());
        }
    }

    @Test
    public void test03_accountOperations() throws RemoteException {
        for (int i = 0; i < STRINGS.size(); i++) {
            String id = STRINGS.get(i);
            Account account = bank.createAccount(id);
            int balance = account.getAmount();
            for (int j = 0; j < 3; j++) {
                int amount = INTEGERS.get((i + j) % INTEGERS.size());
                account.setAmount(account.getAmount() + amount);
                balance += amount;
                Assert.assertEquals(balance, account.getAmount());
            }
        }
    }

    @Test
    public void test04_personOperations() throws RemoteException {
        String firstName = STRINGS.get(0);
        String lastName = STRINGS.get(4);
        String passport = STRINGS.get(8);

        Person person = bank.createPerson(firstName, lastName, passport);
        for (int i = 0; i < STRINGS.size(); i++) {
            String subId = STRINGS.get(i);

            Account account = person.createAccount(subId);
            Assert.assertEquals(account, person.getAccount(subId));
            int amount = INTEGERS.get(i % INTEGERS.size());
            account.setAmount(amount);
            Assert.assertEquals(amount, account.getAmount());
        }
    }

    @Test
    public void test05_localRemote() throws RemoteException {
        String str = STRINGS.get(8);
        Person person = bank.createPerson(str, str, str);

        Account account = person.createAccount(str);
        for (int amount : INTEGERS) {
            int prevAmount = account.getAmount();
            Person localPerson = bank.getPerson(str, false);

            account.setAmount(amount);
            Account localAccount = localPerson.getAccount(str);
            Assert.assertEquals(prevAmount, localAccount.getAmount());

            localAccount.setAmount((amount + 1) * 2);
            Assert.assertNotEquals(account.getAmount(), localAccount.getAmount());
        }
    }
}
