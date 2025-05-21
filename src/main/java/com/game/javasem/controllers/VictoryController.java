package com.game.javasem.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class VictoryController {
    @FXML private Button mainMenuButton;

    @FXML
    private void handleMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/game/javasem/fxml/MainMenu.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) mainMenuButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Main Menu");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}