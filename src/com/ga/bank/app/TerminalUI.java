package com.ga.bank.app;

import com.ga.bank.User.Customer;
import com.ga.bank.User.User;
import com.ga.bank.account.Account;
import com.ga.bank.util.PasswordEncryptor;
import com.ga.bank.fileDbMods.FileDBWriter;
import com.ga.bank.fileDbMods.FileDBReader;
import com.ga.bank.debitCards.CardType;

import com.ga.bank.User.Role;

import java.util.List;
import java.util.regex.*;
import java.util.Scanner;

public class TerminalUI {
    private Scanner scanner = new Scanner(System.in);
    private User currentUser;
    private Account currentAccount;
    private List<String> accountList;

    public void start() {
        boolean running = true;

        while (running) {
            showMainMenu();

            String selectedAction = scanner.nextLine();

            switch (selectedAction) {
                case "1":
                    if (login()) {
                        if (currentUser instanceof Customer) {
                            operations();
                        }
                    }
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


    private boolean login() {
        System.out.println("Enter your username");
        String userName = scanner.nextLine();

        System.out.println("Enter your password");
        String plainPassword = scanner.nextLine();

        FileDBReader login = new FileDBReader();
        if (login.authLogin(userName, plainPassword)) {
            System.out.println("Login Successful");
            //todo: pick user data
            currentUser = login.getCurrentUser(userName);
            accountList = login.userAccounts(userName);
            String userAccountId = setAccountId();
            currentAccount = login.getCurrentUserAccount(userName, userAccountId, currentUser);

            return true;
        } else {
            System.out.println("Invalid credentials");
            return false;
        }
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

    public void operations() {
        boolean loggedIn = true;
        //TODO: create User Object here?
        //TODO: create account object here?

        while (loggedIn) {
            System.out.println("\n=== Available Operations ===");
            showOperations();
            String operation = scanner.nextLine();

            switch (operation) {
                case "1":
                    System.out.println("Deposit selected");
                    // TODO: deposit logic
                    double amount = getDeposit();
                    currentAccount.deposit(amount, null);

                    break;
                case "2":
                    System.out.println("Withdraw selected");
                    // TODO: withdraw logic
                    break;
                case "3":
                    System.out.println("Transfer selected");
                    // TODO: transfer logic
                    break;
                case "4":
                    System.out.println("Show current balance selected");
                    System.out.println("Current Balance: " + currentAccount.getBalance());
                    // TODO: transfer logic
                    break;
                case "0":
                    System.out.println("Logging out...");
                    loggedIn = false; // exit operations loop
                    break;
                default:
                    System.out.println("Choose a valid operation");
            }
        }
    }

    private void showOperations() {
        System.out.println("Select operation: ");
        System.out.println("1. Deposit");
        System.out.println("2. Withdraw");
        System.out.println("3. Transfer");
        System.out.println("4. Show current balance");
        System.out.println("0. Exit");
    }

    public String setAccountId() {
        System.out.println("Select Which Account you want to use for the operations");

        for (int i = 0; i < accountList.size(); i++) {
            System.out.println((i + 1) + ". Account ID: " + accountList.get(i));
        }

        while (true) {
            System.out.print("Enter your choice (1-" + accountList.size() + "): ");
            String input = scanner.nextLine();

            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= accountList.size()) {
                    return accountList.get(choice - 1);
                }
            } catch (NumberFormatException ignored) {
            }

            System.out.println("Invalid choice. Try again.");
        }
    }

    public double getDeposit() {
        System.out.print("Amount to deposit: ");
        double deposit = 0;
        boolean verifyDeposit = true;

        while (verifyDeposit) {
            String input = scanner.nextLine();
            try {
                deposit = Double.parseDouble(input);
                if (deposit <= 0) {
                    System.out.print("Amount must be greater than 0. Try again: ");
                } else {
                    verifyDeposit = false; // valid input, exit loop
                }
            } catch (NumberFormatException e) {
                System.out.print("Invalid input! Please enter a number: ");
            }
        }

        return deposit;
    }

}
