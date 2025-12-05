package com.ga.bank.account;

import com.ga.bank.User.User;
import com.ga.bank.debitCards.DebitCard;

public class Transactions implements Operations{
    private String accountId;
    private User user;
    private double balance;
    private boolean isActive;
    private DebitCard debitCard;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public DebitCard getDebitCard() {
        return debitCard;
    }

    public void setDebitCard(DebitCard debitCard) {
        this.debitCard = debitCard;
    }


    public void deposit(double amount, DebitCard debitCard) {

    }

    public void withdraw(double amount, DebitCard debitCard) {

    }

    public void overdraft(double amount, DebitCard debitCard) {

    }
}
