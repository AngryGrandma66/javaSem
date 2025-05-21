package com.game.javasem.model.gameState;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.javasem.controllers.RoomController;
import com.game.javasem.model.GameItem;
import com.game.javasem.model.map.DungeonMap;
import com.game.javasem.model.map.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

public class SaveManager {
    private static final Logger log = LoggerFactory.getLogger(SaveManager.class);

    private static final Path SAVE_FILE = Paths.get("savegame.json");
    private final ObjectMapper M = new ObjectMapper();

    public void save(RoomController rc) throws IOException {
        log.info("Starting save to file: {}", SAVE_FILE);

        SaveData sd = new SaveData();
        try {
            sd.currentRoomIndex = rc.getCurrentRoom().getIndex();
            sd.inventoryItemIds = rc.getPlayer()
                    .getInventory()
                    .getItems()
                    .stream()
                    .map(GameItem::getName)  // or getYourId()
                    .collect(Collectors.toList());
            sd.equippedWeaponId = rc.getPlayer().getEquippedWeapon().getName();
            sd.equippedArmorId = rc.getPlayer().getEquippedArmor().getName();
            sd.equippedAmuletId = rc.getPlayer().getEquippedAmulet().getName();

            DungeonMap dm = rc.getDungeonMap();
            DungeonState ds = new DungeonState();
            ds.gridSize = dm.getGridSize();
            ds.rooms = new ArrayList<>();

            for (Room r : dm.getRooms()) {
                RoomState rs = new RoomState();
                rs.index = r.getIndex();
                rs.exists = r.exists();
                rs.gridSize = dm.getGridSize();
                rs.boss = r.isBoss();
                rs.doors = new ArrayList<>(r.getDoors());
                rs.layoutFlags = new HashSet<>(r.getLayoutFlags());
                rs.layout = r.getLayout();
                ds.rooms.add(rs);
            }

            sd.dungeon = ds;
            M.writeValue(SAVE_FILE.toFile(), sd);

            log.info("Game saved successfully to {}", SAVE_FILE);
        } catch (Exception e) {
            log.error("Failed to save game state", e);
            throw e;
        }
    }

    public SaveData load() throws IOException {
        log.info("Loading save file: {}", SAVE_FILE);
        try {
            SaveData sd = M.readValue(SAVE_FILE.toFile(), SaveData.class);
            log.info("Game loaded successfully from {}", SAVE_FILE);
            return sd;
        } catch (Exception e) {
            log.error("Failed to load game state", e);
            throw e;
        }
    }
}
