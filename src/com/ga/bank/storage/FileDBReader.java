package com.ga.bank.storage;

import com.ga.bank.util.PasswordEncryptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileDBReader {
    private final String authPath = "data/users";

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
        return true;    }
}
