package com.game.javasem.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.javasem.controllers.RoomController;
import com.game.javasem.model.map.DungeonMap;
import com.game.javasem.model.map.Room;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

public class SaveManager {
    private static final Path SAVE_FILE = Paths.get("savegame.json");
    private final ObjectMapper M = new ObjectMapper();

    public void save(RoomController rc) throws IOException {
        SaveData sd = new SaveData();
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
        System.out.println("Game saved to " + SAVE_FILE);
    }

    public SaveData load() throws IOException {
        return M.readValue(SAVE_FILE.toFile(), SaveData.class);
    }
}
