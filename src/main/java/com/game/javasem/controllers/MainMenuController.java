package com.game.javasem.controllers;

import com.game.javasem.model.GameItem;
import com.game.javasem.model.GameItemFactory;
import com.game.javasem.model.Player;
import com.game.javasem.model.map.DungeonMap;
import com.game.javasem.model.map.Room;
import com.game.javasem.controllers.RoomController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

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
    private void handleLoadGame() {
        System.out.println("Load Game button clicked!");
        // TODO: Add logic to load a saved game
    }
}