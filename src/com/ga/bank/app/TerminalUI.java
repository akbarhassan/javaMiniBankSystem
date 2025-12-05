package com.ga.bank.app;

import com.ga.bank.util.PasswordEncryptor;
import com.ga.bank.fileDbMods.FileDBWriter;
import com.ga.bank.fileDbMods.FileDBReader;
import com.ga.bank.debitCards.CardType;

import com.ga.bank.User.Role;

import java.util.regex.*;
import java.io.File;
import java.util.Scanner;

public class TerminalUI {
    private Scanner scanner = new Scanner(System.in);

    public void start() {
        boolean running = true;

        while (running) {
            showMainMenu();

            String selectedAction = scanner.nextLine();

            switch (selectedAction) {
                case "1":
                    login();
                    break;
                case "2":
                    register();
                    break;
                case "0":
                    System.out.println("Have a good day!");
                    running = false;
                    break;
                default:
                    System.out.println("Choose a valid option");
            }
        }
    }

    private void showMainMenu() {
        System.out.println("\n=== Welcome to ACME Bank ===");
        System.out.println("1. Login");
        System.out.println("2. Register Account");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }


    private void login() {
        System.out.println("Enter your username");
        String userName = scanner.nextLine();

        System.out.println("Enter your password");
        String plainPassword = scanner.nextLine();

        FileDBReader login = new FileDBReader();
        if (login.authLogin(userName, plainPassword)) System.out.println("Login Successful");

    }

    private void register() {
        System.out.println("Enter a username");
        String userName = scanner.nextLine();

        System.out.println("Enter your full name");
        String fullName = scanner.nextLine();

        System.out.println("Enter your email");
        String email = scanner.nextLine();

        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        boolean verifiedEmail = email != null && pattern.matcher(email).matches();

        if (!verifiedEmail) {
            return;
        }

        System.out.println("Enter your password");
        String plainPassword = scanner.nextLine();

        PasswordEncryptor encryptor = new PasswordEncryptor();
        String hashedPassword;
        try {
            hashedPassword = encryptor.encrypt(plainPassword);
        } catch (Exception e) {
            System.out.println("Error encrypting password: " + e.getMessage());
            return; // stop registration if hashing fails
        }
        System.out.println("Select Card Type: by entering 1 or 2 or 3");
        int i = 1;
        for (CardType type : CardType.values()) {
            System.out.println(i + ". " + type);
            i++;
        }
        System.out.print("Your choice: ");
        String input = scanner.nextLine();
        if (!input.matches("[1-3]")) {
            System.out.println("Invalid choice!");
            return; // or ask again
        }
        CardType cardType = CardType.values()[Integer.parseInt(input) - 1];

        System.out.println("Enter your balance");
        String balanceInput = scanner.nextLine();

        double balance;

        try {
            balance = Double.parseDouble(balanceInput);
        } catch (NumberFormatException e) {
            System.out.println("Invalid balance! Must be a number.");
            return; // or loop again
        }

        FileDBWriter authRegister = new FileDBWriter();
        boolean fileIsCreated = authRegister.writeUserCredentials(userName, fullName, email, hashedPassword, String.valueOf(Role.CUSTOMER), false);
        if (fileIsCreated) {
            boolean createAccount = authRegister.writeUserAccount(
                    userName,
                    cardType,
                    balance,
                    0
                    , true
            );
            // Confirm immediately
            if (createAccount) {
                System.out.println("Account created successfully!");
            } else {
                System.out.println("Account created, but file is not visible yet (this shouldn't happen).");
            }
        }


    }
}
