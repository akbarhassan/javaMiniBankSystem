package com.ga.bank.app;
import com.ga.bank.util.PasswordEncryptor;
import com.ga.bank.storage.FileDBWriter;
import com.ga.bank.User.Role;
import java.util.Scanner;

public class TerminalUI {
    private Scanner scanner = new Scanner(System.in);

    public void start() {
        boolean running = true;

        while (running) {
            showMainMenu();

            String selectedAction = scanner.nextLine();

            switch (selectedAction){
                case "1":
                    login();
                case "2":
                    register();
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


    private void login(){
        System.out.println("Enter your username");
        String userName = scanner.nextLine();

        System.out.println("Enter your password");
        String plainPassowrd = scanner.nextLine();



    }

    private void register() {
        System.out.println("Enter a username");
        String userName = scanner.nextLine();

        System.out.println("Enter your full name");
        String fullName = scanner.nextLine();

        System.out.println("Enter your email");
        String email = scanner.nextLine();

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

        System.out.println("Hashed password: " + hashedPassword);

        FileDBWriter authRegister = new FileDBWriter();
        authRegister.writeUserCredentials(userName,fullName,email,hashedPassword, String.valueOf(Role.CUSTOMER),false);
    }


}
