package com.game.javasem;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;

public class MainController {
    @FXML
    private ImageView mapView;
    @FXML
    private ImageView character;

    // Movement flags
    private boolean movingUp, movingDown, movingLeft, movingRight;
    // Speed in pixels per second
    private final double SPEED = 200;

    private AnimationTimer animationTimer;
    private long lastTimeNano;

    /**
     * Sets up key listeners and starts the animation loop.
     */
    public void bindMovement(Scene scene) {
        // Key pressed: set flags
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case UP, W -> movingUp = true;
                case DOWN, S -> movingDown = true;
                case LEFT, A -> movingLeft = true;
                case RIGHT, D -> movingRight = true;
                default -> {
                }
            }
        });

        // Key released: clear flags
        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case UP, W -> movingUp = false;
                case DOWN, S -> movingDown = false;
                case LEFT, A -> movingLeft = false;
                case RIGHT, D -> movingRight = false;
                default -> {
                }
            }
        });

        // Animation loop for smooth movement
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTimeNano == 0) {
                    lastTimeNano = now;
                    return;
                }
                double deltaSeconds = (now - lastTimeNano) / 1_000_000_000.0;
                lastTimeNano = now;

                double dx = 0;
                double dy = 0;
                if (movingUp) dy -= SPEED * deltaSeconds;
                if (movingDown) dy += SPEED * deltaSeconds;
                if (movingLeft) dx -= SPEED * deltaSeconds;
                if (movingRight) dx += SPEED * deltaSeconds;

                // Update character position
                character.setX(character.getX() + dx);
                character.setY(character.getY() + dy);
            }
        };
        animationTimer.start();
    }
}
