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
    FileDBReader fileDBReader = new FileDBReader();

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

        File folder = new File(accountsPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
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
        boolean exists;
        File folder = new File(folderPath);

        do {
            accountId = String.valueOf((int) (Math.random() * 900000) + 100000);
            exists = false;

            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().startsWith(accountId + "-")) {
                        exists = true;
                        break;
                    }
                }
            }
        } while (exists);
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
        String senderPath = transactionsPath + "/" + accountId + "-" + userName + ".txt";
        File senderFile = new File(senderPath);

        if (!senderFile.exists()) {
            createTransactionsFile(userName, accountId);
        }

        String dateTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // ---- SENDER RECORD ----
        int senderId = getNextTransactionId(senderFile);

        String senderRecord = senderId + "," + dateTime + "," + operation + "," +
                toAccountId + "," + amount + "," + balance;

        appendToFile(senderFile, senderRecord);

        // ---- STOP if same account ----
        if (accountId.equals(toAccountId)) return;

        // ---- RECEIVER RECORD ----
        String toUserName = fileDBReader.getOwnerOfAccount(toAccountId);

        if (toUserName == null) {
            System.out.println("Receiver account not found.");
            return;
        }

        String receiverPath = transactionsPath + "/" + toAccountId + "-" + toUserName + ".txt";
        File receiverFile = new File(receiverPath);

        if (!receiverFile.exists()) {
            createTransactionsFile(toUserName, toAccountId);
        }

        int receiverId = getNextTransactionId(receiverFile);
        double toPostBalance = fileDBReader.getAccountBalance(toAccountId, toUserName);

        String receiverRecord = receiverId + "," + dateTime + "," +
                OperationType.INCOMING + "," + accountId + "," +
                amount + "," + toPostBalance;

        appendToFile(receiverFile, receiverRecord);
    }

    private int getNextTransactionId(File file) {
        int id = 1;

        try (Scanner sc = new Scanner(file)) {
            String last = null;
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (!line.isEmpty()) last = line;
            }
            if (last != null) {
                id = Integer.parseInt(last.split(",")[0].trim()) + 1;
            }
        } catch (Exception ignored) {
        }

        return id;
    }

    private void appendToFile(File file, String line) {
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(line + "\n");
        } catch (IOException ex) {
            System.out.println("Error writing transaction: " + ex.getMessage());
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


    //TODO: create a function to create user accounts, with type, user accounts only 2 i assume
}
