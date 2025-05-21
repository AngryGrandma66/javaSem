package com.game.javasem.controllers;

import com.game.javasem.model.mapObjects.Door;
import javafx.animation.AnimationTimer;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovementController {
    private static final Logger log = LoggerFactory.getLogger(MovementController.class);
    private static final double SPEED = 400;

    private final ImageView character;
    private final Pane tileLayer;
    private final AnimationTimer timer;

    private boolean movingUp, movingDown, movingLeft, movingRight;
    private long lastTime;

    public MovementController(ImageView character, Pane tileLayer) {
        this.character = character;
        this.tileLayer   = tileLayer;
        log.info("MovementController initialized");

        this.timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                double delta = (now - lastTime) / 1e9;
                lastTime = now;
                double dx = (movingRight ? SPEED : 0) - (movingLeft ? SPEED : 0);
                double dy = (movingDown  ? SPEED : 0) - (movingUp   ? SPEED : 0);
                if (dx != 0 || dy != 0) {
                    move(dx * delta, dy * delta);
                }
            }
        };
    }

    public void start() {
        lastTime = 0;
        timer.start();
        log.debug("Movement timer started");
    }

    public void stop() {
        timer.stop();
        log.debug("Movement timer stopped");
    }

    public void reset() {
        movingUp = movingDown = movingLeft = movingRight = false;
        lastTime = 0;
        log.debug("Movement flags and timer reset");
    }

    public void setMovingUp(boolean movingUp) {
        this.movingUp = movingUp;
        log.trace("MovingUp set to {}", movingUp);
    }

    public void setMovingDown(boolean movingDown) {
        this.movingDown = movingDown;
        log.trace("MovingDown set to {}", movingDown);
    }

    public void setMovingLeft(boolean movingLeft) {
        this.movingLeft = movingLeft;
        log.trace("MovingLeft set to {}", movingLeft);
    }

    public void setMovingRight(boolean movingRight) {
        this.movingRight = movingRight;
        log.trace("MovingRight set to {}", movingRight);
    }

    public void centerCharacter() {
        double viewW = character.getParent().getLayoutBounds().getWidth();
        double viewH = character.getParent().getLayoutBounds().getHeight();
        double charW = character.getBoundsInLocal().getWidth();
        double charH = character.getBoundsInLocal().getHeight();

        character.setLayoutX((viewW - charW) / 2);
        character.setLayoutY((viewH - charH) / 2);
        log.debug("Character centered at ({}, {})", character.getLayoutX(), character.getLayoutY());
    }

    public void placeAtDoor(Door door, double cellW, double cellH, double gap) {
        log.info("Placing character at door {} (r={}, c={})", door.getSprite(), door.getRow(), door.getCol());
        double charW = character.getBoundsInLocal().getWidth();
        double charH = character.getBoundsInLocal().getHeight();

        double spawnX = 0, spawnY = 0;
        switch (door.getDirection()) {
            case "U" -> {
                spawnX = door.getCol() * cellW + (cellW - charW) * 0.5;
                spawnY = door.getRow() * cellH + cellH + gap;
            }
            case "D" -> {
                spawnX = door.getCol() * cellW + (cellW - charW) * 0.5;
                spawnY = door.getRow() * cellH - charH - gap;
            }
            case "L" -> {
                spawnX = door.getCol() * cellW + cellW + gap;
                spawnY = door.getRow() * cellH + (cellH - charH) * 0.5;
            }
            case "R" -> {
                spawnX = door.getCol() * cellW - charW - gap;
                spawnY = door.getRow() * cellH + (cellH - charH) * 0.5;
            }
            default -> {
                log.warn("Unknown door direction: {}", door.getDirection());
                throw new IllegalArgumentException("Unknown door direction: " + door.getDirection());
            }
        }

        character.setLayoutX(spawnX);
        character.setLayoutY(spawnY);
        log.debug("Character placed at ({}, {}) after door teleport", spawnX, spawnY);
    }

    private void move(double dx, double dy) {
        double prevX = character.getLayoutX(), prevY = character.getLayoutY();
        character.setLayoutX(prevX + dx);
        if (collides()) {
            character.setLayoutX(prevX);
            log.debug("Collision on X axis, reverting to {}", prevX);
        }

        character.setLayoutY(prevY + dy);
        if (collides()) {
            character.setLayoutY(prevY);
            log.debug("Collision on Y axis, reverting to {}", prevY);
        }
    }

    private boolean collides() {
        boolean hit = tileLayer.getChildren().stream()
                .anyMatch(n -> character.getBoundsInParent().intersects(n.getBoundsInParent()));
        if (hit) log.trace("Collision detected at ({}, {})", character.getLayoutX(), character.getLayoutY());
        return hit;
    }
}
