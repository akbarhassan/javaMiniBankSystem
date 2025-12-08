package com.ga.bank.User;

import com.ga.bank.fileDbMods.FileDBReader;
import com.ga.bank.fileDbMods.FileDBWriter;

public class Banker extends User {
    FileDBWriter fileDBWriter = new FileDBWriter();
    FileDBReader fileDBReader = new FileDBReader();

    public Banker(String userName, String fullName, String email, String password) {
        super(userName, fullName, email, password, Role.BANKER);
    }

    public void viewUserDetails(String userName) {
        User user = fileDBReader.getCurrentUser(userName);
        System.out.println("Full Name : " + user.getFullName());
        System.out.println("Email     : " + user.getEmail());
        System.out.println("Role      : " + user.getRole());
    }

    public void deActivateAccount(String accountId, String userName) {
        fileDBWriter.modifyAccountOverDraft(accountId, userName, 0, false);
    }

    public void activateAccount(String accountId, String userName) {
        fileDBWriter.modifyAccountOverDraft(accountId, userName, 0, true);

    }

    public void listUserTransactions(String accountId) {
    }

    public void createBankerAccount(String userName, String fullName, String email, String password) {
        fileDBWriter.writeUserCredentials(userName, fullName, email, password, String.valueOf(Role.BANKER), false);
    }

}
