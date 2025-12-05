package com.ga.bank.debitCards;

import java.util.HashMap;
import java.util.Map;

public class CardLimits {

    private static final Map<Operations, Map<CardType, Integer>> limits = new HashMap<>();

    static {

        // WithdrawLimitPerDay
        Map<CardType, Integer> withdrawLimits = new HashMap<>();
        withdrawLimits.put(CardType.Platinum, 20000);
        withdrawLimits.put(CardType.Titanium, 10000);
        withdrawLimits.put(CardType.Mastercard, 5000);
        limits.put(Operations.WithdrawLimitPerDay, withdrawLimits);

        // TransferLimitPerDayOwnAccount
        Map<CardType, Integer> transferOwnLimits = new HashMap<>();
        transferOwnLimits.put(CardType.Platinum, 80000);
        transferOwnLimits.put(CardType.Titanium, 40000);
        transferOwnLimits.put(CardType.Mastercard, 20000);
        limits.put(Operations.TransferLimitPerDayOwnAccount, transferOwnLimits);

        // DepositLimitPerDay
        Map<CardType, Integer> depositLimits = new HashMap<>();
        depositLimits.put(CardType.Platinum, 100000);
        depositLimits.put(CardType.Titanium, 100000);
        depositLimits.put(CardType.Mastercard, 100000);
        limits.put(Operations.DepositLimitPerDay, depositLimits);

        // DepositLimitPerDayOwnAccount
        Map<CardType, Integer> depositOwnLimits = new HashMap<>();
        depositOwnLimits.put(CardType.Platinum, 200000);
        depositOwnLimits.put(CardType.Titanium, 200000);
        depositOwnLimits.put(CardType.Mastercard, 200000);
        limits.put(Operations.DepositLimitPerDayOwnAccount, depositOwnLimits);
    }

    public static int getLimit(Operations op, CardType type) {
        return limits.get(op).get(type);
    }
}
