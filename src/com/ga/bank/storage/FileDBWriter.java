package com.ga.bank.storage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.FileNotFoundException;

public class FileDBWriter {

    private final String folderPath = "data/users";

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
}
