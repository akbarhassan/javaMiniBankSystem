package com.ga.bank.User;

public class Banker extends User {
    public Banker(String fullName, String email, String password) {
        super(fullName, email, password, Role.BANKER);
    }
}
