package ru.ifmo.rain.tebloev.bank.server;

import ru.ifmo.rain.tebloev.bank.common.Person;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

abstract class AbstractPerson implements Person, Serializable {
    protected final String firstName;
    protected final String lastName;
    protected final String passport;
    protected final ConcurrentMap<String, CommonAccount> accounts;

    protected AbstractPerson(String firstName, String lastName, String passport) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passport = passport;
        this.accounts = new ConcurrentHashMap<>();
    }

    protected AbstractPerson(AbstractPerson other) {
        this.firstName = other.firstName;
        this.lastName = other.lastName;
        this.passport = other.passport;
        this.accounts = other.accounts.entrySet().stream()
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey, entry -> new CommonAccount(entry.getValue())));
    }

    protected String getAccountId(String subId) {
        return String.format("%s:%s", passport, subId);
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getPassport() {
        return passport;
    }
}
