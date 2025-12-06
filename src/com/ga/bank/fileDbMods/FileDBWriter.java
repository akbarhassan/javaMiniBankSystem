package com.ga.bank.fileDbMods;

import com.ga.bank.account.OperationType;
import com.ga.bank.debitCards.CardType;
import com.ga.bank.debitCards.Operations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.FileNotFoundException;

public class FileDBWriter {

    private final String folderPath = "data/users";
    private final String accountsPath = "data/accounts";
    private final String transactionsPath = "data/transactions";

    public FileDBWriter() {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public boolean writeUserCredentials(
            String username,
            String fullName,
            String email,
            String hashedPassword,
            String role,
            boolean isLocked
    ) {
        String userFile = folderPath + "/" + username + ".txt"; // one file per user

        File file = new File(userFile);

        if (file.exists() && file.isFile()) {
            throw new RuntimeException("File already exists");
        }
        try (FileWriter writer = new FileWriter(userFile)) {
            String credentials = "Username:" + username + "\n" +
                    "FullName:" + fullName + "\n" +
                    "Email:" + email + "\n" +
                    "Password:" + hashedPassword + "\n" +
                    "Role:" + role + "\n" +
                    "loginAttempt:" + 0 + "\n" +
                    "Locked:" + isLocked;

            writer.write(credentials);
            writer.flush();
            return true;
        } catch (IOException e) {
            System.out.println("Error writing user file: " + e.getMessage());
            return false;
        }
    }

    public void increaseLoginCounter(int counter, String username) {
        String userFile = folderPath + "/" + username + ".txt";
        File file = new File(userFile);

        if (!file.exists() || !file.isFile()) {
            throw new RuntimeException("File does not exist");
        }
        List<String> lines = new ArrayList<>();

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }
        counter++;
        lines.set(lines.size() - 2, "loginAttempt:" + counter); // always update

        if (counter >= 3) {
            lines.set(lines.size() - 1, "Locked:true");
            System.out.println("User has been locked due to 3 failed login attempts.");
        }


        try (FileWriter writer = new FileWriter(file)) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
            writer.flush();
        } catch (IOException e) {
            System.out.println("Error updating user file: " + e.getMessage());
        }

    }


    public void resetCounter(String username) {
        int counter = 0;
        String userFile = folderPath + "/" + username + ".txt";
        File file = new File(userFile);

        if (!file.exists() || !file.isFile()) {
            throw new RuntimeException("File does not exist");
        }
        List<String> lines = new ArrayList<>();

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }

        lines.set(lines.size() - 2, "loginAttempt:" + counter);
        lines.set(lines.size() - 1, "Locked:false");
        System.out.println("User attempts has been reset.");


        try (FileWriter writer = new FileWriter(file)) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
            writer.flush();
        } catch (IOException e) {
            System.out.println("Error updating user file: " + e.getMessage());
        }
    }


    public boolean writeUserAccount(
            String username,
            CardType cardType,
            double balance,
            int overdraft,
            boolean isActive

    ) {
        String accountId = generateAccountId(username);

        String userAccountFile = accountsPath + "/" + accountId + "-" + username + ".txt";

        File file = new File(userAccountFile);

        if (file.exists() || file.isFile()) {
            throw new RuntimeException("File already exists");
        }

        try (FileWriter writer = new FileWriter(userAccountFile)) {

            String credentials = "Username:" + username + "\n" +
                    "accountId:" + accountId + "\n" +
                    "cardType:" + cardType.name() + "\n" +
                    "balance:" + balance + "\n" +
                    "overdraft:" + overdraft + "\n" +
                    "isActive:" + isActive + "\n" +
                    "CardNumber:" + generateSimpleCardNumber(accountId);

            writer.write(credentials);
            writer.flush();
            createTransactionsFile(username, accountId);
            return true;
        } catch (IOException e) {
            System.out.println("Error writing user file: " + e.getMessage());
            return false;
        }

    }

    private String generateAccountId(String username) {
        String accountId;
        File file;

        do {
            // random 6-digit ID
            accountId = String.valueOf((int) (Math.random() * 900000) + 100000);
            file = new File(folderPath + "/" + accountId + "-" + username + ".txt");
        } while (file.exists());

        return accountId;
    }

    public static String generateSimpleCardNumber(String accountId) {
        StringBuilder sb = new StringBuilder();

        // use userId first (padded to ensure fixed length)
        sb.append(String.format("%06d", Integer.parseInt(accountId))); // 6-digit user id

        // add random numbers to reach 16 digits
        for (int i = sb.length(); i < 16; i++) {
            sb.append((int) (Math.random() * 10));
        }

        return sb.toString();
    }


    public void modifyAccountBalance(String accountId, String username, double balance) {
        String userAccountFile = accountsPath + "/" + accountId + "-" + username + ".txt";

        File file = new File(userAccountFile);

        if (!file.exists() || !file.isFile()) {
            throw new RuntimeException("File does not exist");
        }

        List<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("balance:")) {
                lines.set(i, "balance:" + balance);
                break;
            }
        }
        try (FileWriter writer = new FileWriter(file)) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
            writer.flush();
        } catch (IOException e) {
            System.out.println("Error updating user file: " + e.getMessage());
        }

    }


    public void createTransactionsFile(String userName, String accountId) {
        // Path: data/transactions/{accountId}-{userName}.txt
        String filePath = transactionsPath + "/" + accountId + "-" + userName + ".txt";

        File file = new File(filePath);

        // Create the transactions folder if it doesn't exist
        File folder = new File(transactionsPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(""); // empty file
                System.out.println("Transaction file created for account: " + accountId);
            } catch (IOException e) {
                System.out.println("Error creating transactions file: " + e.getMessage());
            }
        } else {
            System.out.println("Transaction file already exists for account: " + accountId);
        }
    }

    public void createTransaction(
            String userName,
            String accountId,
            OperationType operation,
            String toAccountId,
            double amount,
            double balance
    ) {
        String filePath = transactionsPath + "/" + accountId + "-" + userName + ".txt";

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateTime = now.format(formatter);

        int id = 1;

        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            createTransactionsFile(userName, accountId);
        }

        List<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(file)) {
            String lastNonEmptyLine = null;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    lastNonEmptyLine = line;
                }
            }
            if (lastNonEmptyLine != null) {
                String[] parts = lastNonEmptyLine.split(",", 2); // ID is the first part
                try {
                    id = Integer.parseInt(parts[0].trim()) + 1;
                } catch (NumberFormatException e) {
                    id++;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
            return;
        }
        String record = id + "," + dateTime + "," + operation + "," + toAccountId + "," + amount + "," + balance;
        try (FileWriter writer = new FileWriter(file, true)) { // append mode
            writer.write(record + "\n");
            writer.flush();
        } catch (IOException e) {
            System.out.println("Error writing transaction: " + e.getMessage());
        }
    }

    public void modifyAccountOverDraft(String accountId, String username, int overdraft, boolean isActive) {
        String userAccountFile = accountsPath + "/" + accountId + "-" + username + ".txt";
        File file = new File(userAccountFile);

        if (!file.exists() || !file.isFile()) {
            throw new RuntimeException("File does not exist");
        }

        List<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).toLowerCase();
            if (line.startsWith("overdraft:")) {
                lines.set(i, "overdraft:" + overdraft);
            } else if (line.startsWith("isactive:")) {
                lines.set(i, "isActive:" + isActive);
            }
        }

        try (FileWriter writer = new FileWriter(file)) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
            writer.flush();
        } catch (IOException e) {
            System.out.println("Error updating user file: " + e.getMessage());
        }

    }
}
