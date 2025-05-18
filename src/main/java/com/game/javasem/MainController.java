package com.game.javasem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class MainController {
    @FXML private ImageView mapView;
    @FXML private ImageView character;
    @FXML private Pane tileLayer;

    // Movement flags
    private boolean movingUp, movingDown, movingLeft, movingRight;
    private final double SPEED = 200; // px/s

    private AnimationTimer animationTimer;
    private long lastTime;

    /**
     * Called by FXMLLoader after loading FXML. Initializes grid and movement.
     */
    public void initialize(Scene scene) {
        loadGridLayout("layouts/Room1.json");
        bindMovement(scene);
    }

    private void loadGridLayout(String jsonPath) {
        try (InputStream is = getClass().getResourceAsStream(jsonPath);
             Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            ObjectMapper mapper = new ObjectMapper();
            List<List<String>> layout = mapper.readValue(
                    reader,
                    new TypeReference<List<List<String>>>() {}
            );

            int rows = layout.size();
            int cols = layout.get(0).size();
            double cellW = mapView.getFitWidth() / cols;
            double cellH = mapView.getFitHeight() / rows;

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    String spriteName = layout.get(r).get(c);
                    if (spriteName != null && !spriteName.isEmpty()) {
                        Image img = new Image(
                                Objects.requireNonNull(getClass().getResourceAsStream("images/" + spriteName))
                        );
                        ImageView iv = new ImageView(img);
                        iv.setFitWidth(cellW);
                        iv.setFitHeight(cellH);
                        iv.setLayoutX(c * cellW);
                        iv.setLayoutY(r * cellH);
                        tileLayer.getChildren().add(iv);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindMovement(Scene scene) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case UP, W -> movingUp = true;
                case DOWN, S -> movingDown = true;
                case LEFT, A -> movingLeft = true;
                case RIGHT, D -> movingRight = true;
                default -> {}
            }
        });
        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case UP, W -> movingUp = false;
                case DOWN, S -> movingDown = false;
                case LEFT, A -> movingLeft = false;
                case RIGHT, D -> movingRight = false;
                default -> {}
            }
        });
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                double delta = (now - lastTime) / 1e9;
                lastTime = now;
                double dx = 0, dy = 0;
                if (movingUp)    dy -= SPEED * delta;
                if (movingDown)  dy += SPEED * delta;
                if (movingLeft)  dx -= SPEED * delta;
                if (movingRight) dx += SPEED * delta;
                character.setX(character.getX() + dx);
                character.setY(character.getY() + dy);
            }
        };
        animationTimer.start();
    }
}
