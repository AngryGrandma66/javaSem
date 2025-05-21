package com.game.javasem.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import com.game.javasem.model.Player;
import com.game.javasem.model.map.DungeonMap;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class PauseMenuController {
    @FXML private Button resumeButton;
    @FXML private Button saveButton;
    @FXML private Button exitButton;

    private RoomController roomController;
    private Stage stage;
    private Scene previousScene;

    public void init(RoomController rc, Stage st, Scene prev) {
        this.roomController = rc;
        this.stage = st;
        this.previousScene = prev;
    }

    @FXML
    private void handleResume() {
        stage.setScene(previousScene);
    }

    @FXML
    private void handleSave() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("savegame.dat"))) {
            // save dungeon map
            DungeonMap map = roomController.getDungeonMap();
            out.writeObject(map);
            // save inventory
            Player player = roomController.getPlayer();
            out.writeObject(player.getInventory());
            // save character position
            out.writeDouble(roomController.getCharLayoutX());
            out.writeDouble(roomController.getCharLayoutY());
            System.out.println("Game saved.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExit() {
        roomController.backToMainMenu();
    }
}
