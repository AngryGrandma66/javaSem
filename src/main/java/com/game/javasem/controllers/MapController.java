package com.game.javasem.controllers;

import com.game.javasem.model.map.DungeonMap;
import com.game.javasem.model.map.Room;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class MapController {
    @FXML
    private GridPane mapGrid;

    /**
     * Called by RoomController.showMap(...)
     */
    public void showMap(DungeonMap dungeon, int currentIndex) {
        mapGrid.getChildren().clear();
        int size = dungeon.getGridSize();
        double cell = 20;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int idx = r * size + c;
                Room room = dungeon.getRooms()[idx];

                Rectangle rect = new Rectangle(cell, cell);

                if (!room.exists()) {
                    // fully transparent
                    rect.setFill(Color.TRANSPARENT);
                    rect.setStroke(Color.TRANSPARENT);
                } else {
                    // normal or boss room
                    rect.setFill(room.isBoss() ? Color.DARKRED : Color.DARKGRAY);
                    rect.setStroke(Color.BLACK);
                    rect.setStrokeWidth(1);
                }

                // highlight the current room
                if (idx == currentIndex) {
                    rect.setStrokeWidth(3);
                    rect.setStroke(Color.GOLD);
                }

                mapGrid.add(rect, c, r);
            }
        }
    }
}

