package com.ga.bank.account;

import com.ga.bank.User.User;
import com.ga.bank.debitCards.CardType;
import com.ga.bank.debitCards.DebitCard;
import com.ga.bank.debitCards.CardLimits;
import com.ga.bank.fileDbMods.FileDBWriter;

import java.util.HashMap;
import java.util.Map;

public class Transactions implements Operations {
    FileDBWriter fileDBWriter = new FileDBWriter();

    public void deposit(double amount, DebitCard debitCard, Account account, String toAccount, double postBalance) {
        fileDBWriter.createTransaction(
                account.getUser().getUserName(),
                account.getAccountId(),
                OperationType.DEPOSIT,
                toAccount,
                amount,
                postBalance
        );
    }

    public void withdraw(double amount, DebitCard debitCard, Account account, String toAccount, double postBalance) {
        fileDBWriter.createTransaction(
                account.getUser().getUserName(),
                account.getAccountId(),
                OperationType.WITHDRAW,
                toAccount,
                amount,
                postBalance
        );
    }

    public void transfer(double amount, DebitCard debitCard, Account account, String toAccount, double postBalance) {
        fileDBWriter.createTransaction(
                account.getUser().getUserName(),
                account.getAccountId(),
                OperationType.TRANSFER,
                toAccount,
                amount,
                postBalance
        );
    }

    public void overdraft(double amount, DebitCard debitCard, Account account, String toAccount, double postBalance) {

    }
}
