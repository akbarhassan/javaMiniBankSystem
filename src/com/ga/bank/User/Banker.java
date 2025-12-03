package com.ga.bank.User;

public class Banker extends User {
    public Banker(String userName,String fullName, String email, String password) {
        super(userName,fullName, email, password, Role.BANKER);
    }
}
