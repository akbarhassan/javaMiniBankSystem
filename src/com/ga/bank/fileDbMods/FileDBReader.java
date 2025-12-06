package com.ga.bank.fileDbMods;

import com.ga.bank.User.Customer;
import com.ga.bank.User.Role;
import com.ga.bank.User.User;
import com.ga.bank.account.Account;
import com.ga.bank.account.OperationType;
import com.ga.bank.debitCards.CardType;
import com.ga.bank.debitCards.DebitCard;
import com.ga.bank.util.PasswordEncryptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileDBReader {
    private final String authPath = "data/users";
    private final String accountsFolder = "data/accounts"; // adjust path to your accounts folder
    private final String transactionsPath = "data/transactions";

    public boolean authLogin(String userName, String plainPassword) {
        PasswordEncryptor encryptor = new PasswordEncryptor();
        File authFile = new File(authPath + "/" + userName.toLowerCase() + ".txt");

        int counter = 0;
        boolean locked = false;
        String hashedPassword = null;

        // Read the file and extract login data
        try (Scanner myReader = new Scanner(authFile)) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();

                if (data.toLowerCase().startsWith("locked:")) {
                    locked = Boolean.parseBoolean(data.split(":")[1]);
                } else if (data.toLowerCase().startsWith("loginattempt:")) {
                    counter = Integer.parseInt(data.split(":")[1]);
                } else if (data.toLowerCase().startsWith("password:")) {
                    hashedPassword = data.split(":", 2)[1];
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred. " + e.getMessage());
            return false;
        }

        if (locked) {
            System.out.println("Account is locked");
            return false;
        }

        boolean isCorrectPassword = encryptor.compare(plainPassword, hashedPassword);

        if (!isCorrectPassword) {
            FileDBWriter writer = new FileDBWriter();
            writer.increaseLoginCounter(counter, userName);
            return false;
        }

        FileDBWriter writer = new FileDBWriter();
        writer.resetCounter(userName);
        return true;
    }

    public User getCurrentUser(String userName) {
        File authFile = new File(authPath + "/" + userName.toLowerCase() + ".txt");
        String role = null;
        String fullName = null;
        String email = null;
        String password = null;
        String username = null;


        // use Customer Class
        try (Scanner myReader = new Scanner(authFile)) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();

                if (data.toLowerCase().startsWith("fullname:")) {
                    fullName = data.split(":")[1];
                } else if (data.toLowerCase().startsWith("email:")) {
                    email = data.split(":")[1];
                } else if (data.toLowerCase().startsWith("password:")) {
                    password = data.split(":", 2)[1];
                } else if (data.toLowerCase().startsWith("role:")) {
                    role = data.split(":", 2)[1];
                } else if (data.toLowerCase().startsWith("username")) {
                    username = data.split(":")[1];
                }
            }
            if (role != null) {
                Role roleEnum = Role.valueOf(role.toUpperCase());

                if (roleEnum == Role.CUSTOMER) {
                    Customer customer = new Customer(
                            username,
                            fullName,
                            email,
                            password
                    );
                    return customer;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred. " + e.getMessage());
        }

        return null;
    }

    public List<String> userAccounts(String userName) {
        List<String> accounts = new ArrayList<>();
        File folder = new File(accountsFolder);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Accounts folder does not exist");
            return accounts;
        }
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    if (fileName.toLowerCase().endsWith("-" + userName.toLowerCase() + ".txt")) {
                        String accountId = fileName.split("-")[0];
                        accounts.add(accountId);
                    }
                }
            }
        }
        return accounts;
    }

    public Account getCurrentUserAccount(String userName, String accountId, User currentUser) {

        File accountFile = new File(accountsFolder + "/" + accountId + "-" + userName + ".txt");

        double balance = 0d;
        boolean isActive = false;
        int overDraft = 0;

        String cardType = null;
        String cardNumber = null;

        try (Scanner myReader = new Scanner(accountFile)) {

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine().trim();

                if (data.toLowerCase().startsWith("balance:")) {
                    balance = Double.parseDouble(data.split(":", 2)[1]);

                } else if (data.toLowerCase().startsWith("isactive:")) {
                    isActive = Boolean.parseBoolean(data.split(":", 2)[1]);

                } else if (data.toLowerCase().startsWith("overdraft:")) {
                    overDraft = Integer.parseInt(data.split(":", 2)[1]);

                } else if (data.toLowerCase().startsWith("cardtype:")) {
                    cardType = data.split(":", 2)[1].trim();

                } else if (data.toLowerCase().startsWith("cardnumber:")) {
                    cardNumber = data.split(":", 2)[1].trim();
                }
            }

            DebitCard debitCard = null;
            CardType cardTypeEnum = null;

            if (cardType != null) {
                cardTypeEnum = CardType.valueOf(cardType.toUpperCase());
            }

            if (cardTypeEnum != null && cardNumber != null) {
                debitCard = new DebitCard(cardTypeEnum, cardNumber);
            }
            return new Account(
                    accountId,
                    balance,
                    isActive,
                    currentUser,
                    debitCard,
                    overDraft
            );

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred. " + e.getMessage());
        }

        return null;
    }

    public double getDailyLimit(String userName, String accountId, OperationType operationType) {
        String filePath = transactionsPath + "/" + accountId + "-" + userName + ".txt";
        double limit = 0d;

        File transactionFile = new File(filePath);

        List<String> todaysTransactions = new ArrayList<>();
        LocalDate today = LocalDate.now();

        try (Scanner myReader = new Scanner(transactionFile)) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine().trim();
                if (data.isEmpty()) continue;

                String[] transactionData = data.split(",");
                if (transactionData.length < 6) continue;

                String createdAt = transactionData[1];
                String operation = transactionData[2];
                if (createdAt.startsWith(today.toString()) &&
                        operation.equalsIgnoreCase(operationType.name())
                ) {
                    todaysTransactions.add(data);
                }
            }

            for (String transactionData : todaysTransactions) {
                String[] data = transactionData.split(",");
                limit += Double.parseDouble(data[4]);
            }

            return limit;

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred. " + e.getMessage());
        }

        return 0d;
    }

}
