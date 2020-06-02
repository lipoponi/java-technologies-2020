import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import ru.ifmo.rain.tebloev.bank.Account;
import ru.ifmo.rain.tebloev.bank.Client;
import ru.ifmo.rain.tebloev.bank.Person;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientTest extends BaseTest {
    @Test
    public void test01_create() throws RemoteException {
        String firstName = STRINGS.get(6);
        String lastName = STRINGS.get(7);
        String passport = STRINGS.get(8);
        String subId = STRINGS.get(10);
        int amount = INTEGERS.get(6);

        Client.main(firstName, lastName, passport, subId, Integer.toString(amount));
        Person person = bank.getPerson(passport, true);

        Assert.assertNotNull(person);
        Assert.assertEquals(passport, person.getPassport());
        Assert.assertNotNull(person.getAccount(subId));
        Assert.assertEquals(amount, person.getAccount(subId).getAmount());
    }

    @Test
    public void test02_differentData() throws RemoteException {
        for (int i = 0; i < (STRINGS.size() + 2) / 3; i++) {
            String firstName = STRINGS.get((i * 3) % STRINGS.size());
            String lastName = STRINGS.get((i * 3 + 1) % STRINGS.size());
            String passport = STRINGS.get((i * 3 + 2) % STRINGS.size());
            String subId = STRINGS.get((i * 3 + 3) % STRINGS.size());
            int amount = INTEGERS.get(i % INTEGERS.size());

            Person person = bank.getPerson(passport, true);
            int prevAmount = 0;
            if (person != null) {
                Account account = person.getAccount(subId);
                prevAmount = account != null ? account.getAmount() : 0;
            }

            Client.main(firstName, lastName, passport, subId, Integer.toString(amount));

            if (person == null) {
                person = bank.getPerson(passport, true);
                Assert.assertNotNull(person);
                Assert.assertEquals(firstName, person.getFirstName());
                Assert.assertEquals(lastName, person.getLastName());
                Assert.assertEquals(passport, person.getPassport());
            }

            Account account = person.getAccount(subId);

            Assert.assertNotNull(account);
            Assert.assertEquals(prevAmount + amount, account.getAmount());
        }
    }

    @Test
    public void test03_multipleAccounts() throws RemoteException {
        String firstName = STRINGS.get(6);
        String lastName = STRINGS.get(7);
        String passport = STRINGS.get(8);

        Person person = bank.createPerson(firstName, lastName, passport);
        Map<String, Integer> balanceMap = new HashMap<>();

        for (int i = 0; i < 30; i++) {
            String subId = STRINGS.get(i % STRINGS.size());
            int amount = INTEGERS.get(i % INTEGERS.size());
            Account account = person.getAccount(subId);
            if (account != null) {
                balanceMap.put(subId, account.getAmount());
            }

            Client.main(firstName, lastName, passport, subId, Integer.toString(amount));

            account = person.getAccount(subId);
            Assert.assertEquals(balanceMap.getOrDefault(subId, 0) + amount, account.getAmount());
            balanceMap.put(subId, balanceMap.getOrDefault(subId, 0) + amount);
        }
    }
}
