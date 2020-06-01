package ru.ifmo.rain.tebloev.bank;

import java.io.Serializable;

public final class CommonAccount implements Account, Serializable {
    private final String id;
    private int amount = 0;

    CommonAccount(String id) {
        this.id = id;
    }

    CommonAccount(CommonAccount other) {
        this.id = other.getId();
        this.amount = other.getAmount();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public synchronized int getAmount() {
        Util.log("Getting amount of money for account " + id);
        return amount;
    }

    @Override
    public synchronized void setAmount(final int amount) {
        Util.log("Setting amount of money for account " + id);
        this.amount = amount;
    }
}
