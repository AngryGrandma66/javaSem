// Player.java
package com.game.javasem.model;

public class Player {
    private final Inventory inventory = new Inventory();
    private GameItem equippedWeapon;
    private GameItem equippedArmor;
    private GameItem equippedAmulet;
    private int baseMaxHealth;
    private int currentHealth;

    /**
     * Create a new player with base health and default gear.
     * The default gear will be added to the inventory then auto-equipped.
     */
    public Player(int baseMaxHealth,
                  GameItem defaultWeapon,
                  GameItem defaultArmor,
                  GameItem defaultAmulet) {
        this.baseMaxHealth = baseMaxHealth;
        // initial health before equipment effects
        this.currentHealth = baseMaxHealth;

        // equip in the order Weapon → Armor → Amulet
        if (defaultWeapon != null) {
            inventory.addItem(defaultWeapon);
            equipItem(defaultWeapon);
        }
        if (defaultArmor != null) {
            inventory.addItem(defaultArmor);
            equipItem(defaultArmor);
        }
        if (defaultAmulet != null) {
            inventory.addItem(defaultAmulet);
            equipItem(defaultAmulet);
        }
    }


    public Inventory getInventory() {
        return inventory;
    }

    public GameItem getEquippedWeapon() {
        return equippedWeapon;
    }

    public GameItem getEquippedArmor() {
        return equippedArmor;
    }

    public GameItem getEquippedAmulet() {
        return equippedAmulet;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        int max = baseMaxHealth;
        if (equippedArmor != null) {
            max += equippedArmor.getHealthBonus();
        }
        if (equippedAmulet != null) {
            max = (int) Math.round(max * equippedAmulet.getHealthMultiplier());
        }
        return max;
    }

    public void equipItem(GameItem item) {
        if (!inventory.getItems().contains(item)) return;
        switch (item.getType()) {
            case WEAPON -> {
                if (equippedWeapon != null)
                    inventory.addItem(equippedWeapon);
                equippedWeapon = item;
                inventory.removeItem(item);
            }
            case ARMOR -> {
                if (equippedArmor != null)
                    inventory.addItem(equippedArmor);
                equippedArmor = item;
                inventory.removeItem(item);
            }
            case AMULET -> {
                if (equippedAmulet != null)
                    inventory.addItem(equippedAmulet);
                equippedAmulet = item;
                inventory.removeItem(item);
            }
            default -> { /* consumables etc. */ }
        }
    }

    public void unequipItem(GameItem.Type slot) {
        switch (slot) {
            case WEAPON -> {
                if (equippedWeapon != null) {
                    inventory.addItem(equippedWeapon);
                    equippedWeapon = null;
                }
            }
            case ARMOR -> {
                if (equippedArmor != null) {
                    inventory.addItem(equippedArmor);
                    equippedArmor = null;
                }
            }
            case AMULET -> {
                if (equippedAmulet != null) {
                    inventory.addItem(equippedAmulet);
                    equippedAmulet = null;
                }
            }
            default -> {
            }
        }
    }
}
