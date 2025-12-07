package com.ga.bank.account;

import com.ga.bank.User.User;
import com.ga.bank.debitCards.CardLimits;
import com.ga.bank.debitCards.DebitCard;
import com.ga.bank.debitCards.Operations;
import com.ga.bank.fileDbMods.FileDBWriter;
import com.ga.bank.fileDbMods.FileDBReader;

import java.util.HashMap;

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

        if (toAccount == null) {
            toAccount = getAccountId();   // default: "own account"
        }

        double postBalance = this.balance;

        boolean isOwnAccount = toAccount.equals(getAccountId());

        if (OperationType.DEPOSIT.equals(operationType)) {
            if (isOwnAccount) {
                this.balance += amount; // own account deposit
                fileDBWriter.modifyAccountBalance(getAccountId(), user.getUserName(), this.balance);
            } else { // deposit to someone else's account
                String toUserName = fileDBReader.getOwnerOfAccount(toAccount);
                double toBalance = fileDBReader.getAccountBalance(toAccount, toUserName);
                fileDBWriter.modifyAccountBalance(toAccount, toUserName, toBalance + amount);
            }
        } else if (OperationType.TRANSFER.equals(operationType)) {
            this.balance += amount;
            fileDBWriter.modifyAccountBalance(getAccountId(), user.getUserName(), this.balance);

            String toUserName = fileDBReader.getOwnerOfAccount(toAccount);
            double toBalance = fileDBReader.getAccountBalance(toAccount, toUserName);
            fileDBWriter.modifyAccountBalance(toAccount, toUserName, toBalance + Math.abs(amount));
        } else if (OperationType.WITHDRAW.equals(operationType) || OperationType.OVERDRAFT.equals(operationType)) {
            this.balance += amount;
            fileDBWriter.modifyAccountBalance(getAccountId(), user.getUserName(), this.balance);
        }


        double absAmount = Math.abs(amount);

        // ---- WRITE TRANSACTION ----
        if (operationType == OperationType.DEPOSIT) {
            transactions.deposit(absAmount, debitCard, this, toAccount, postBalance);

        } else if (operationType == OperationType.WITHDRAW) {
            transactions.withdraw(absAmount, debitCard, this, toAccount, postBalance);

        } else if (operationType == OperationType.TRANSFER) {
            transactions.transfer(absAmount, debitCard, this, toAccount, postBalance);

        } else if (operationType == OperationType.OVERDRAFT) {
            transactions.overdraft(absAmount, debitCard, this, toAccount, postBalance);
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
        if (toAccount == null) {
            toAccount = getAccountId();
        }


        if (!getActive()) {
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


        if (fileDBReader.isOwnAccount(toAccount, user.getUserName())) {
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

        if (!getActive()) {
            System.out.println("Account is blocked. Overdraft limit reached. Contact banker.");
            return;
        }

        if (amount <= 0) {
            System.out.println("Amount must be greater than zero");
            return;
        }

        double usedToday = fileDBReader.getDailyLimit(
                user.getUserName(),
                getAccountId(),
                OperationType.WITHDRAW
        );

        double cardLimit = CardLimits.getLimit(Operations.WithdrawLimitPerDay, debitCard.getCardType());

        if (amount + usedToday > cardLimit) {
            System.out.println("Daily withdraw limit reached.");
            return;
        }

        double balance = getBalance();

        if (amount > balance) {

            int od = getOverDraft();

            if (od + 1 >= 2) {
                System.out.println("Final overdraft attempt reached.");

                if (amount > 100) amount = 100;

                setBalance(-amount, getAccountId(), OperationType.WITHDRAW);
                overDraft();
                setOverDraft(od + 1);
                setActive(false);
                return;
            }

            System.out.println("Using overdraft.");
            setOverDraft(od + 1);
            setBalance(-amount, getAccountId(), OperationType.WITHDRAW);
            overDraft();
            return;
        }

        setBalance(-amount, getAccountId(), OperationType.WITHDRAW);

        if (getOverDraft() != 0) {
            setOverDraft(0);
            setActive(true);
        }

        System.out.printf("Withdrawn %.2f%n", amount);
    }


    public void transfer(double amount, String toAccount) {
        if (!getActive()) {
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

        if (!fileDBReader.AccountExists(toAccount)) {
            System.out.println("Account does not exist");
            return;
        }

        String toUserName = fileDBReader.getOwnerOfAccount(toAccount);
        HashMap<String, String> toAccountStatus = fileDBReader.getToAccount(toAccount, toUserName);


        if (fileDBReader.isOwnAccount(toAccount, user.getUserName())) {
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
        if (toAccountStatus.containsKey("balance")) {
            double toAccountBalance = Double.parseDouble(toAccountStatus.get("balance"));
            if (amount - toAccountBalance >= 0) {
                fileDBWriter.modifyAccountOverDraft(toAccount, toUserName, 0, true);
            }
        }
        if (toAccountStatus.containsKey("balance")) {
            double toAccountBalance = Double.parseDouble(toAccountStatus.get("balance"));
            toAccountBalance = toAccountBalance + amount;
            fileDBWriter.modifyAccountBalance(toAccount, toUserName, toAccountBalance);
        }
    }

    public void overDraft() {
        double fee = 35;
        setBalance(-fee, getAccountId(), OperationType.OVERDRAFT);
    }


}
