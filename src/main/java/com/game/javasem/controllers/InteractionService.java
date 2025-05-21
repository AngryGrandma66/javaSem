package com.game.javasem.controllers;

import com.game.javasem.model.map.Room;
import com.game.javasem.model.mapObjects.MapObject;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class InteractionService {
    private static final double TILE_RADIUS = 1.2;

    private final Pane tileLayer;
    private double cellW, cellH;

    public InteractionService(Pane tileLayer) {
        this.tileLayer = tileLayer;
    }

    public double getCellH() {
        return cellH;
    }

    public double getCellW() {
        return cellW;
    }

    /** Call after each room‐render to refresh tile size. */
    public void updateCellSize(double cellW, double cellH) {
        this.cellW = cellW;
        this.cellH = cellH;
    }

    /**
     * Finds the nearest un‐interacted MapObject
     * within TILE_RADIUS, or null.
     */
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

            if (distSq <= bestDistSq ) {
                bestDistSq = distSq;
                nearest    = obj;
            }
        }

        return nearest;
    }

    /** Triggers .onInteract(...) on the nearest object, if any. */
    public void handleInteraction(ImageView character,RoomController controller) {
        MapObject obj = findNearbyObject(character);
        if (obj != null) {
            obj.onInteract(controller);  // you can pass your controller if needed
        }
    }
    public void removeTileAt(Room currentRoom, MapObject obj) {
        for (int r = 0; r < currentRoom.getLayout().size(); r++) {
            for (int c = 0; c <currentRoom.getLayout().get(r).size(); c++) {
                if (currentRoom.getLayout().get(r).get(c) == obj) {
                    currentRoom.getLayout().get(r).set(c, null);
                    double x = c * cellW;
                    double y = r * cellH;
                    tileLayer.getChildren().removeIf(n -> n.getLayoutX() == x &&
                            n.getLayoutY() == y);
                    return;
                }
            }
        }
    }
}
