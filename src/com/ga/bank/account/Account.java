package com.ga.bank.account;

import com.ga.bank.User.User;
import com.ga.bank.debitCards.CardLimits;
import com.ga.bank.debitCards.CardType;
import com.ga.bank.debitCards.DebitCard;
import com.ga.bank.debitCards.Operations;
import com.ga.bank.fileDbMods.FileDBWriter;

public class Account {
    private String accountId;
    private double balance;
    private boolean isActive;
    private User user;
    private DebitCard debitCard;
    private int overDraft;
    Transactions transactions;
    FileDBWriter fileDBWriter = new FileDBWriter();

    public Account(String accountId, double balance, boolean isActive, User user, DebitCard debitCard, int overDraft) {
        this.accountId = accountId;
        this.balance = balance;
        this.isActive = isActive;
        this.user = user;
        this.debitCard = debitCard;
        this.overDraft = overDraft;
        transactions = new Transactions();
    }

    public int getOverDraft() {
        return overDraft;
    }

    public void setOverDraft(int overDraft) {
        if (overDraft >= 2) {
            //TODO: deactivate the account
            this.overDraft = overDraft;
        }
    }

    public String getAccountId() {
        return accountId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double amount, String toAccount) {
        double postBalance = this.balance;
        this.balance = this.balance + amount;
        fileDBWriter.modifyAccountBalance(getAccountId(), user.getUserName(), this.balance);
        transactions.deposit(
                amount,
                debitCard,
                this,
                toAccount,
                postBalance
        );
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

    public void deposit(double amount, String toAccount) {
        //TODO: modify the transaction record or create a new 1 if not exist
        //TODO: check if to own account
        //TODO: check deposited from account get date etc and check if he still have limit
        //TODO: check first to own account or no
        if (amount < 0d) {
            System.out.println("Amount must be greater than zero");
            return;
        }

        double cardLimit = CardLimits.getLimit(Operations.DepositLimitPerDayOwnAccount, debitCard.getCardType());
        if (amount >= cardLimit) {
            System.out.println("The limit is for this day, try again tomorrow");
            return;
        }
        if (toAccount == null) {
            toAccount = getAccountId();
        }

        setBalance(amount, toAccount);
        System.out.println("Current Balance: " + getBalance());

    }

}
