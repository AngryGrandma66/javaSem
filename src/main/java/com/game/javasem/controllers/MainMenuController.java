package com.game.javasem.controllers;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.game.javasem.model.gameState.RoomState;
import com.game.javasem.model.gameState.SaveData;
import com.game.javasem.model.gameState.SaveManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.javasem.model.*;
import com.game.javasem.model.map.DungeonMap;
import com.game.javasem.model.map.Room;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.util.Map;

public class MainMenuController {
    private static final Logger log = LoggerFactory.getLogger(MainMenuController.class);

    @FXML private Button newGameButton;
    @FXML private Button loadGameButton;
    @FXML private Button logButton;

    @FXML
    private void handleNewGame() {
        log.info("New Game button clicked");
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/game/javasem/fxml/Room.fxml")
            );
            Parent gameRoot = loader.load();
            DungeonMap dungeon = new DungeonMap(10, 12, 20);
            RoomController rc = loader.getController();

            Stage stage = (Stage) newGameButton.getScene().getWindow();
            Scene gameScene = new Scene(gameRoot);
            stage.setScene(gameScene);
            stage.setTitle("Dungeon Explorer");
            log.debug("Loaded Room.fxml and set up scene");

            Map<String, GameItem> allItems = GameItemFactory.loadAll();
            log.debug("Loaded {} items definitions", allItems.size());

            GameItem starterWeapon = allItems.get("ironDagger");
            GameItem starterArmor  = allItems.get("leatherArmor");
            GameItem starterAmulet = allItems.get("silverBracelet");
            log.info("Selected starter gear: {}, {}, {}",
                    starterWeapon.getName(), starterArmor.getName(), starterAmulet.getName());

            Player player = new Player(40, starterWeapon, starterArmor, starterAmulet);
            log.debug("Created new Player: {}", player);

            rc.setPlayer(player);
            rc.setDungeonMap(dungeon);
            rc.initialize();
            rc.setScene(gameScene);
            rc.showRoom(dungeon.getStartRoom());
            log.info("Game initialized, showing starting room");

        } catch (Exception e) {
            log.error("Failed to start new game", e);
        }
    }

    @FXML
    private void handleLoadGame() {
        log.info("Load Game button clicked");
        try {
            SaveData sd = new SaveManager().load();
            log.debug("Loaded SaveData for roomIndex={}, inventory size={}",
                    sd.currentRoomIndex, sd.inventoryItemIds.size());

            Room[] restored = sd.dungeon.rooms.stream()
                    .map(RoomState::toRoom)
                    .toArray(Room[]::new);
            log.debug("Reconstructed {} Room instances", restored.length);

            DungeonMap dm = new DungeonMap(
                    sd.dungeon.gridSize,
                    restored,
                    sd.currentRoomIndex
            );
            log.info("DungeonMap restored (size={}, startIndex={})",
                    dm.getGridSize(), sd.currentRoomIndex);

            Map<String, GameItem> allItems = GameItemFactory.loadAll();
            Player p = new Player(
                    40,
                    allItems.get(sd.equippedWeaponId),
                    allItems.get(sd.equippedArmorId),
                    allItems.get(sd.equippedAmuletId)
            );
            sd.inventoryItemIds.forEach(id -> {
                GameItem it = allItems.get(id);
                p.getInventory().addItem(it);
            });
            log.info("Player and inventory restored ({} items)", p.getInventory().getItems().size());

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/game/javasem/fxml/Room.fxml"));
            Parent root = loader.load();

            RoomController rc = loader.getController();
            Scene s = new Scene(root);
            Stage primary = (Stage) loadGameButton.getScene().getWindow();
            primary.setScene(s);
            primary.setTitle("Dungeon Explorer (loaded)");
            log.debug("Room.fxml loaded for saved game");

            rc.setPlayer(p);
            rc.setDungeonMap(dm);
            rc.initialize();
            rc.setScene(s);
            rc.showRoom(dm.getRooms()[sd.currentRoomIndex]);
            log.info("Saved game shown at room index {}", sd.currentRoomIndex);

        } catch (Exception e) {
            log.error("Failed to load saved game", e);
        }
    }

    @FXML
    private void handleToggleLogging() {
        LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
        var root = ctx.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        Level current = root.getLevel();
        Level next;
        String btnText;

        if (current == Level.DEBUG) {
            next = Level.INFO;
            btnText = "Disable Debug";
        } else if (current == Level.INFO) {
            next = Level.OFF;
            btnText = "Enable ALL Logs";
        } else {
            next = Level.DEBUG;
            btnText = "Disable All Logs";
        }

        root.setLevel(next);
        log.info("Log level changed from {} to {}", current, next);
        logButton.setText(btnText);
    }
}
