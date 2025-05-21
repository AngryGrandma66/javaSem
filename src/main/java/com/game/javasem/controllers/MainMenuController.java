package com.game.javasem.controllers;

import com.game.javasem.model.map.DungeonMap;
import com.game.javasem.model.map.Room;
import com.game.javasem.controllers.RoomController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

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
            DungeonMap dungeon = new DungeonMap(/*gridSize=*/10, /*min=*/12, /*max=*/20);
            RoomController rc = loader.getController();
            // 2) Swap scenes on the same stage
            Stage stage = (Stage)newGameButton.getScene().getWindow();
            Scene gameScene = new Scene(gameRoot);
            stage.setScene(gameScene);
            stage.setTitle("Dungeon Explorer");

            // 3) Initialize your map & controller

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