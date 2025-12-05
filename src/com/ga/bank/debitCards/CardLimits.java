package com.ga.bank.debitCards;

import java.util.HashMap;
import java.util.Map;

public class CardLimits {

    private static final Map<Operations, Map<CardType, Double>> limits = new HashMap<>();

    static {

        // WithdrawLimitPerDay
        Map<CardType, Double> withdrawLimits = new HashMap<>();
        withdrawLimits.put(CardType.PLATINUM, 20_000d);
        withdrawLimits.put(CardType.TITANIUM, 10_000d);
        withdrawLimits.put(CardType.MASTERCARD, 5_000d);
        limits.put(Operations.WithdrawLimitPerDay, withdrawLimits);

        // TransferLimitPerDayOwnAccount
        Map<CardType, Double> transferOwnLimits = new HashMap<>();
        transferOwnLimits.put(CardType.PLATINUM, 80_000d);
        transferOwnLimits.put(CardType.TITANIUM, 40_000d);
        transferOwnLimits.put(CardType.MASTERCARD, 20_000d);
        limits.put(Operations.TransferLimitPerDayOwnAccount, transferOwnLimits);

        // DepositLimitPerDay
        Map<CardType, Double> depositLimits = new HashMap<>();
        depositLimits.put(CardType.PLATINUM, 100_000d);
        depositLimits.put(CardType.TITANIUM, 100_000d);
        depositLimits.put(CardType.MASTERCARD, 100_000d);
        limits.put(Operations.DepositLimitPerDay, depositLimits);

        // DepositLimitPerDayOwnAccount
        Map<CardType, Double> depositOwnLimits = new HashMap<>();
        depositOwnLimits.put(CardType.PLATINUM, 200_000d);
        depositOwnLimits.put(CardType.TITANIUM, 200_000d);
        depositOwnLimits.put(CardType.MASTERCARD, 200_000d);
        limits.put(Operations.DepositLimitPerDayOwnAccount, depositOwnLimits);
    }

    public static Double getLimit(Operations op, CardType type) {
        return limits.get(op).get(type);
    }
}
