package com.ga.bank.User;

public class Customer extends User {
    public Customer(String userName,String fullName, String email, String password) {
        super(userName,fullName, email, password, Role.CUSTOMER);
    }
}
