package com.game.javasem.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.game.javasem.model.map.DungeonMap;
import com.game.javasem.model.map.Room;
import com.game.javasem.model.mapObjects.*;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RoomController {
    @FXML
    private ImageView mapView;
    @FXML
    private ImageView character;
    @FXML
    private Pane tileLayer;

    private Scene scene;          // ← store the scene here
    private DungeonMap dungeonMap;
    private Room currentRoom;
    private double SPEED = 200;
    private boolean movingUp, movingDown, movingLeft, movingRight;
    private AnimationTimer timer;
    private long lastTime;

    private int rows;
    private int cols;
    private double cellW;
    private double cellH;
    private double interactionPadX;
    private double interactionPadY;

    private Map<String, Map<String, Object>> obstacleDefs;
    private Map<String, Map<String, Object>> itemDefs;
    private Map<String, Map<String, Object>> enemyDefs;
    private Map<String, Map<String, Object>> doorDefs;

    private List<List<MapObject>> layout;

    public void initialize(Scene scene) {
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
    /** called from MainMenuController */
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
        tileLayer.getChildren().clear();

        layout = currentRoom.getLayout();
        rows = layout.size();
        cols = layout.getFirst().size();
        cellW = mapView.getFitWidth() / cols;
        cellH = mapView.getFitHeight() / rows;
        interactionPadX = cellW * 0.2;
        interactionPadY = cellH * 0.2;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                MapObject obj = layout.get(r).get(c);
                if (obj == null) continue;
                if (obj instanceof Door door && doorDefs != null) {
                    Map<String,Object> def = doorDefs.get(door.getSprite());
                    if (def != null) {
                        door.setDirection((String)def.get("direction"));
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
        }
        else if (obj instanceof Door && enemyDefs != null) {
            key = obj.getSprite();
            Map<String, Object> def = doorDefs.get(key);
            if (def != null) return (String) def.get("sprite");
        }
        return null;
    }

    private void bindKeys(Scene s) {
        s.setOnKeyPressed(e -> {
            KeyCode code = e.getCode();
            switch (code) {
                case UP, W -> movingUp = true;
                case DOWN, S -> movingDown = true;
                case LEFT, A -> movingLeft = true;
                case RIGHT, D -> movingRight = true;
                case E -> handleInteraction();
            }
        });
        s.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case UP, W -> movingUp = false;
                case DOWN, S -> movingDown = false;
                case LEFT, A -> movingLeft = false;
                case RIGHT, D -> movingRight = false;
            }
        });
    }

    private void moveWithCollision(double dx, double dy) {
        character.setX(character.getX() + dx);
        if (collides()) character.setX(character.getX() - dx);
        character.setY(character.getY() + dy);
        if (collides()) character.setY(character.getY() - dy);
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
        Bounds cb = character.getBoundsInParent();
        Bounds expanded = new BoundingBox(
                cb.getMinX() - interactionPadX,
                cb.getMinY() - interactionPadY,
                cb.getWidth() + interactionPadX * 2,
                cb.getHeight() + interactionPadY * 2
        );
        for (Node node : tileLayer.getChildren()) {
            if (node instanceof ImageView && node.getUserData() instanceof MapObject) {
                if (node.getBoundsInParent().intersects(expanded)) {
                    MapObject obj = (MapObject) node.getUserData();
                    if (!obj.hasInteracted()) return obj;
                }
            }
        }
        return null;
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
                    layout.get(r).set(c,null);
                    double x = c * cellW;
                    double y = r * cellH;
                    tileLayer.getChildren().removeIf(n -> n.getLayoutX() == x &&
                            n.getLayoutY() == y);
                    return;
                }
            }
        }
    }
    public void changeRoom(String dir) {
        int idx = currentRoom.getIndex();
        int size = dungeonMap.getGridSize();
        int row = idx / size, col = idx % size;
        int newIdx = switch(dir) {
            case "U" -> (row > 0)           ? idx - size : -1;
            case "D" -> (row < size - 1)    ? idx + size : -1;
            case "L" -> (col > 0)           ? idx - 1    : -1;
            case "R" -> (col < size - 1)    ? idx + 1    : -1;
            default  -> -1;
        };
        if (newIdx >= 0) {
            Room next = dungeonMap.getRooms()[newIdx];
            if (next.exists()) {
                setRoom(next);
            }
        }
    }
}
