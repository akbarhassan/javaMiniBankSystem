package com.ga.bank.app;

import com.ga.bank.util.PasswordEncryptor;
import com.ga.bank.storage.FileDBWriter;
import com.ga.bank.storage.FileDBReader;
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

        if(!verifiedEmail) {
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

        FileDBWriter authRegister = new FileDBWriter();
        boolean fileIsCreated = authRegister.writeUserCredentials(userName, fullName, email, hashedPassword, String.valueOf(Role.CUSTOMER), false);
        if (fileIsCreated) {
            // Confirm immediately
            File userFile = new File("data/users/" + userName + ".txt");
            if (userFile.exists()) {
                System.out.println("Account created successfully!");
                System.out.println("File is immediately available at: " + userFile.getAbsolutePath());
            } else {
                System.out.println("Account created, but file is not visible yet (this shouldn't happen).");
            }
        }


    }
}
