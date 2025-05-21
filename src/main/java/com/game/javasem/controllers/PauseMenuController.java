package com.game.javasem.controllers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.javasem.model.Player;
import com.game.javasem.model.SaveData;
import com.game.javasem.model.SaveManager;
import com.game.javasem.model.map.DungeonMap;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.File;
import com.game.javasem.model.GameItem;

import java.io.IOException;
import java.util.stream.Collectors;

public class PauseMenuController {
    @FXML private Button resumeButton;
    @FXML private Button saveButton;
    @FXML private Button exitButton;

    private RoomController rc;
    private Stage stage;
    private Scene previousScene;

    public void init(RoomController rc, Stage st, Scene prev) {
        this.rc= rc;
        this.stage = st;
        this.previousScene = prev;
    }

    @FXML
    private void handleResume() {
        stage.setScene(previousScene);
    }

    @FXML
    private void handleSave() throws IOException {
        SaveManager saveManager = new SaveManager();
        saveManager.save(rc);
    }
    @FXML
    private void handleExit() {
       rc.backToMainMenu();
    }
}
