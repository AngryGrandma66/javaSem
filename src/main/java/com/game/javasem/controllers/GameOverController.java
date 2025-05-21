package com.game.javasem.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameOverController {
    private static final Logger log = LoggerFactory.getLogger(GameOverController.class);

    @FXML private Button mainMenuButton;

    @FXML
    private void handleMainMenu() {
        log.info("GameOver -> MainMenu pressed");
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/game/javasem/fxml/MainMenu.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) mainMenuButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Main Menu");
            log.debug("MainMenu scene successfully loaded");
        } catch (Exception e) {
            log.error("Failed to load MainMenu.fxml", e);
        }
    }
}
