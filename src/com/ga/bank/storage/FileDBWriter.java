package com.ga.bank.storage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileDBWriter {

    private final String folderPath = "data/users";

    public FileDBWriter() {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs(); // create "data/users" folder
        }
    }

    public void writeUserCredentials(
            String username,
            String fullName,
            String email,
            String hashedPassword,
            String role,
            boolean isLocked
    ) {
        String userFile = folderPath + "/" + username + ".txt"; // one file per user

        try (FileWriter writer = new FileWriter(userFile)) { // overwrite if file exists
            String credentials = "Username:" + username + "\n" +
                    "FullName:" + fullName + "\n" +
                    "Email:" + email + "\n" +
                    "Password:" + hashedPassword + "\n" +
                    "Role:" + role + "\n" +
                    "Locked:" + isLocked;

            writer.write(credentials);
        } catch (IOException e) {
            System.out.println("Error writing user file: " + e.getMessage());
        }
    }
}
