package com.ga.bank.fileDbMods;

import com.ga.bank.account.OperationType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class FileDBReaderTest {

    private FileDBReader reader;

    private final String testUser = "john";
    private final String testAccount = "123456";

    @Before
    public void setUp() throws IOException {
        reader = new FileDBReader();

        // Create user file
        File usersFolder = new File("data/users");
        usersFolder.mkdirs();
        File userFile = new File(usersFolder, testUser + ".txt");
        try (FileWriter fw = new FileWriter(userFile)) {
            fw.write("username:" + testUser + "\n");
            fw.write("password:hashedpassword\n");
        }

        // Create account file
        File accountsFolder = new File("data/accounts");
        accountsFolder.mkdirs();
        File accountFile = new File(accountsFolder, testAccount + "-" + testUser + ".txt");
        try (FileWriter fw = new FileWriter(accountFile)) {
            fw.write("balance:500\n");
            fw.write("isactive:true\n");
        }

        // Create transactions file
        File transactionsFolder = new File("data/transactions");
        transactionsFolder.mkdirs();
        File transactionsFile = new File(transactionsFolder, testAccount + "-" + testUser + ".txt");
        try (FileWriter fw = new FileWriter(transactionsFile)) {
            fw.write("1,2025-12-09 10:00:00,DEPOSIT,123,200,desc\n");
            fw.write("2,2025-12-09 12:00:00,WITHDRAW,123,50,desc\n");
        }
    }

    @After
    public void tearDown() {
        // Clean up all created files
        new File("data/users/" + testUser + ".txt").delete();
        new File("data/accounts/" + testAccount + "-" + testUser + ".txt").delete();
        new File("data/transactions/" + testAccount + "-" + testUser + ".txt").delete();
    }

    @Test
    public void testUserExists() {
        assertTrue(reader.userExists(testUser));
        assertFalse(reader.userExists("doesnotexist"));
    }

    @Test
    public void testAccountExists() {
        assertTrue(reader.AccountExists(testAccount));
        assertFalse(reader.AccountExists("000000"));
    }

    @Test
    public void testGetAccountBalance() {
        assertEquals(500, reader.getAccountBalance(testAccount, testUser), 0.01);
    }

    @Test
    public void testGetDailyLimit() {
        double depositLimit = reader.getDailyLimit(testUser, testAccount, OperationType.DEPOSIT);
        assertEquals(200, depositLimit, 0.01);

        double withdrawLimit = reader.getDailyLimit(testUser, testAccount, OperationType.WITHDRAW);
        assertEquals(50, withdrawLimit, 0.01);
    }

    @Test
    public void testUserExistsFail() {
        // This should fail because the user "john" **does exist**
        assertFalse(reader.userExists(testUser));
    }

    @Test
    public void testGetAccountBalanceFail() {
        // This should fail because the actual balance is 500
        assertEquals(1000, reader.getAccountBalance(testAccount, testUser), 0.01);
    }
}