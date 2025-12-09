# Account Module

This module provides the `Account` class, which represents a bank account for a user. It handles operations like deposits, withdrawals, transfers, and overdrafts, while enforcing daily limits and overdraft rules. Transactions are logged, and account data is persisted using the `FileDBReader` and `FileDBWriter` classes.

---

### Class: Account

#### `getBalance()`
Returns the current balance of the account.

#### `setBalance(double amount, String toAccount, OperationType operationType)`
Updates the account balance based on the operation type (`DEPOSIT`, `WITHDRAW`, `TRANSFER`, `OVERDRAFT`). Also writes the change to file storage and logs the transaction.

#### `deposit(double amount, String toAccount)`
Deposits money into this or another account. Checks account activity and daily deposit limits before updating balance.

#### `withdraw(double amount)`
Withdraws money from the account. Checks account activity, daily limits, and uses overdraft if necessary.

#### `transfer(double amount, String toAccount)`
Transfers money to another account. Validates target account, enforces transfer limits, and updates balances of both accounts.

#### `overDraft()`
Applies an overdraft fee (default 35 units) to the account and logs the transaction.

#### `getActive() / setActive(boolean active)`
Gets or sets the account's active status. Accounts are blocked when overdraft limits are reached.

#### `getOverDraft() / setOverDraft(int overDraft)`
Tracks overdraft attempts. Automatically blocks the account if overdraft attempts exceed the allowed limit.

#### `getUser()`
Returns the `User` object associated with the account.

---

### Notes
- All operations interact with `FileDBReader` and `FileDBWriter` to persist changes.
- Daily limits and overdraft rules are enforced automatically.
- Transactions are logged via the `Transactions` class.
