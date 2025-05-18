package com.game.javasem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.Node;
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

    private boolean movingUp, movingDown, movingLeft, movingRight;
    private final double SPEED = 200; // pixels per second

    private AnimationTimer animationTimer;
    private long lastTime;

    /** Called by FXMLLoader after FXML load */
    public void initialize(Scene scene) {
        loadGridLayout("layouts/Room1.json");
        bindMovement(scene);
    }

    /**
     * Loads grid layout from JSON and populates tileLayer with ImageViews
     */
    private void loadGridLayout(String jsonPath) {
        try (InputStream is = getClass().getResourceAsStream(jsonPath)) {
            if (is == null) {
                System.err.println("Layout JSON not found: " + jsonPath);
                return;
            }
            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            List<List<String>> layout = mapper.readValue(reader, new TypeReference<>() {});

            int rows = layout.size();
            int cols = layout.get(0).size();
            double cellW = mapView.getFitWidth() / cols;
            double cellH = mapView.getFitHeight() / rows;

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    String sprite = layout.get(r).get(c);
                    if (sprite != null && !sprite.isEmpty()) {
                        Image img = new Image(getClass().getResourceAsStream("images/" + sprite));
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

    /**
     * Sets up key handling and starts the animation loop
     */
    private void bindMovement(Scene scene) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case UP, W    -> movingUp = true;
                case DOWN, S  -> movingDown = true;
                case LEFT, A  -> movingLeft = true;
                case RIGHT, D -> movingRight = true;
                default -> {}
            }
        });
        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case UP, W    -> movingUp = false;
                case DOWN, S  -> movingDown = false;
                case LEFT, A  -> movingLeft = false;
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

                double dx = (movingRight ? SPEED : 0) - (movingLeft ? SPEED : 0);
                double dy = (movingDown ? SPEED : 0) - (movingUp ? SPEED : 0);

                attemptMove(dx * delta, dy * delta);
            }
        };
        animationTimer.start();
    }

    /**
     * Tries to move the character by dx, dy, with bounding-box collision against tiles
     */
    private void attemptMove(double dx, double dy) {
        // Horizontal move
        character.setX(character.getX() + dx);
        if (checkCollision()) {
            character.setX(character.getX() - dx);
        }
        // Vertical move
        character.setY(character.getY() + dy);
        if (checkCollision()) {
            character.setY(character.getY() - dy);
        }
    }

    /**
     * Returns true if character intersects any impassable tile
     */
    private boolean checkCollision() {
        for (Node tile : tileLayer.getChildren()) {
            if (character.getBoundsInParent().intersects(tile.getBoundsInParent())) {
                return true;
            }
        }
        return false;
    }
}
