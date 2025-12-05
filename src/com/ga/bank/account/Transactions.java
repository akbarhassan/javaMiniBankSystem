package com.ga.bank.account;

import com.ga.bank.User.User;
import com.ga.bank.debitCards.CardType;
import com.ga.bank.debitCards.DebitCard;
import com.ga.bank.debitCards.CardLimits;

import java.util.HashMap;
import java.util.Map;

public class Transactions implements Operations{
    private Account accountId;
    private CardType cardType;

    public Account getAccountId() {
        return accountId;
    }

    public Transactions(Account accountId, CardType cardType) {
        this.accountId = accountId;
        this.cardType = cardType;
    }

    public CardType getCardType() {
        return cardType;
    }



    public void deposit(double amount, DebitCard debitCard) {
        //TODO: modify the transaction record
    }

    public void withdraw(double amount, DebitCard debitCard) {

    }

    public void overdraft(double amount, DebitCard debitCard) {

    }
}
