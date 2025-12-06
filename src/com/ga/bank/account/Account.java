package com.ga.bank.account;

import com.ga.bank.User.User;
import com.ga.bank.debitCards.CardLimits;
import com.ga.bank.debitCards.DebitCard;
import com.ga.bank.debitCards.Operations;
import com.ga.bank.fileDbMods.FileDBWriter;
import com.ga.bank.fileDbMods.FileDBReader;

public class Account {
    private String accountId;
    private double balance;
    private boolean isActive;
    private User user;
    private DebitCard debitCard;
    private int overDraft;
    Transactions transactions;
    FileDBWriter fileDBWriter = new FileDBWriter();
    FileDBReader fileDBReader = new FileDBReader();
    Operations operations;

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
            setActive(false);
            fileDBWriter.modifyAccountOverDraft(
                    getAccountId(),
                    user.getUserName(),
                    2,
                    false
            );
            return;
        }
        this.overDraft = overDraft;
        fileDBWriter.modifyAccountOverDraft(
                getAccountId(),
                user.getUserName(),
                overDraft,
                true
        );
    }

    public String getAccountId() {
        return accountId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double amount, String toAccount, OperationType operationType) {
        double postBalance = this.balance;
        this.balance = this.balance + amount;
        fileDBWriter.modifyAccountBalance(getAccountId(), user.getUserName(), this.balance);
        if (toAccount == null) {
            toAccount = getAccountId();
        }
        if (operationType == OperationType.DEPOSIT) {
            transactions.deposit(
                    amount,
                    debitCard,
                    this,
                    toAccount,
                    postBalance
            );
        } else if (operationType == OperationType.WITHDRAW) {
            transactions.withdraw(
                    amount,
                    debitCard,
                    this,
                    toAccount,
                    postBalance);
        }
    }

    public boolean getActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public User getUser() {
        return user;
    }


    public void deposit(double amount, String toAccount) {
        double cardLimit = 0d;
        double depositTransactionsAmount = fileDBReader.getDailyLimit(
                user.getUserName(),
                getAccountId(),
                OperationType.DEPOSIT
        );

        if (toAccount == null) {
            toAccount = getAccountId();
        }

        if (toAccount.equals(getAccountId())) {
            cardLimit = CardLimits.getLimit(Operations.DepositLimitPerDayOwnAccount, debitCard.getCardType());
        } else {
            cardLimit = CardLimits.getLimit(Operations.DepositLimitPerDay, debitCard.getCardType());
        }
        cardLimit -= depositTransactionsAmount;

        if (amount < 0d) {
            System.out.println("Amount must be greater than zero");
            return;
        }

        if (amount >= cardLimit) {
            System.out.println("The limit is for this day, try again tomorrow");
            return;
        }

        setBalance(amount, toAccount, OperationType.DEPOSIT);
        System.out.println("Current Balance: " + getBalance());

        //TODO: balance is negative and amount is more reset overdraft

    }

    public void withdraw(double amount) {

        if (amount <= 0) {
            System.out.println("Amount must be greater than zero");
            return;
        }

        double balance = getBalance();
        if (amount > balance) {
            if (getOverDraft() + 1 >= 2) {
                System.out.println("Overdraft limit reached! Maximum allowed overdraft attempts is 2.");

                if (amount > 100) {
                    amount = 100;
                }

                setBalance(-amount, getAccountId(), OperationType.WITHDRAW);
                setActive(false);
                setOverDraft(getOverDraft() + 1);
                return;
            }
            System.out.println("Using overdraft.");
            setOverDraft(getOverDraft() + 1);
            setBalance(-amount, getAccountId(), OperationType.WITHDRAW);
            return;
        }

        setBalance(-amount, getAccountId(), OperationType.WITHDRAW);
        if (getOverDraft() != 0) {
            setOverDraft(0);
            setActive(true);
        }
    }


}
