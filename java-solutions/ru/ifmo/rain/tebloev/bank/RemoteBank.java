package ru.ifmo.rain.tebloev.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final CheckedConsumer<Remote, RemoteException> exporter;
    private final ConcurrentMap<String, CommonAccount> accountMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AbstractPerson> personMap = new ConcurrentHashMap<>();

    RemoteBank(CheckedConsumer<Remote, RemoteException> exporter) {
        this.exporter = exporter;
    }

    @Override
    public CommonAccount createAccount(String id) throws RemoteException {
        final CommonAccount account = new CommonAccount(id);
        if (accountMap.putIfAbsent(id, account) == null) {
            Util.log("Creating account " + id);
            exporter.accept(account);
            return account;
        } else {
            return getAccount(id);
        }
    }

    @Override
    public CommonAccount getAccount(String id) {
        Util.log("Retrieving account " + id);
        return accountMap.get(id);
    }

    @Override
    public Person createPerson(String firstName, String lastName, String passport) throws RemoteException {
        final AbstractPerson person = new RemotePerson(this, firstName, lastName, passport);
        if (personMap.putIfAbsent(passport, person) == null) {
            Util.log("Creating person with passport " + passport);
            exporter.accept(person);
            return person;
        } else {
            return getPerson(passport, true);
        }
    }

    @Override
    public Person getPerson(String passport, boolean remote) {
        Util.log("Retrieving person with passport " + passport);

        AbstractPerson person = personMap.get(passport);

        return remote || person == null
                ? person
                : new LocalPerson(person);
    }
}
