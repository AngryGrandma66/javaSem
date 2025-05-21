package com.game.javasem.controllers;

import com.game.javasem.model.*;
import com.game.javasem.model.map.DungeonMap;
import com.game.javasem.model.map.Room;
import com.game.javasem.controllers.RoomController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

public class MainMenuController {

    @FXML private Button newGameButton;
    @FXML private Button loadGameButton;

    @FXML
    private void initialize() {
        // no-op
    }

    @FXML
    private void handleNewGame() {
        try {
            // 1) Load your RoomView.fxml (make sure the path is correct relative to your classpath)
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/game/javasem/fxml/Room.fxml")
            );
            Parent gameRoot = loader.load();
            DungeonMap dungeon = new DungeonMap(10, 12,20);
            RoomController rc = loader.getController();
            // 2) Swap scenes on the same stage
            Stage stage = (Stage)newGameButton.getScene().getWindow();
            Scene gameScene = new Scene(gameRoot);
            stage.setScene(gameScene);
            stage.setTitle("Dungeon Explorer");
            Map<String, GameItem> allItems = GameItemFactory.loadAll();

// 2) Pick defaults by their JSON keys:
            GameItem starterWeapon = allItems.get("ironDagger");
            GameItem starterArmor  = allItems.get("leatherArmor");
            GameItem starterAmulet = allItems.get("silverBracelet");

// 3) Create the player *with* defaults
            Player player = new Player(
                    40,
                    starterWeapon,
                    starterArmor,
                    starterAmulet
            );
            // 3) Initialize your map & controller
            rc.setPlayer(player);              // <<— inject the player here
            rc.setDungeonMap(dungeon);
            rc.initialize();
            // bind key‐handlers
            rc.setScene(gameScene);
            // show the central starting room
            rc.showRoom(dungeon.getStartRoom());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoadGame() throws Exception {
        SaveData sd = new SaveManager().load();

        // 1) make an “empty” map of the right size:
        Room[] restored = sd.dungeon.rooms.stream()
                .map(RoomState::toRoom)      // or new Room(rs)
                .toArray(Room[]::new);

// 2) Create a fresh DungeonMap pointing at exactly those Room instances:
        DungeonMap dm = new DungeonMap(
                sd.dungeon.gridSize,        // size
                restored,                     // your new constructor taking Room[]
                sd.currentRoomIndex
        );

        Map<String, GameItem> allItems = GameItemFactory.loadAll();
        Player p = new Player(
                40,
                allItems.get(sd.equippedWeaponId),
                allItems.get(sd.equippedArmorId),
                allItems.get(sd.equippedAmuletId)
        );
        // fill inventory:
        for (String id : sd.inventoryItemIds) {
            GameItem it = GameItemFactory.loadAll().get(id);
            p.getInventory().addItem(it);
        }


        // 4) boot up the RoomController exactly like NewGame:
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/game/javasem/fxml/Room.fxml"));
        Parent root = loader.load();

        RoomController rc = loader.getController();


        Scene s = new Scene(root);
        Stage primary= (Stage)loadGameButton.getScene().getWindow();        primary.setScene(s);
        primary.setTitle("Dungeon Explorer (loaded)");
        primary.setScene(s);
        rc.setPlayer(p);
        rc.setDungeonMap(dm);
        rc.initialize();
        rc.setScene(s);
        Room start = dm.getRooms()[sd.currentRoomIndex];
        rc.showRoom(start);
    }
}