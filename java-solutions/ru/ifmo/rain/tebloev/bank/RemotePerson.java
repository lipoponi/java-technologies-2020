package ru.ifmo.rain.tebloev.bank;

import java.rmi.RemoteException;

public final class RemotePerson extends AbstractPerson {
    private final transient RemoteBank bank;

    RemotePerson(RemoteBank bank, String firstName, String lastName, String passport) {
        super(firstName, lastName, passport);
        this.bank = bank;
    }

    @Override
    public Account createAccount(String subId) throws RemoteException {
        String accountId = getAccountId(subId);
        CommonAccount account = bank.createAccount(accountId);
        accounts.putIfAbsent(subId, account);

        return account;
    }

    @Override
    public Account getAccount(String subId) {
        Account account = accounts.get(subId);
        if (account == null) {
            String accountId = getAccountId(subId);
            account = bank.getAccount(accountId);
        }

        return account;
    }
}
