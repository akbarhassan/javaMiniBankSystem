package com.ga.bank.debitCards;

import java.util.HashMap;
import java.util.Map;

public class CardLimits {

    private static final Map<Operations, Map<CardType, Double>> limits = new HashMap<>();

    static {

        // WithdrawLimitPerDay
        Map<CardType, Double> withdrawLimits = new HashMap<>();
        withdrawLimits.put(CardType.Platinum, 20_000d);
        withdrawLimits.put(CardType.Titanium, 10_000d);
        withdrawLimits.put(CardType.Mastercard, 5_000d);
        limits.put(Operations.WithdrawLimitPerDay, withdrawLimits);

        // TransferLimitPerDayOwnAccount
        Map<CardType, Double> transferOwnLimits = new HashMap<>();
        transferOwnLimits.put(CardType.Platinum, 80_000d);
        transferOwnLimits.put(CardType.Titanium, 40_000d);
        transferOwnLimits.put(CardType.Mastercard, 20_000d);
        limits.put(Operations.TransferLimitPerDayOwnAccount, transferOwnLimits);

        // DepositLimitPerDay
        Map<CardType, Double> depositLimits = new HashMap<>();
        depositLimits.put(CardType.Platinum, 100_000d);
        depositLimits.put(CardType.Titanium, 100_000d);
        depositLimits.put(CardType.Mastercard, 100_000d);
        limits.put(Operations.DepositLimitPerDay, depositLimits);

        // DepositLimitPerDayOwnAccount
        Map<CardType, Double> depositOwnLimits = new HashMap<>();
        depositOwnLimits.put(CardType.Platinum, 200_000d);
        depositOwnLimits.put(CardType.Titanium, 200_000d);
        depositOwnLimits.put(CardType.Mastercard, 200_000d);
        limits.put(Operations.DepositLimitPerDayOwnAccount, depositOwnLimits);
    }

    public static Double getLimit(Operations op, CardType type) {
        return limits.get(op).get(type);
    }
}
