package com.nations.data;

import com.google.gson.JsonObject;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Economy {
    private static final Map<UUID, Double> balances = new ConcurrentHashMap<>();
    private static final double STARTING_BALANCE = 100.0;
    private static final String CURRENCY = "монет";

    public static double getBalance(UUID player) {
        return balances.getOrDefault(player, STARTING_BALANCE);
    }

    public static void setBalance(UUID player, double amount) {
        balances.put(player, Math.max(0, amount));
    }

    public static boolean withdraw(UUID player, double amount) {
        double bal = getBalance(player);
        if (bal < amount) return false;
        setBalance(player, bal - amount);
        return true;
    }

    public static void deposit(UUID player, double amount) {
        setBalance(player, getBalance(player) + amount);
    }

    public static boolean transfer(UUID from, UUID to, double amount) {
        if (!withdraw(from, amount)) return false;
        deposit(to, amount);
        return true;
    }

    public static String getCurrency() { return CURRENCY; }

    public static String format(double amount) {
        return String.format("%.1f %s", amount, CURRENCY);
    }

    // Town treasury
    private static final Map<String, Double> townTreasury = new ConcurrentHashMap<>();

    public static double getTownBalance(String townName) {
        return townTreasury.getOrDefault(townName.toLowerCase(), 0.0);
    }

    public static void depositToTown(String townName, double amount) {
        String key = townName.toLowerCase();
        townTreasury.put(key, getTownBalance(townName) + amount);
    }

    public static boolean withdrawFromTown(String townName, double amount) {
        double bal = getTownBalance(townName);
        if (bal < amount) return false;
        townTreasury.put(townName.toLowerCase(), bal - amount);
        return true;
    }

    // Nation treasury
    private static final Map<String, Double> nationTreasury = new ConcurrentHashMap<>();

    public static double getNationBalance(String nationName) {
        return nationTreasury.getOrDefault(nationName.toLowerCase(), 0.0);
    }

    public static void depositToNation(String nationName, double amount) {
        String key = nationName.toLowerCase();
        nationTreasury.put(key, getNationBalance(nationName) + amount);
    }

    public static boolean withdrawFromNation(String nationName, double amount) {
        double bal = getNationBalance(nationName);
        if (bal < amount) return false;
        nationTreasury.put(nationName.toLowerCase(), bal - amount);
        return true;
    }

    // Tax collection
    public static double collectTax(Town town, double taxRate) {
        double total = 0;
        for (UUID member : town.getMembers()) {
            double tax = getBalance(member) * taxRate;
            if (withdraw(member, tax)) {
                total += tax;
            }
        }
        depositToTown(town.getName(), total);
        return total;
    }

    // Serialization
    public static JsonObject toJson() {
        JsonObject json = new JsonObject();

        JsonObject bals = new JsonObject();
        for (var e : balances.entrySet()) bals.addProperty(e.getKey().toString(), e.getValue());
        json.add("balances", bals);

        JsonObject towns = new JsonObject();
        for (var e : townTreasury.entrySet()) towns.addProperty(e.getKey(), e.getValue());
        json.add("townTreasury", towns);

        JsonObject nations = new JsonObject();
        for (var e : nationTreasury.entrySet()) nations.addProperty(e.getKey(), e.getValue());
        json.add("nationTreasury", nations);

        return json;
    }

    public static void fromJson(JsonObject json) {
        balances.clear();
        townTreasury.clear();
        nationTreasury.clear();

        if (json.has("balances")) {
            for (var e : json.getAsJsonObject("balances").entrySet()) {
                balances.put(UUID.fromString(e.getKey()), e.getValue().getAsDouble());
            }
        }
        if (json.has("townTreasury")) {
            for (var e : json.getAsJsonObject("townTreasury").entrySet()) {
                townTreasury.put(e.getKey(), e.getValue().getAsDouble());
            }
        }
        if (json.has("nationTreasury")) {
            for (var e : json.getAsJsonObject("nationTreasury").entrySet()) {
                nationTreasury.put(e.getKey(), e.getValue().getAsDouble());
            }
        }
    }
}
