package com.game.javasem.controllers;

import com.game.javasem.model.gameState.SaveManager;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PauseMenuController {
    private static final Logger log = LoggerFactory.getLogger(PauseMenuController.class);

    private RoomController rc;
    private Stage stage;
    private Scene previousScene;

    public void init(RoomController rc, Stage st, Scene prev) {
        this.rc = rc;
        this.stage = st;
        this.previousScene = prev;
        log.debug("PauseMenuController initialized with RoomController and previous scene");
    }

    @FXML
    private void handleResume() {
        log.info("Resuming game, returning to room view");
        stage.setScene(previousScene);
    }

    @FXML
    private void handleSave() {
        log.info("User requested game save");
        try {
            new SaveManager().save(rc);
            log.info("Game successfully saved");
        } catch (IOException e) {
            log.error("Failed to save game", e);
        }
    }

    @FXML
    private void handleExit() {
        log.info("Exiting to main menu");
        rc.backToMainMenu();
    }
}
