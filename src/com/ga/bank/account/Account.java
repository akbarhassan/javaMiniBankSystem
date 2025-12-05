package com.ga.bank.account;

import com.ga.bank.User.User;
import com.ga.bank.debitCards.DebitCard;

public class Account {
    private String accountId;
    private double balance;
    private boolean isActive;
    private User user;
    private DebitCard debitCard;
    private int overDraft;
    Transactions transactions;

    public Account(String accountId, double balance, boolean isActive, User user, DebitCard debitCard, int overDraft) {
        this.accountId = accountId;
        this.balance = balance;
        this.isActive = isActive;
        this.user = user;
        this.debitCard = debitCard;
        this.overDraft = overDraft;
        transactions = new Transactions(this.accountId,this.debitCard.getCardType());
    }

    public int getOverDraft() {
        return overDraft;
    }

    public void setOverDraft(int overDraft) {
        if (overDraft < 2) {
            this.overDraft = overDraft;
        } else {
            this.overDraft = overDraft;
            // TODO: un activate the account
        }
    }

    public String getAccountId() {
        return accountId;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(double amount) {
        if (getOverDraft() >= 2) {
            if (amount >= balance) {
                //TODO: reset overdraft unlock account
            }

        }
        this.balance = this.balance + amount;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DebitCard getDebitCard() {
        return debitCard;
    }

    public void setDebitCard(DebitCard debitCard) {
        this.debitCard = debitCard;
    }

    public void deposit(double amount, DebitCard debitCard, String toAccountId) {
        //TODO: modify the transaction record or create a new 1 if not exist
        //TODO: check if to own account
        //TODO: check deposited from account get date etc and check if he still have limit

        setBalance(amount);
        transactions.deposit(getBalance(),getDebitCard());



    }

}
