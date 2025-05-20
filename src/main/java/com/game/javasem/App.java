package com.game.javasem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("fxml/Room.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setTitle("JavaFX Map Movement Example");
        stage.setScene(scene);
        stage.show();
        // Pass scene to controller to capture key presses
        RoomController controller = fxmlLoader.getController();
        controller.initialize(scene);


    }

    public static void main(String[] args) {
        launch();
    }
}