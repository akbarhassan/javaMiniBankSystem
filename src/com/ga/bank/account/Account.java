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
        } else if (operationType == OperationType.TRANSFER) {

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
        if (!isActive) {
            System.out.println("Account is blocked overdraft reached, balance is negative");
            System.out.println("Contact the banker to top up until positive balance to reset");
            return;
        }

        if (getBalance() < 0 && getBalance() + amount > 0) {
            setActive(true);
            setOverDraft(0);
        }
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

        if (amount > cardLimit) {
            System.out.println("The limit of deposit is hit for this day, try again tomorrow");
            return;
        }

        setBalance(amount, toAccount, OperationType.DEPOSIT);
        System.out.println("Current Balance: " + getBalance());
    }

    public void withdraw(double amount) {
        if (!isActive) {
            System.out.println("Account is blocked overdraft reached, balance is negative");
            return;
        }

        if (amount <= 0) {
            System.out.println("Amount must be greater than zero");
            return;
        }

        double cardLimit = 0d;
        double withdrawTransactionsAmount = fileDBReader.getDailyLimit(
                user.getUserName(),
                getAccountId(),
                OperationType.WITHDRAW
        );
        cardLimit = CardLimits.getLimit(Operations.WithdrawLimitPerDay, debitCard.getCardType());

        cardLimit -= withdrawTransactionsAmount;

        if (amount > cardLimit) {
            System.out.println("The limit of withdraw is hit for this day, try again tomorrow");
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
        System.out.printf("Amount Withdrawn Successfully %f", amount);
    }

    public void transfer(double amount, String toAccount) {
        if (!isActive) {
            System.out.println("Account is blocked overdraft reached, balance is negative");
            return;
        }

        if (amount <= 0) {
            System.out.println("Amount must be greater than zero");
            return;
        }

        if (amount > getBalance()) {
            System.out.println("Transfer amount should less than or equal the balance");
            return;
        }


        double cardLimit = 0d;

        double transferTransactionsAmount = fileDBReader.getDailyLimit(
                user.getUserName(),
                getAccountId(),
                OperationType.TRANSFER
        );

        if (toAccount == null) {
            toAccount = getAccountId();
        }

        if (toAccount.equals(getAccountId())) {
            System.out.println("Transferring from this account to this account? bruh");
            return;
        }

        //TODO: create a function that return if he is sending it to one of his accounts
        if (toAccount.equals("test")) {
            cardLimit = CardLimits.getLimit(Operations.TransferLimitPerDayOwnAccount, debitCard.getCardType());
        } else {
            cardLimit = CardLimits.getLimit(Operations.TransferLimitPerDay, debitCard.getCardType());
        }
        cardLimit -= transferTransactionsAmount;

        if (amount > cardLimit) {
            System.out.println("The limit of transfer is hit for this day, try again tomorrow");
            return;
        }

        setBalance(-amount, toAccount, OperationType.TRANSFER);
    }


}
