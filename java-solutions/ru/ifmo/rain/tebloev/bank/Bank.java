package ru.ifmo.rain.tebloev.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it is not already exists.
     *
     * @param id account id
     * @return created or existing account.
     */
    Account createAccount(String id) throws RemoteException;

    /**
     * Returns account by identifier.
     *
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exists.
     */
    Account getAccount(String id) throws RemoteException;

    /**
     * Creates a new person with specified data or returns existing one.
     *
     * @param firstName First name of person
     * @param lastName  Last name of person
     * @param passport  Unique identification number
     * @return created or existing person
     */
    Person createPerson(String firstName, String lastName, String passport) throws RemoteException;

    /**
     * Returns person by passport.
     *
     * @param passport Unique identification number
     * @param remote   {@code true} if instance should be remote, {@code false} if local
     * @return person with specified passport or {@code null} if person with that passport doesn't exists
     */
    Person getPerson(String passport, boolean remote) throws RemoteException;
}
