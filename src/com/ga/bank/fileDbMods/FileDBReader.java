package com.ga.bank.fileDbMods;

import com.ga.bank.User.Banker;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
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
                } else if (roleEnum == Role.BANKER) {
                    Banker banker = new Banker(
                            userName,
                            fullName,
                            email,
                            password
                    );
                    return banker;
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


    public boolean AccountExists(String accountId) {
        File folder = new File(accountsFolder);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Accounts folder does not exist");
            return false;
        }

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    if (fileName.matches("^" + accountId + "-.*")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isOwnAccount(String toAccountId, String userName) {
        File folder = new File(accountsFolder);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Accounts folder does not exist");
            return false;
        }
        String expectedFileName = toAccountId + "-" + userName + ".txt";
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().equalsIgnoreCase(expectedFileName)) {
                    return true;
                }
            }
        }

        return false;
    }

    // return list of all accounts except current user account object
    public List<String> toTransferToAccounts(String userName) {
        List<String> canTransferToAccounts = new ArrayList<>();
        File folder = new File(accountsFolder);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Accounts folder does not exist");
            return canTransferToAccounts;
        }

        File[] files = folder.listFiles();
        if (files == null) return canTransferToAccounts;
        String targetUser = userName.toLowerCase();
        for (File file : files) {
            if (!file.isFile()) continue;
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith("-" + targetUser + ".txt")) {
                continue;
            }
            String accountNumber = file.getName().split("-")[0];
            canTransferToAccounts.add(accountNumber);
        }

        return canTransferToAccounts;
    }


    public List<String> getCurrentUserOtherAccounts(String accountId, String userName) {
        List<String> otherAccounts = new ArrayList<>();
        File folder = new File(accountsFolder);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Accounts folder does not exist");
            return otherAccounts;
        }

        File[] files = folder.listFiles();
        if (files == null) return otherAccounts;

        for (File file : files) {
            if (!file.isFile()) continue;
            String fileName = file.getName().toLowerCase();
            String user = userName.toLowerCase();
            if (!fileName.endsWith("-" + user + ".txt")) continue;
            String[] parts = file.getName().split("-");
            if (parts.length < 2) continue;
            String accNumber = parts[0];
            if (accNumber.equals(accountId)) continue;
            otherAccounts.add(accNumber);
        }

        return otherAccounts;
    }

    public String getOwnerOfAccount(String accountId) {
        File folder = new File(accountsFolder);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Accounts folder does not exist");
            return null;
        }

        File[] files = folder.listFiles();
        if (files == null) return null;

        for (File file : files) {
            if (file.isFile()) {
                String fileName = file.getName();

                // Example fileName: 604290-hasan.txt
                if (fileName.startsWith(accountId + "-") && fileName.endsWith(".txt")) {
                    // Remove "{accountId}-" and ".txt"
                    return fileName.substring(
                            (accountId + "-").length(),
                            fileName.length() - 4
                    );
                }
            }
        }

        return null;
    }

    public HashMap<String, String> getToAccount(String toAccountId, String userName) {

        HashMap<String, String> result = new HashMap<>();

        File accountFile = new File(accountsFolder + "/" + toAccountId + "-" + userName + ".txt");

        double balance = 0d;
        boolean isActive = false;
        int overDraft = 0;
        String cardType = "";
        try (Scanner myReader = new Scanner(accountFile)) {

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine().trim().toLowerCase();

                if (data.startsWith("balance:")) {
                    balance = Double.parseDouble(data.split(":", 2)[1]);

                } else if (data.startsWith("isactive:")) {
                    isActive = Boolean.parseBoolean(data.split(":", 2)[1]);

                } else if (data.startsWith("overdraft:")) {
                    overDraft = Integer.parseInt(data.split(":", 2)[1]);

                } else if (data.startsWith("cardtype:")) {
                    cardType = data.split(":", 2)[1];
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("Account file not found: " + e.getMessage());
            return null;
        }

        result.put("balance", String.valueOf(balance));
        result.put("isActive", String.valueOf(isActive));
        result.put("overDraft", String.valueOf(overDraft));
        result.put("cardType", cardType);

        return result;
    }


    public double getAccountBalance(String accountId, String username) {
        String filePath = accountsFolder + "/" + accountId + "-" + username + ".txt";
        File file = new File(filePath);

        if (!file.exists()) return 0d;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim().toLowerCase();
                if (line.startsWith("balance:")) {
                    return Double.parseDouble(line.split(":")[1]);
                }
            }
        } catch (Exception e) {
            System.out.println("Could not read balance: " + e.getMessage());
        }
        return 0d;
    }


    public boolean userExists(String username) {
        String usersPath = authPath + "/" + username + ".txt";

        File file = new File(usersPath);

        return file.isFile() && file.exists();
    }


    public List<String[]> getFilteredTransactions(
            String accountId,
            String userName,
            LocalDateTime fromDate,
            String typeFilter   // "ALL", "TRANSFER", "WITHDRAW", etc
    ) {
        List<String[]> result = new ArrayList<>();
        File file = new File(transactionsPath + "/" + accountId + "-" + userName + ".txt");

        if (!file.exists()) return result;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] p = line.split(",");

                LocalDateTime date = LocalDateTime.parse(p[1], formatter);
                String type = p[2];

                // date filter
                if (date.isBefore(fromDate)) continue;

                // type filter
                if (!typeFilter.equals("ALL") && !type.equalsIgnoreCase(typeFilter)) continue;

                result.add(p);
            }
        } catch (Exception ignored) {
        }

        return result;
    }


}
