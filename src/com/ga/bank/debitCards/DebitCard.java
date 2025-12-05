package com.ga.bank.debitCards;

public class DebitCard {
    private CardType cardType;
    private String cardNumber;

    public DebitCard(CardType cardType, String cardNumber) {
        this.cardType = cardType;
        this.cardNumber = cardNumber;
    }


    public String getCardNumber() {
        return cardNumber;
    }


    public CardType getCardType() {
        return cardType;
    }


}
