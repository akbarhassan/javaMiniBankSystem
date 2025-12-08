package com.ga.bank.app;

import com.ga.bank.User.Banker;
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
    FileDBReader fileDBReader = new FileDBReader();

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
                        } else {
                            if (currentUser instanceof Banker) {
                                bankerOperations();
                            }
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

        if (fileDBReader.authLogin(userName, plainPassword)) {
            System.out.println("Login Successful");
            currentUser = fileDBReader.getCurrentUser(userName);
            if (currentUser instanceof Customer) {
                accountList = fileDBReader.userAccounts(userName);
                String userAccountId = setAccountId();
                currentAccount = fileDBReader.getCurrentUserAccount(userName, userAccountId, currentUser);
            }
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

        while (loggedIn) {
            System.out.println("\n=== Available Operations ===");
            showOperations();
            String operation = scanner.nextLine();

            switch (operation) {
                case "1":
                    System.out.println("Deposit selected");
                    double amount = getDeposit("Deposit");
                    String toAccountId = sendToAccount("Deposit");
                    currentAccount.deposit(amount, toAccountId);

                    break;
                case "2":
                    System.out.println("Withdraw selected");
                    double amount1 = getDeposit("Withdraw");
                    currentAccount.withdraw(amount1);
                    break;
                case "3":
                    System.out.println("Transfer selected");
                    double amount2 = getDeposit("Transfer");
                    String toAccountId1 = sendToAccount("Transfer");
                    currentAccount.transfer(amount2, toAccountId1);
                    break;
                case "4":
                    System.out.println("Show current balance selected");
                    System.out.println("Current Balance: " + currentAccount.getBalance());
                    break;
                case "5":
                    createAccount();
                    System.out.println("To use this new account logout and login again");
                case "0":
                    System.out.println("Logging out...");
                    loggedIn = false;
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
        System.out.println("5. Create an account");
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

    public double getDeposit(String operation) {
        System.out.print("Amount to : " + operation + ": ");
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

    public String sendToAccount(String operation) {
        System.out.printf("Do you want to %s to one of your accounts, another person's account, or enter manually?\n", operation);
        System.out.println("1. Yes (own accounts)");
        System.out.println("2. No (other users)");
        System.out.println("3. Enter Manual");
        System.out.println("4. To Current Account");

        int choice = 0;

        while (true) {
            String input = scanner.nextLine();

            try {
                choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= 4) break;
            } catch (NumberFormatException ignored) {
            }

            System.out.println("Invalid choice. Enter 1, 2, or 3.");
        }


        if (choice == 3) {
            while (true) {
                System.out.print("Enter the account ID manually: ");
                String manualAcc = scanner.nextLine().trim();

                if (fileDBReader.AccountExists(manualAcc)) {
                    return manualAcc;
                }
                System.out.println("Account does not exist. Try again.");
            }
        }

        if (choice == 4) {
            return currentAccount.getAccountId();
        }


        if (choice == 1) {
            List<String> ownUserAccounts = fileDBReader.getCurrentUserOtherAccounts(
                    currentAccount.getAccountId(),
                    currentUser.getUserName()
            );

            if (ownUserAccounts.isEmpty()) {
                System.out.println("No other accounts found under your name.");
                return null;
            }

            System.out.println("Select which of your accounts to send to:");
            for (int i = 0; i < ownUserAccounts.size(); i++) {
                System.out.println((i + 1) + ". Account ID: " + ownUserAccounts.get(i));
            }

            while (true) {
                System.out.printf("Enter your choice (1-%d): ", ownUserAccounts.size());
                String input = scanner.nextLine();

                try {
                    int accChoice = Integer.parseInt(input);
                    if (accChoice >= 1 && accChoice <= ownUserAccounts.size()) {
                        return ownUserAccounts.get(accChoice - 1);
                    }
                } catch (NumberFormatException ignored) {
                }

                System.out.println("Invalid choice. Try again.");
            }
        }

        List<String> othersAccounts = fileDBReader.toTransferToAccounts(currentUser.getUserName());

        if (othersAccounts.isEmpty()) {
            System.out.println("No other accounts available.");
            return null;
        }

        System.out.println("Select which account to send to:");
        for (int i = 0; i < othersAccounts.size(); i++) {
            System.out.println((i + 1) + ". Account ID: " + othersAccounts.get(i));
        }

        while (true) {
            System.out.printf("Enter your choice (1-%d): ", othersAccounts.size());
            String input = scanner.nextLine();

            try {
                int accChoice = Integer.parseInt(input);
                if (accChoice >= 1 && accChoice <= othersAccounts.size()) {
                    return othersAccounts.get(accChoice - 1);
                }
            } catch (NumberFormatException ignored) {
            }

            System.out.println("Invalid choice. Try again.");
        }
    }


    public void createAccount() {
        System.out.println("Creating a new account.");
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
            return;
        }

        FileDBWriter createAccount = new FileDBWriter();
        boolean isCreated = createAccount.writeUserAccount(
                currentUser.getUserName(),
                cardType,
                balance,
                0
                , true
        );

        if (isCreated) {
            System.out.println("Account created successfully!");
        } else {
            System.out.println("Account created, but file is not visible yet (this shouldn't happen).");
        }

    }

    public void bankerOperationsTemplate() {
        System.out.println("Select operations");
        System.out.println("1. List customer details");
        System.out.println("2. List customer account details");
        System.out.println("3. Create Banker");
        System.out.println("4. List customer transaction details");
        System.out.println("5. Activate customer account");
        System.out.println("6. Deactivate customer account");
        System.out.println("7. Exit");

    }

    public void bankerOperations() {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n=== Available Operations ===");
            bankerOperationsTemplate();
            String operation = scanner.nextLine();

            switch (operation) {
                case "1":
                    System.out.println("Listing user details selected");
                    enterCustomerUserName();                    break;
                case "2":
                    System.out.println("Listing customer account details");
                    break;
                case "3":
                    System.out.println("To create banker account fill the following");
                    break;
                case "4":
                    System.out.println("List customer transactions");
                    break;
                case "5":
                    System.out.println("Enter Customer account to activate");
                    break;
                case "6":
                    System.out.println("Enter Customer account to deactivate");
                    break;
                case "7":
                    System.out.println("Exiting Have fun !");
                    loggedIn=false;
                    break;
                default:
                    System.out.println("Choose a valid operation");

            }
        }

    }


    public void enterCustomerUserName() {
        System.out.println("Write the user name of the customer:");

        while (true) {
            String input = scanner.nextLine();

            if (!fileDBReader.userExists(input)) {
                System.out.println("User does not exist");
            } else {
                User user = fileDBReader.getCurrentUser(input);
                System.out.println("Full Name : " + user.getFullName());
                System.out.println("Email     : " + user.getEmail());
                System.out.println("Role      : " + user.getRole());
                return; // exit method
            }
        }
    }


}
