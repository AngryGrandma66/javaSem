package com.game.javasem.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.game.javasem.model.map.DungeonMap;
import com.game.javasem.model.map.Room;
import com.game.javasem.model.mapObjects.*;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RoomController {
    private static final double TILE_RADIUS = 1.2;
    @FXML
    private ImageView mapView;
    @FXML
    private ImageView character;
    @FXML
    private Pane tileLayer;

    @FXML private AnchorPane   roomPane;
    @FXML private StackPane mapPane;
    private MapController      mapController;

    private Stage mapStage;
    private Scene scene;          // ← store the scene here
    private DungeonMap dungeonMap;
    private Room currentRoom;
    private double SPEED = 500;
    private boolean movingUp, movingDown, movingLeft, movingRight;
    private AnimationTimer timer;
    private long lastTime;

    private int rows;
    private int cols;
    private double cellW;
    private double cellH;


    private Map<String, Map<String, Object>> obstacleDefs;
    private Map<String, Map<String, Object>> itemDefs;
    private Map<String, Map<String, Object>> enemyDefs;
    private Map<String, Map<String, Object>> doorDefs;

    private List<List<MapObject>> layout;

    public void initialize(Scene scene) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/game/javasem/fxml/MapView.fxml")
            );
            Parent mapRoot = loader.load();
            mapController = loader.getController();
            mapPane.getChildren().add(mapRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mapPane.setVisible(false);
        mapPane.setManaged(false);
        loadDefinitions();
        bindKeys(scene);
        timer = new AnimationTimer() {
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
                moveWithCollision(dx * delta, dy * delta);
                logNearbyInteractable();
            }
        };
        timer.start();
    }

    public void setDungeonMap(DungeonMap dungeonMap) {
        this.dungeonMap = dungeonMap;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        bindKeys(scene);             // bind your key‐handling here
    }

    /**
     * called from MainMenuController
     */
    public void showRoom(Room room) {
        this.currentRoom = room;
        List<List<MapObject>> raw = room.getLayout();
        if (raw == null || raw.isEmpty()) {
            System.err.println("Room has no layout!");
            return;
        }
        renderRoom();            // your existing render logic
    }

    private void loadDefinitions() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream in = getClass().getResourceAsStream("/com/game/javasem/data/obstacles.json")) {
            if (in != null) obstacleDefs = mapper.readValue(in, new TypeReference<>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (InputStream in = getClass().getResourceAsStream("/com/game/javasem/data/items.json")) {
            if (in != null) itemDefs = mapper.readValue(in, new TypeReference<>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (InputStream in = getClass().getResourceAsStream("/com/game/javasem/data/enemies.json")) {
            if (in != null) enemyDefs = mapper.readValue(in, new TypeReference<>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (InputStream in = getClass().getResourceAsStream("/com/game/javasem/data/doors.json")) {
            if (in != null) doorDefs = mapper.readValue(in, new TypeReference<>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setRoom(Room room) {
        this.currentRoom = room;
        renderRoom();
    }

    private void renderRoom() {
        System.out.println(currentRoom.getIndex());
        System.out.println(currentRoom.getLayoutFlags());
        tileLayer.getChildren().clear();

        layout = currentRoom.getLayout();
        rows = layout.size();
        cols = layout.getFirst().size();
        cellW = mapView.getFitWidth() / cols;
        cellH = mapView.getFitHeight() / rows;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                MapObject obj = layout.get(r).get(c);
                if (obj == null) continue;
                if (obj instanceof Door door && doorDefs != null) {
                    Map<String, Object> def = doorDefs.get(door.getSprite());
                    if (def != null) {
                        door.setDirection((String) def.get("direction"));
                        door.setPosition(r, c);
                    }
                }
                String sprite = getSpriteFor(obj);// your MapObject subclasses return the correct sprite URL
                if (sprite == null) continue;
                ImageView iv = new ImageView(new Image(Objects.requireNonNull(getClass().getResource(
                        "/com/game/javasem/images/" + sprite)).toExternalForm()));
                iv.setFitWidth(cellW);
                iv.setFitHeight(cellH);
                iv.setLayoutX(c * cellW);
                iv.setLayoutY(r * cellH);
                iv.setUserData(obj);
                tileLayer.getChildren().add(iv);
            }
        }
    }

    private String getSpriteFor(MapObject obj) {
        String key;
        if (obj instanceof Obstacle && obstacleDefs != null) {
            key = obj.getSprite();
            Map<String, Object> def = obstacleDefs.get(key);
            if (def != null) return (String) def.get("sprite");
        } else if (obj instanceof Item && itemDefs != null) {
            key = obj.getType();
            Map<String, Object> def = itemDefs.get(key);
            if (def != null) return (String) def.get("sprite");
        } else if (obj instanceof Enemy && enemyDefs != null) {
            key = obj.getType();
            Map<String, Object> def = enemyDefs.get(key);
            if (def != null) return (String) def.get("sprite");
        } else if (obj instanceof Door && enemyDefs != null) {
            key = obj.getSprite();
            Map<String, Object> def = doorDefs.get(key);
            if (def != null) return (String) def.get("sprite");
        }
        return null;
    }

    private void bindKeys(Scene s) {
        s.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case M -> {
                    toggleMap();
                    //e.consume();
                }
                case UP, W    -> movingUp    = true;
                case DOWN, S  -> movingDown  = true;
                case LEFT, A  -> movingLeft  = true;
                case RIGHT, D -> movingRight = true;
                case E        -> handleInteraction();
            }
        });
        s.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case UP, W    -> movingUp    = false;
                case DOWN, S  -> movingDown  = false;
                case LEFT, A  -> movingLeft  = false;
                case RIGHT, D -> movingRight = false;
            }
        });
    }
    private void toggleMap() {
        boolean showing = mapPane.isVisible();

        // flip the map overlay
        mapPane.setVisible(!showing);
        mapPane.setManaged(!showing);

        if (!showing) {
            // --- map just turned ON ---
            // pause movement
            timer.stop();
            movingUp = movingDown = movingLeft = movingRight = false;

            // draw the map
            mapController.showMap(dungeonMap, currentRoom.getIndex());
            mapPane.toFront();
        } else {
            // --- map just turned OFF ---
            mapPane.toBack();

            // resume movement
            lastTime = 0;    // reset so we don't get a big jump
            timer.start();
        }
    }
    private void moveWithCollision(double dx, double dy) {
        double newX = character.getLayoutX() + dx;
        character.setLayoutX(newX);
        if (collides()) character.setLayoutX(character.getLayoutX() - dx);

        double newY = character.getLayoutY() + dy;
        character.setLayoutY(newY);
        if (collides()) character.setLayoutY(character.getLayoutY() - dy);
    }

    private void handleInteraction() {
        System.out.println("[DEBUG] Interaction key pressed.");
        MapObject obj = findNearbyObject();
        if (obj != null) {
            System.out.println("[DEBUG] Interacting with: " + obj.getClass().getSimpleName());
            obj.onInteract(this);
        } else {
            System.out.println("[DEBUG] No interactable object nearby.");
        }
    }

    private MapObject findNearbyObject() {
        // character center
        Bounds cb = character.getBoundsInParent();
        double cx = cb.getMinX() + cb.getWidth() / 2;
        double cy = cb.getMinY() + cb.getHeight() / 2;

        MapObject nearest = null;
        double bestNormDistSq = TILE_RADIUS * TILE_RADIUS;

        for (Node node : tileLayer.getChildren()) {
            if (!(node instanceof ImageView) || !(node.getUserData() instanceof MapObject))
                continue;

            Bounds ob = node.getBoundsInParent();
            double ox = ob.getMinX() + ob.getWidth() / 2;
            double oy = ob.getMinY() + ob.getHeight() / 2;

            double dx = ox - cx, dy = oy - cy;
            // normalize by tile size
            double nx = dx / cellW;
            double ny = dy / cellH;
            double normDistSq = nx * nx + ny * ny;

            if (normDistSq <= bestNormDistSq) {
                MapObject obj = (MapObject) node.getUserData();
                if (!obj.hasInteracted()) {
                    bestNormDistSq = normDistSq;
                    nearest = obj;
                }
            }
        }
        return nearest;
    }

    private void logNearbyInteractable() {
        MapObject obj = findNearbyObject();
        if (obj != null) {
            //System.out.println("[DEBUG] Nearby: " + obj.getClass().getSimpleName());
        }
    }

    private boolean collides() {
        for (Node t : tileLayer.getChildren()) {
            if (character.getBoundsInParent().intersects(t.getBoundsInParent())) {
                return true;
            }
        }
        return false;
    }

    public void pickupItem(Item item) {
        System.out.println("Picked up: " + item.getType());
        removeTileAt(item);
    }

    public void removeTileAt(MapObject obj) {
        for (int r = 0; r < layout.size(); r++) {
            for (int c = 0; c < layout.get(r).size(); c++) {
                if (layout.get(r).get(c) == obj) {
                    layout.get(r).set(c, null);
                    double x = c * cellW;
                    double y = r * cellH;
                    tileLayer.getChildren().removeIf(n -> n.getLayoutX() == x &&
                            n.getLayoutY() == y);
                    return;
                }
            }
        }
    }

    public void changeRoom(Door usedDoor) {
        String dir = usedDoor.getDirection();
        int idx = currentRoom.getIndex();
        int size = dungeonMap.getGridSize();
        System.out.println("→ Travelling " + dir + " from room idx=" + idx);

        int newIdx = switch (dir) {
            case "U" -> idx >= size ? idx - size : -1;
            case "D" -> idx < size * (size - 1) ? idx + size : -1;
            case "L" -> idx % size != 0 ? idx - 1 : -1;
            case "R" -> idx % size != size - 1 ? idx + 1 : -1;
            default -> -1;
        };
        System.out.println("   computed newIdx=" + newIdx);
        if (newIdx < 0) return;

        timer.stop();
        movingUp = movingDown = movingLeft = movingRight = false;
        Room next = dungeonMap.getRooms()[newIdx];
        System.out.println("   exists? " + next.exists());
        if (!next.exists()) return;

        // render the new room
        setRoom(next);

        // find the door in the new room that has the opposite direction
        String opposite = switch (dir) {
            case "U" -> "D";
            case "D" -> "U";
            case "L" -> "R";
            case "R" -> "L";
            default -> null;
        };

        Door entry = null;
        int entryRow = -1, entryCol = -1;
        for (int r = 0; r < next.getLayout().size(); r++) {
            for (int c = 0; c < next.getLayout().get(r).size(); c++) {
                MapObject mo = next.getLayout().get(r).get(c);
                if (mo instanceof Door d && opposite.equals(d.getDirection())) {
                    entry = d;
                    entryRow = r;
                    entryCol = c;
                    System.out.println("   found entry door at tile (" + r + "," + c + ")");
                    break;
                }
            }
            if (entry != null) break;
        }
        if (entry == null) {
            System.err.println("   ! no matching opposite door");
            return;
        }

        // compute character size and gap
        double charW = character.getBoundsInLocal().getWidth();
        double charH = character.getBoundsInLocal().getHeight();
        double gap = 2;

        // now choose spawnX/spawnY so we're just outside the door tile
        double spawnX = 0, spawnY = 0;
        switch (opposite) {
            case "U" -> {
                // door at top row (row=0), spawn just below
                spawnX = entryCol * cellW + (cellW - charW) * 0.5;
                spawnY = entryRow * cellH + cellH + gap;
            }
            case "D" -> {
                // door at bottom row, spawn just above
                spawnX = entryCol * cellW + (cellW - charW) * 0.5;
                spawnY = entryRow * cellH - charH - gap;
            }
            case "L" -> {
                // door at leftmost col, spawn just to the right
                spawnX = entryCol * cellW + cellW + gap;
                spawnY = entryRow * cellH + (cellH - charH) * 0.5;
            }
            case "R" -> {
                // door at rightmost col, spawn just to the left
                spawnX = entryCol * cellW - charW - gap;
                spawnY = entryRow * cellH + (cellH - charH) * 0.5;
            }
        }

        System.out.println(String.format("   placing character at px(%.1f,%.1f)", spawnX, spawnY));
        character.setLayoutX(spawnX);
        character.setLayoutY(spawnY);
        lastTime = 0;     // so first tick doesn't carry an old timestamp
        timer.start();
    }
}
