package com.game.javasem.controllers;

import com.game.javasem.model.mapObjects.Door;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class MovementController {
    private static final double SPEED = 400;

    private final ImageView character;
    private final Pane      tileLayer;
    private AnimationTimer  timer;

    private boolean movingUp, movingDown, movingLeft, movingRight;
    private long    lastTime;

    public boolean isMovingRight() {
        return movingRight;
    }

    public void setMovingRight(boolean movingRight) {
        this.movingRight = movingRight;
    }

    public boolean isMovingLeft() {
        return movingLeft;
    }

    public void setMovingLeft(boolean movingLeft) {
        this.movingLeft = movingLeft;
    }

    public boolean isMovingDown() {
        return movingDown;
    }

    public void setMovingDown(boolean movingDown) {
        this.movingDown = movingDown;
    }

    public boolean isMovingUp() {
        return movingUp;
    }

    public void setMovingUp(boolean movingUp) {
        this.movingUp = movingUp;
    }

    public MovementController(ImageView character, Pane tileLayer) {
        this.character = character;
        this.tileLayer = tileLayer;

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

                move(dx * delta, dy * delta);
            }
        };
    }


    public void placeAtDoor(Door door, double cellW, double cellH, double gap) {
        // get character dimensions
        double charW = character.getBoundsInLocal().getWidth();
        double charH = character.getBoundsInLocal().getHeight();

        // compute spawn coords
        double spawnX = 0, spawnY = 0;
        switch (door.getDirection()) {
            case "U" -> {
                // door on top edge of room, so spawn just below
                spawnX = door.getCol() * cellW + (cellW - charW) * 0.5;
                spawnY = door.getRow() * cellH + cellH + gap;
            }
            case "D" -> {
                // door on bottom edge, spawn just above
                spawnX = door.getCol() * cellW + (cellW - charW) * 0.5;
                spawnY = door.getRow() * cellH - charH - gap;
            }
            case "L" -> {
                // door on left edge, spawn just to the right
                spawnX = door.getCol() * cellW + cellW + gap;
                spawnY = door.getRow() * cellH + (cellH - charH) * 0.5;
            }
            case "R" -> {
                // door on right edge, spawn just to the left
                spawnX = door.getCol() * cellW - charW - gap;
                spawnY = door.getRow() * cellH + (cellH - charH) * 0.5;
            }
            default -> throw new IllegalArgumentException("Unknown door direction: " + door.getDirection());
        }

        // apply it
        character.setLayoutX(spawnX);
        character.setLayoutY(spawnY);
    }
    /** Start the animation loop. */
    public void start() {
        lastTime = 0;
        timer.start();
    }

    /** Stop it so the character can’t move. */
    public void stop() {
        timer.stop();
    }

    /** Zero out any pending movement so you don’t “jump” on resume. */
    public void reset() {
        movingUp = movingDown = movingLeft = movingRight = false;
        lastTime = 0;
    }

    /** Move the character by dx/dy, undo if we hit a wall. */
    private void move(double dx, double dy) {
        // X
        character.setLayoutX(character.getLayoutX() + dx);
        if (collides()) character.setLayoutX(character.getLayoutX() - dx);

        // Y
        character.setLayoutY(character.getLayoutY() + dy);
        if (collides()) character.setLayoutY(character.getLayoutY() - dy);
    }

    /** A simple AABB check against every tile in the room. */
    private boolean collides() {
        return tileLayer.getChildren().stream().anyMatch(n ->
                character.getBoundsInParent().intersects(n.getBoundsInParent())
        );
    }

    /** Moves the character to the exact center of the view. */
    public void centerCharacter() {
        double viewW = character.getParent().getLayoutBounds().getWidth();
        double viewH = character.getParent().getLayoutBounds().getHeight();
        double charW = character.getBoundsInLocal().getWidth();
        double charH = character.getBoundsInLocal().getHeight();

        character.setLayoutX((viewW - charW) / 2);
        character.setLayoutY((viewH - charH) / 2);
    }
}
