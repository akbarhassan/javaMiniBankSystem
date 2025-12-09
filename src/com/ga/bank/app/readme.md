# ACME Bank Terminal UI

A command-line banking application that provides banking operations for customers and administrative functions for bankers.

---

## Overview

This terminal-based banking system allows users to register, login, perform banking operations, and manage accounts. It features role-based access control with separate interfaces for customers and bankers.

---

## Main Functions

### `start()`
**Entry point of the application**
- Displays the main menu with options to login, register, or exit
- Runs in a loop until user chooses to exit
- Routes to appropriate operations based on user role (Customer/Banker)

### `showMainMenu()`
**Displays the welcome screen**
- Shows three options: Login, Register Account, Exit
- Called repeatedly in the main loop

---

## Authentication Functions

### `login()`
**Handles user authentication**
- Prompts for username and password
- Validates credentials using `FileDBReader.authLogin()`
- Sets `currentUser` and loads their account data if login successful
- For customers, loads their accounts and prompts to select active account
- Returns `true` if login successful, `false` otherwise

### `register()`
**Creates new customer account**
- Collects username, full name, email (with validation), and password
- Validates email format using regex pattern
- Encrypts password using `PasswordEncryptor`
- Prompts user to select card type (3 options)
- Sets initial balance
- Creates user credentials and account files using `FileDBWriter`

---

## Customer Operations

### `operations()`
**Main menu for customer banking operations**
- Displays available operations menu
- Handles the following operations:
    1. Deposit
    2. Withdraw
    3. Transfer
    4. Show current balance
    5. Create new account
    6. Show transaction filters
    0. Logout

### `showOperations()`
**Displays customer operations menu**
- Lists all available banking operations for customers

### `setAccountId()`
**Account selection interface**
- Displays all accounts owned by the current user
- Prompts user to select which account to use for operations
- Validates input and returns selected account ID

### `getDeposit(String operation)`
**Gets amount for banking operation**
- Prompts user to enter amount for specified operation (deposit/withdraw/transfer)
- Validates input is a positive number
- Loops until valid amount is entered
- Returns the validated amount

### `sendToAccount(String operation)`
**Determines destination account for deposit/transfer**
- Presents four options:
    1. One of user's own accounts
    2. Another user's account
    3. Enter account ID manually
    4. Current account
- For option 1: Lists user's other accounts
- For option 2: Lists all other users' accounts
- For option 3: Validates manually entered account ID
- Returns selected account ID

### `createAccount()`
**Creates additional account for existing user**
- Prompts for card type selection
- Prompts for initial balance
- Creates new account using `FileDBWriter`
- Notifies user to logout and login again to use new account

### `transactionFilters(Account currentAccount)`
**Displays filtered transaction history**
- Allows filtering by:
    - Date range (Today, Last 7 days, Last 30 days)
    - Transaction type (DEPOSIT, WITHDRAW, TRANSFER, OVERDRAFT, INCOMING, ALL)
- Fetches filtered transactions from `FileDBReader`
- Displays results in formatted table using `printTransactionTable()`

---

## Banker Operations

### `bankerOperations()`
**Main menu for banker administrative operations**
- Displays banker operations menu
- Handles the following operations:
    1. List customer details
    2. List customer account details
    3. Create Banker
    4. List customer transaction details
    5. Activate customer account
    6. Deactivate customer account
    7. Exit

### `bankerOperationsTemplate()`
**Displays banker operations menu**
- Lists all available administrative operations for bankers

### `enterCustomerUserName()`
**Views specific customer details**
- Prompts for customer username
- Validates user exists
- Calls `Banker.viewUserDetails()` to display user information

### `getUserAccountsDetails()`
**Views detailed account information for any customer**
- Prompts for customer username
- Lists all accounts for that customer
- Allows selection of specific account to view details
- Displays account details retrieved from `FileDBReader.getToAccount()`
- Loops to allow viewing multiple accounts

### `setAccountActive()`
**Activates a customer account**
- Banker-only function
- Prompts for customer username
- Lists all accounts for that customer
- Allows selection of account to activate
- Calls `Banker.activateAccount()` to perform activation
- Can activate multiple accounts in one session

### `setAccountInActive()`
**Deactivates a customer account**
- Banker-only function
- Prompts for customer username
- Lists all accounts for that customer
- Allows selection of account to deactivate
- Calls `Banker.deActivateAccount()` to perform deactivation
- Can deactivate multiple accounts in one session

### `createBankerAccount()`
**Creates new banker user**
- Banker-only function
- Collects username, full name, email, and password
- Validates email format using regex
- Encrypts password
- Calls `Banker.createBankerAccount()` to create banker credentials

### `transactionBankerFilters()`
**Views filtered transactions for any customer account**
- Banker-only function
- Prompts for customer username
- Lists customer's accounts
- Allows filtering by:
    - Date range (Today, Last 7 days, Last 30 days)
    - Transaction type (DEPOSIT, WITHDRAW, TRANSFER, OVERDRAFT, INCOMING, ALL)
- Fetches and displays filtered transactions
- Can view transactions for multiple accounts in one session

---

## Utility Functions

### `printTransactionTable(List<String[]> rows)`
**Formats and displays transaction data**
- Creates formatted table with columns:
    - ID
    - Date & Time
    - Type
    - To/From
    - Amount
    - Post Balance
- Handles empty result sets with appropriate message
- Uses fixed-width formatting for clean display

---

## Class Variables

- `scanner`: Scanner object for user input
- `currentUser`: Currently logged-in user (Customer or Banker)
- `currentAccount`: Currently active account for operations
- `accountList`: List of account IDs owned by current user
- `fileDBReader`: Handles reading data from file-based database



---

## Usage

1. Run the application
2. Select Register to create a new customer account, or Login if you already have an account
3. After logging in as a customer, select from various banking operations
4. Bankers have access to administrative functions to manage customer accounts
5. Select Exit/Logout when finished

---

## Notes

- All passwords are encrypted before storage
- Email validation uses standard regex pattern
- Customer accounts can be activated/deactivated by bankers
- Transaction history is filterable by date range and transaction type
- Users can have multiple accounts with different card types