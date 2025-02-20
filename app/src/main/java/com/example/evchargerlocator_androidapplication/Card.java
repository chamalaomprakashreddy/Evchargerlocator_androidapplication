package com.example.evchargerlocator_androidapplication;

public class Card {
    private String cardNumber, expiryDate, cardHolderName;

    public Card() {}

    public Card(String cardNumber, String expiryDate, String cardHolderName) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cardHolderName = cardHolderName;
    }

    public String getCardNumber() { return cardNumber; }
    public String getExpiryDate() { return expiryDate; }
    public String getCardHolderName() { return cardHolderName; }
}