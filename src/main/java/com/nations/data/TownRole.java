package com.nations.data;

public enum TownRole {
    RULER("ruler", "§6👑 Правитель", 100),
    VICE_RULER("vice_ruler", "§e⚜ Зам. Правителя", 80),
    GENERAL("general", "§c⚔ Генерал", 60),
    OFFICER("officer", "§9🛡 Офицер", 40),
    BUILDER("builder", "§a🔨 Строитель", 30),
    CITIZEN("citizen", "§7👤 Гражданин", 10);

    private final String id;
    private final String displayName;
    private final int power;

    TownRole(String id, String displayName, int power) {
        this.id = id;
        this.displayName = displayName;
        this.power = power;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getPower() { return power; }

    public boolean canClaim() { return power >= 30; }
    public boolean canInvite() { return power >= 40; }
    public boolean canKick() { return power >= 60; }
    public boolean canManageTown() { return power >= 80; }
    public boolean isRuler() { return power >= 100; }

    public static TownRole fromId(String id) {
        for (TownRole r : values()) {
            if (r.id.equalsIgnoreCase(id)) return r;
        }
        return null;
    }
}
