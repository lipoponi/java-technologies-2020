package ru.ifmo.rain.tebloev.bank.client;

import ru.ifmo.rain.tebloev.bank.common.Account;
import ru.ifmo.rain.tebloev.bank.common.Bank;
import ru.ifmo.rain.tebloev.bank.common.Person;
import ru.ifmo.rain.tebloev.bank.common.Util;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Objects;
import java.util.stream.Stream;

public class Client {
    public static void main(final String... args) throws RemoteException {
        final Bank bank;
        try {
            Registry registry = LocateRegistry.getRegistry(Util.DEFAULT_RMI_PORT);
            bank = (Bank) registry.lookup("bank");
        } catch (NotBoundException e) {
            Util.handleException("Bank is not bound", e);
            return;
        }

        if (args.length != 5) {
            Util.handleException("Usage: Client firstName lastName passport subId amount");
            return;
        }

        if (Stream.of(args).anyMatch(Objects::isNull)) {
            Util.handleException("Null arguments not supported");
            return;
        }

        if (!args[4].matches("^-?\\d+$")) {
            Util.handleException("Amount should be number");
            return;
        }

        String firstName = args[0];
        String lastName = args[1];
        String passport = args[2];
        String subId = args[3];
        int change = Integer.parseInt(args[4]);

        Person person = bank.getPerson(passport, true);
        if (person == null) {
            person = bank.createPerson(firstName, lastName, passport);
        }

        Account account = person.getAccount(subId);
        if (account == null) {
            account = person.createAccount(subId);
        }

        Util.log("Account id: " + account.getId());
        Util.log("Money: " + account.getAmount());
        Util.log("Operating money");
        account.setAmount(account.getAmount() + change);
        Util.log("Money: " + account.getAmount());
    }
}
