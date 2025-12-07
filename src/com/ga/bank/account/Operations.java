package com.ga.bank.account;

import com.ga.bank.debitCards.DebitCard;

public interface Operations {
    void deposit(double amount, DebitCard debitCard, Account account, String toAccount, double postBalance);

    void withdraw(double amount, DebitCard debitCard, Account account, String toAccount, double postBalance);

    void overdraft(double amount, DebitCard debitCard, Account account, String toAccount, double postBalance);

    void transfer(double amount, DebitCard debitCard, Account account, String toAccount, double postBalance);

}
