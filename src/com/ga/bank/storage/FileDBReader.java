package com.ga.bank.storage;

import com.ga.bank.util.PasswordEncryptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileDBReader {
    private final String authPath = "data/users";

    public boolean authLogin(String userName, String plainPassword) {

        File authFile = new File(authPath+"/"+userName.toLowerCase());

        try (Scanner myReader = new Scanner(authFile)) {
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (data.toLowerCase().startsWith("username:" + userName.toLowerCase() + ",")) {
                    // TODO: get password, compare
                    return true;
                }

                System.out.println(data);
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }


        return false;
    }
}
