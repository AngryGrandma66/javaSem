package com.game.javasem.model;

import javafx.scene.image.Image;

import java.util.List;

// Item.java (model for items)
public class GameItem {
    private boolean consumable;

    public enum Type { WEAPON, ARMOR, AMULET, CONSUMABLE }
    
    private String name;
    private Type type;
    private Image icon;  // icon to display for this item (loaded from resource)
    // Weapon-specific properties
    private List<Attack> attacks;       // only used if type == WEAPON (list of 3 attacks)
    // Armor-specific property
    private int healthBonus;           // used if type == ARMOR
    // Amulet-specific properties
    private double healthMultiplier;   // used if type == AMULET (e.g., 1.2 for +20% health)
    private double attackMultiplier;   // used if type == AMULET (e.g., 1.5 for +50% attack)

    public static GameItem createConsumable(String name, Image icon) {
        GameItem item = new GameItem(name, Type.CONSUMABLE, icon);
        item.consumable = true;
        return item;
    }
    // Constructor for a weapon
    public static GameItem createWeapon(String name, Image icon, List<Attack> attacks) {
        GameItem gameItem = new GameItem(name, Type.WEAPON, icon);
        gameItem.attacks = attacks;
        return gameItem;
    }
    // Constructor for armor
    public static GameItem createArmor(String name, Image icon, int healthBonus) {
        GameItem gameItem = new GameItem(name, Type.ARMOR, icon);
        gameItem.healthBonus = healthBonus;
        return gameItem;
    }
    // Constructor for amulet
    public static GameItem createAmulet(String name, Image icon, double healthMult, double attackMult) {
        GameItem gameItem = new GameItem(name, Type.AMULET, icon);
        gameItem.healthMultiplier = healthMult;
        gameItem.attackMultiplier = attackMult;
        return gameItem;
    }
    // Private base constructor
    private GameItem(String name, Type type, Image icon) {
        this.name = name;
        this.type = type;
        this.icon = icon;
        // initialize defaults
        this.attacks = new java.util.ArrayList<>();
        this.healthMultiplier = 1.0;
        this.attackMultiplier = 1.0;
    }

    public String getName() { return name; }
    public Type getType() { return type; }
    public Image getIcon() { return icon; }
    public List<Attack> getAttacks() { return attacks; }
    public int getHealthBonus() { return healthBonus; }
    public double getHealthMultiplier() { return healthMultiplier; }
    public double getAttackMultiplier() { return attackMultiplier; }

    /** Returns a description of the item for tooltips. */
    public String getDescription() {
        switch (type) {
            case WEAPON:
                // List each attack with damage and cooldown
                StringBuilder sb = new StringBuilder();
                for (Attack atk : attacks) {
                    sb.append(atk.getName())
                            .append(": ")
                            .append(atk.getDamage()).append(" dmg, ")
                            .append(atk.getCooldown()).append("s cd\n");
                }
                return sb.toString().trim();
            case ARMOR:
                return "Health Bonus: +" + healthBonus;
            case AMULET:
                String desc = "";
                if (healthMultiplier != 1.0) {
                    desc += "Health x" + healthMultiplier;
                }
                if (attackMultiplier != 1.0) {
                    if (!desc.isEmpty()) desc += ", ";
                    desc += "Attack x" + attackMultiplier;
                }
                if (desc.isEmpty()) {
                    desc = "No special effects";
                }
                return desc;
            case CONSUMABLE : return "Consumable item (one‐time use)";
            default:
                return "";
        }

    }
    public boolean isConsumable() {
        return consumable;
    }
    @Override
    public String toString() {
        switch (type) {
            case WEAPON:
                return String.format("GameItem{ name='%s', type=WEAPON, attacks=%s }",
                        name, attacks);
            case ARMOR:
                return String.format("GameItem{ name='%s', type=ARMOR, healthBonus=+%d }",
                        name, healthBonus);
            case AMULET:
                return String.format("GameItem{ name='%s', type=AMULET, health×%.2f, attack×%.2f }",
                        name, healthMultiplier, attackMultiplier);
            case CONSUMABLE:
                return String.format("GameItem{ name='%s', type=CONSUMABLE }", name);
            default:
                return String.format("GameItem{ name='%s', type=%s }", name, type);
        }
    }
}
