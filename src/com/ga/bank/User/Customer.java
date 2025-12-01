package com.ga.bank.User;

public class Customer extends User {
    public Customer(String fullName, String email, String password) {
        super(fullName, email, password, Role.CUSTOMER);
    }
}
