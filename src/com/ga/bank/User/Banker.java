package com.ga.bank.User;

public class Banker extends User {
    public Banker(String userName, String fullName, String email, String password) {
        super(userName, fullName, email, password, Role.BANKER);
    }

    public void viewUserDetails(String userName) {
    }

    public void deActivateAccount(String accountId) {
    }

    public void activateAccount(String accountId) {
    }

    public void listUserTransactions(String accountId) {
    }

}
