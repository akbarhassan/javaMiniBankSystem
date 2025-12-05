package com.ga.bank.account;

import com.ga.bank.debitCards.DebitCard;

public interface Operations {
    void deposit(double amount, DebitCard debitCard);
    void withdraw(double amount,DebitCard debitCard);
    void overdraft(double amount,DebitCard debitCard);
}
