# Debit Cards Module

A comprehensive card management system that defines card types, operation limits, and card structures for the ACME Bank application.

---

## Overview

This module manages the debit card system including:
- Different card tier types (PLATINUM, TITANIUM, MASTERCARD)
- Transaction operation types and their daily limits
- Card object structure with card type and number
- Centralized limit enforcement based on card type and operation

---

## Components

### 1. CardType (Enum)

**Defines the three tiers of debit cards available**

```java
public enum CardType {
    PLATINUM,    // Highest tier card
    TITANIUM,    // Mid-tier card
    MASTERCARD   // Standard tier card
}
```

**Card Hierarchy:**
- **PLATINUM**: Premium card with highest transaction limits
- **TITANIUM**: Mid-range card with moderate transaction limits
- **MASTERCARD**: Standard card with basic transaction limits

---

### 2. Operations (Enum)

**Defines all banking operation types that have daily limits**

```java
public enum Operations {
    WithdrawLimitPerDay,              // Cash withdrawal limit per day
    TransferLimitPerDayOwnAccount,    // Transfer limit to own accounts per day
    TransferLimitPerDay,              // Transfer limit to other accounts per day
    DepositLimitPerDay,               // Deposit limit to other accounts per day
    DepositLimitPerDayOwnAccount      // Deposit limit to own accounts per day
}
```

**Operation Types:**

#### `WithdrawLimitPerDay`
Maximum amount that can be withdrawn from ATMs or branches in a single day

#### `TransferLimitPerDayOwnAccount`
Maximum amount that can be transferred between your own accounts in a single day

#### `TransferLimitPerDay`
Maximum amount that can be transferred to other people's accounts in a single day

#### `DepositLimitPerDay`
Maximum amount that can be deposited to other accounts in a single day

#### `DepositLimitPerDayOwnAccount`
Maximum amount that can be deposited to your own accounts in a single day

---

### 3. DebitCard (Class)

**Represents a physical debit card object**

#### Constructor

```java
public DebitCard(CardType cardType, String cardNumber)
```
- Creates a new debit card with specified type and card number
- `cardType`: The tier of the card (PLATINUM, TITANIUM, or MASTERCARD)
- `cardNumber`: Unique identifier for the card

#### Methods

##### `getCardNumber()`
**Returns the card's unique number**
- Returns: String representing the card number
- Used for card identification and validation

##### `getCardType()`
**Returns the card's tier type**
- Returns: CardType enum value (PLATINUM, TITANIUM, or MASTERCARD)
- Used to determine transaction limits for this card

**Purpose:**
This class encapsulates card information, linking a physical card number to its type/tier for limit enforcement.

---

### 4. CardLimits (Class)

**Central repository for all card type transaction limits**

This class uses a static initialization block to set up a two-dimensional mapping structure:
- First dimension: Operation type (withdraw, transfer, deposit, etc.)
- Second dimension: Card type (PLATINUM, TITANIUM, MASTERCARD)
- Value: Daily limit amount in currency units

#### Static Initialization Block

The `static {}` block runs once when the class is first loaded, setting up all limits:

##### Withdraw Limits Per Day
```java
PLATINUM:   20,000
TITANIUM:   10,000
MASTERCARD:  5,000
```
Maximum daily ATM/branch withdrawal amounts

##### Transfer Limits Per Day (Own Accounts)
```java
PLATINUM:   80,000
TITANIUM:   40,000
MASTERCARD: 20,000
```
Maximum daily transfer amounts between your own accounts

##### Deposit Limits Per Day (Other Accounts)
```java
PLATINUM:   100,000
TITANIUM:   100,000
MASTERCARD: 100,000
```
Maximum daily deposit amounts to other people's accounts

##### Deposit Limits Per Day (Own Accounts)
```java
PLATINUM:   200,000
TITANIUM:   200,000
MASTERCARD: 200,000
```
Maximum daily deposit amounts to your own accounts

#### Methods

##### `getLimit(Operations op, CardType type)`
**Retrieves the daily limit for a specific operation and card type**

**Parameters:**
- `op`: The operation type (enum from Operations)
- `type`: The card type (enum from CardType)

**Returns:**
- Double value representing the daily limit in currency

**Example Usage:**
```java
// Get withdraw limit for PLATINUM card
Double limit = CardLimits.getLimit(Operations.WithdrawLimitPerDay, CardType.PLATINUM);
// Returns: 20000.0

// Get transfer limit for TITANIUM card to own account
Double transferLimit = CardLimits.getLimit(Operations.TransferLimitPerDayOwnAccount, CardType.TITANIUM);
// Returns: 40000.0
```

**How It Works:**
1. Takes an operation type and card type as input
2. Looks up the limits map using the operation type as the key
3. From the resulting map, looks up the card type to get the specific limit
4. Returns the limit value

---

## Data Structure

The limits are stored in a nested HashMap structure:

```
limits (Map)
├── WithdrawLimitPerDay (Map)
│   ├── PLATINUM → 20,000
│   ├── TITANIUM → 10,000
│   └── MASTERCARD → 5,000
│
├── TransferLimitPerDayOwnAccount (Map)
│   ├── PLATINUM → 80,000
│   ├── TITANIUM → 40,000
│   └── MASTERCARD → 20,000
│
├── DepositLimitPerDay (Map)
│   ├── PLATINUM → 100,000
│   ├── TITANIUM → 100,000
│   └── MASTERCARD → 100,000
│
└── DepositLimitPerDayOwnAccount (Map)
    ├── PLATINUM → 200,000
    ├── TITANIUM → 200,000
    └── MASTERCARD → 200,000
```

---

## Usage Example

```java
// Create a platinum debit card
DebitCard myCard = new DebitCard(CardType.PLATINUM, "1234-5678-9012-3456");

// Get the card type
CardType type = myCard.getCardType(); // Returns: PLATINUM

// Check withdraw limit for this card
Double withdrawLimit = CardLimits.getLimit(
    Operations.WithdrawLimitPerDay, 
    type
); // Returns: 20000.0

// Before processing a withdrawal of 15000
if (withdrawalAmount <= withdrawLimit) {
    // Process withdrawal
} else {
    // Reject - exceeds daily limit
}
```

---

## Limit Summary Table

| Operation | PLATINUM | TITANIUM | MASTERCARD |
|-----------|----------|----------|------------|
| **Withdraw/Day** | 20,000 | 10,000 | 5,000 |
| **Transfer/Day (Own)** | 80,000 | 40,000 | 20,000 |
| **Deposit/Day (Other)** | 100,000 | 100,000 | 100,000 |
| **Deposit/Day (Own)** | 200,000 | 200,000 | 200,000 |

---

## Design Benefits

### Type Safety
- Using enums prevents invalid operation or card types
- Compile-time checking of operation and card type values

### Centralized Management
- All limits defined in one place
- Easy to update limits without touching business logic
- Single source of truth for all card limits

### Extensibility
- Easy to add new card types (e.g., GOLD, SILVER)
- Easy to add new operation types (e.g., InternationalTransfer)
- Simply add to enum and update static block

### Performance
- Static initialization occurs once at class loading
- Fast HashMap lookups for limit retrieval
- No database queries needed for limit checks

---

## Notes

- Limits are enforced on a per-day basis
- Higher tier cards (PLATINUM) have higher limits for most operations
- Deposit limits are uniform across all card types for deposits to other accounts
- Own account operations generally have higher limits than other account operations
- All limits are stored as Double values to support decimal currency amount