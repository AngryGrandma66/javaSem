package com.game.javasem.controllers;

import com.game.javasem.model.map.DungeonMap;
import com.game.javasem.model.map.Room;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapController {
    private static final Logger log = LoggerFactory.getLogger(MapController.class);

    @FXML
    private GridPane mapGrid;


    public void showMap(DungeonMap dungeon, int currentIndex) {
        int size = dungeon.getGridSize();
        log.info("Rendering map: size={} x {}, currentIndex={}", size, size, currentIndex);

        mapGrid.getChildren().clear();

        int totalRooms = size * size;
        long existsCount = 0, bossCount = 0;
        for (Room r : dungeon.getRooms()) {
            if (r.exists()) existsCount++;
            if (r.isBoss()) bossCount++;
        }
        log.debug("Total rooms: {}, existing: {}, bosses: {}", totalRooms, existsCount, bossCount);

        double cell = 20;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int idx = r * size + c;
                Room room = dungeon.getRooms()[idx];

                Rectangle rect = new Rectangle(cell, cell);
                if (!room.exists()) {
                    rect.setFill(Color.TRANSPARENT);
                    rect.setStroke(Color.TRANSPARENT);
                } else {
                    rect.setFill(room.isBoss() ? Color.DARKRED : Color.DARKGRAY);
                    rect.setStroke(Color.BLACK);
                    rect.setStrokeWidth(1);
                }
                if (idx == currentIndex) {
                    rect.setStrokeWidth(3);
                    rect.setStroke(Color.GOLD);
                }

                mapGrid.add(rect, c, r);
            }
        }
        log.info("Map rendering complete");
    }
}
