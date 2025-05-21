package com.game.javasem.model.gameState;

import java.util.List;

public class SaveData {
    public DungeonState dungeon;
    public int currentRoomIndex;
    public List<String> inventoryItemIds;
    public String equippedWeaponId;
    public String equippedArmorId;
    public String equippedAmuletId;
}