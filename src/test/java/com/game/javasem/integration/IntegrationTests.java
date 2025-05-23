package com.game.javasem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.javasem.model.GameItem;
import com.game.javasem.model.GameItemFactory;
import com.game.javasem.model.Player;
import com.game.javasem.model.map.DungeonMap;
import com.game.javasem.model.gameState.SaveData;
import com.game.javasem.model.gameState.SaveManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTests {

    @Test
    void testDungeonMapGenerationCreatesValidRoomsAndBoss() throws Exception {
        int gridSize = 8;
        int minRooms = 5;
        int maxRooms = 10;
        DungeonMap dungeonMap = new DungeonMap(gridSize, minRooms, maxRooms);

        // Check room count
        long existsCount = 0;
        int bossCount = 0;
        for (var room : dungeonMap.getRooms()) {
            if (room.exists()) existsCount++;
            if (room.isBoss()) bossCount++;
        }
        assertTrue(existsCount >= minRooms && existsCount <= maxRooms,
                "Number of rooms should be within configured bounds");
        assertEquals(1, bossCount, "Exactly one boss room should be assigned");
    }

    @Test
    void testGameItemFactoryLoadsItemsAndPlayerCanEquip() throws IOException {
        Map<String, GameItem> items = GameItemFactory.loadAll();
        assertFalse(items.isEmpty(), "GameItemFactory should load at least one item definition");

        // Pick a weapon, armor, amulet
        GameItem weapon = items.values().stream()
                .filter(i -> i.getType() == GameItem.Type.WEAPON)
                .findFirst().orElseThrow();
        GameItem armor = items.values().stream()
                .filter(i -> i.getType() == GameItem.Type.ARMOR)
                .findFirst().orElseThrow();
        GameItem amulet = items.values().stream()
                .filter(i -> i.getType() == GameItem.Type.AMULET)
                .findFirst().orElseThrow();

        Player player = new Player(20, weapon, armor, amulet);
        // After creation, equipped items should be removed from inventory
        long inInventory = player.getInventory().getItems().stream()
                .filter(i -> i == weapon || i == armor || i == amulet)
                .count();
        assertEquals(0, inInventory, "Equipped items should not remain in inventory");

        // Unequip armor and check it returns
        player.unequipItem(GameItem.Type.ARMOR);
        assertTrue(player.getInventory().getItems().contains(armor),
                "Unequipped armor should be back in inventory");
    }

    @Test
    void testSaveAndLoadCyclePersistsState(@TempDir Path tempDir) throws Exception {
        // Redirect user.dir so savegame.json is written to tempDir
        String originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        try {
            // Setup initial game state
            Map<String, GameItem> items = GameItemFactory.loadAll();
            Player player = new Player(30,
                    items.get("ironDagger"),
                    items.get("leatherArmor"),
                    items.get("silverBracelet"));
            DungeonMap dungeonMap = new DungeonMap(6, 4, 6);

            // Save using a stub controller
            SaveManager saveManager = new SaveManager();
            saveManager.save(new RoomControllerStub(dungeonMap, player, dungeonMap.getStartIndex()));

            // Attempt to load and verify persisted state
            SaveData loaded = assertDoesNotThrow(() -> saveManager.load(),
                    "Loading after save should not throw");
            assertNotNull(loaded.dungeon, "Loaded dungeon state should not be null");
            assertEquals(dungeonMap.getGridSize(), loaded.dungeon.gridSize,
                    "Loaded gridSize should match original");
            assertEquals(player.getInventory().getItems().size(),
                    loaded.inventoryItemIds.size(), "Inventory size should persist");
            assertEquals(dungeonMap.getStartIndex(), loaded.currentRoomIndex,
                    "Current room index should persist");
        } finally {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    void testRoomStateSerializationRoundTrip() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        // Create a simple SaveData
        SaveData sd = new SaveData();
        sd.currentRoomIndex = 3;
        sd.equippedWeaponId = "ironDagger";
        sd.equippedArmorId = "leatherArmor";
        sd.equippedAmuletId = "silverBracelet";
        sd.inventoryItemIds = java.util.List.of("coin", "key");
        sd.dungeon = new com.game.javasem.model.gameState.DungeonState();
        sd.dungeon.gridSize = 5;
        sd.dungeon.rooms = java.util.Collections.emptyList();

        // Serialize
        String json = mapper.writeValueAsString(sd);
        assertTrue(json.contains("currentRoomIndex"));

        // Deserialize
        SaveData deserialized = mapper.readValue(json, SaveData.class);
        assertEquals(sd.currentRoomIndex, deserialized.currentRoomIndex);
        assertEquals(sd.equippedWeaponId, deserialized.equippedWeaponId);
        assertIterableEquals(sd.inventoryItemIds, deserialized.inventoryItemIds);
    }

    /**
     * Stub to simulate RoomController for SaveManager.save
     */
    static class RoomControllerStub extends com.game.javasem.controllers.RoomController {
        private final DungeonMap dungeonMap;
        private final Player player;
        private final int currentRoomIndex;

        public RoomControllerStub(DungeonMap dungeonMap, Player player, int currentRoomIndex) {
            this.dungeonMap = dungeonMap;
            this.player = player;
            this.currentRoomIndex = currentRoomIndex;
        }

        @Override
        public com.game.javasem.model.map.Room getCurrentRoom() {
            return dungeonMap.getRooms()[currentRoomIndex];
        }

        @Override
        public Player getPlayer() {
            return player;
        }

        @Override
        public DungeonMap getDungeonMap() {
            return dungeonMap;
        }
    }
}
