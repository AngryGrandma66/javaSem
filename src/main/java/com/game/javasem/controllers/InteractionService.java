package com.game.javasem.controllers;

import com.game.javasem.model.map.Room;
import com.game.javasem.model.mapObjects.MapObject;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteractionService {
    private static final Logger log = LoggerFactory.getLogger(InteractionService.class);
    private static final double TILE_RADIUS = 1.2;

    private final Pane tileLayer;
    private double cellW, cellH;

    public InteractionService(Pane tileLayer) {
        this.tileLayer = tileLayer;
        log.debug("InteractionService initialized with tileLayer {}", tileLayer);
    }

    public double getCellH() {
        return cellH;
    }

    public double getCellW() {
        return cellW;
    }

    public void updateCellSize(double cellW, double cellH) {
        this.cellW = cellW;
        this.cellH = cellH;
        log.debug("Cell size updated to ({}, {})", cellW, cellH);
    }

    public MapObject findNearbyObject(ImageView character) {
        Bounds cb = character.getBoundsInParent();
        double cx = cb.getMinX() + cb.getWidth()  / 2;
        double cy = cb.getMinY() + cb.getHeight() / 2;

        MapObject nearest = null;
        double bestDistSq = TILE_RADIUS * TILE_RADIUS;

        for (Node n : tileLayer.getChildren()) {
            if (!(n instanceof ImageView iv)) continue;
            if (!(iv.getUserData() instanceof MapObject obj)) continue;

            Bounds ob = iv.getBoundsInParent();
            double ox = ob.getMinX() + ob.getWidth()  / 2;
            double oy = ob.getMinY() + ob.getHeight() / 2;

            double nx = (ox - cx) / cellW;
            double ny = (oy - cy) / cellH;
            double distSq = nx*nx + ny*ny;

            if (distSq <= bestDistSq) {
                bestDistSq = distSq;
                nearest    = obj;
            }
        }

        log.debug("findNearbyObject: nearest={} at distSq={}", nearest, bestDistSq);
        return nearest;
    }

    public void handleInteraction(ImageView character, RoomController controller) {
        log.info("Interaction requested by character at {}, {}", character.getLayoutX(), character.getLayoutY());
        MapObject obj = findNearbyObject(character);
        if (obj != null) {
            log.info("Interacting with object: {}", obj);
            obj.onInteract(controller);
        } else {
            log.info("No interactable object nearby.");
        }
    }

    public void removeTileAt(Room currentRoom, MapObject obj) {
        log.debug("Removing tile for object {} in room {}", obj, currentRoom.getIndex());
        for (int r = 0; r < currentRoom.getLayout().size(); r++) {
            for (int c = 0; c < currentRoom.getLayout().get(r).size(); c++) {
                if (currentRoom.getLayout().get(r).get(c) == obj) {
                    currentRoom.getLayout().get(r).set(c, null);
                    double x = c * cellW;
                    double y = r * cellH;
                    boolean removed = tileLayer.getChildren().removeIf(n ->
                            n.getLayoutX() == x && n.getLayoutY() == y
                    );
                    log.debug("Tile at ({}, {}) removed: {}", c, r, removed);
                    return;
                }
            }
        }
        log.warn("Attempted to remove object {} not found in room layout", obj);
    }
}
