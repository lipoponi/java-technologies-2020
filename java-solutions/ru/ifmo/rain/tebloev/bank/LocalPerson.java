package ru.ifmo.rain.tebloev.bank;

public final class LocalPerson extends AbstractPerson {
    LocalPerson(AbstractPerson person) {
        super(person);
    }

    @Override
    public Account createAccount(String subId) {
        String accountId = getAccountId(subId);
        return accounts.computeIfAbsent(subId, key -> new CommonAccount(accountId));
    }

    @Override
    public Account getAccount(String subId) {
        return accounts.get(subId);
    }
}
