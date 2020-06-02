package ru.ifmo.rain.tebloev.bank.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Person extends Remote {
    /**
     * Returns first name of person.
     *
     * @return first name
     */
    String getFirstName() throws RemoteException;

    /**
     * Returns last name of person.
     *
     * @return last name
     */
    String getLastName() throws RemoteException;

    /**
     * Returns passport of person.
     *
     * @return passport
     */
    String getPassport() throws RemoteException;

    /**
     * Creates account with specified {@code subId}.
     *
     * @param subId part of account id which format is {@code passport:subId}
     * @return created or existing Account with specified {@code subId}
     */
    Account createAccount(String subId) throws RemoteException;

    /**
     * Returns account with specified {@code subId}.
     *
     * @param subId part of account id which format is {@code passport:subId}
     * @return account with specified {@code subId} or {@code null} if account with that {@code subId} doesn't exist
     */
    Account getAccount(String subId) throws RemoteException;
}
