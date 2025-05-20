package com.game.javasem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.game.javasem.model.Enemy;
import com.game.javasem.model.Item;
import com.game.javasem.model.MapObject;
import com.game.javasem.model.Obstacle;
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

public class RoomController {
    @FXML private ImageView mapView;
    @FXML private ImageView character;
    @FXML private Pane tileLayer;

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
    private MapObject[][] layout;

    @SuppressWarnings("unchecked")
    public void initialize(Scene scene) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // Load definitions
            InputStream obsStream = getClass().getResourceAsStream("data/obstacles.json");
            if (obsStream != null) {
                obstacleDefs = mapper.readValue(obsStream,
                        new TypeReference<Map<String, Map<String, Object>>>() {});
            }
            InputStream itemStream = getClass().getResourceAsStream("data/items.json");
            if (itemStream != null) {
                itemDefs = mapper.readValue(itemStream,
                        new TypeReference<Map<String, Map<String, Object>>>() {});
            }
            InputStream enemyStream = getClass().getResourceAsStream("data/enemies.json");
            if (enemyStream != null) {
                enemyDefs = mapper.readValue(enemyStream,
                        new TypeReference<Map<String, Map<String, Object>>>() {});
            }
            // Load layout
            InputStream layoutStream = getClass().getResourceAsStream("layouts/Room1.json");
            if (layoutStream == null) {
                System.err.println("Could not load layouts/Room1.json");
                return;
            }
            List<List<MapObject>> raw = mapper.readValue(
                    layoutStream,
                    new TypeReference<List<List<MapObject>>>() {}
            );
            rows = raw.size();
            cols = raw.get(0).size();
            layout = new MapObject[rows][cols];
            cellW = mapView.getFitWidth() / cols;
            cellH = mapView.getFitHeight() / rows;
            // set interaction padding to 20% of cell size (smaller radius)
            interactionPadX = cellW * 0.2;
            interactionPadY = cellH * 0.2;

            // Instantiate tile images
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    MapObject obj = raw.get(r).get(c);
                    layout[r][c] = obj;
                    if (obj != null) {
                        String spriteFile = getSpriteFor(obj);
                        if (spriteFile != null) {
                            ImageView iv = createTileImageView(spriteFile, c, r);
                            iv.setUserData(obj);
                            tileLayer.getChildren().add(iv);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        bind(scene);
    }

    private String getSpriteFor(MapObject obj) {
        String key;
        if (obj instanceof Obstacle && obstacleDefs != null) {
            key = obj.getSprite();
            Map<String,Object> def = obstacleDefs.get(key);
            if (def != null) return (String)def.get("sprite");
        } else if (obj instanceof Item && itemDefs != null) {
            key = ((Item) obj).getType();
            Map<String,Object> def = itemDefs.get(key);
            if (def != null) return (String)def.get("sprite");
        } else if (obj instanceof Enemy && enemyDefs != null) {
            key = ((Enemy) obj).getType();
            Map<String,Object> def = enemyDefs.get(key);
            if (def != null) return (String)def.get("sprite");
        }
        return null;
    }

    private ImageView createTileImageView(String spriteFile, int col, int row) {
        java.net.URL url = getClass().getResource("images/" + spriteFile);
        if (url == null) return new ImageView();
        ImageView iv = new ImageView(new Image(url.toExternalForm()));
        iv.setFitWidth(cellW);
        iv.setFitHeight(cellH);
        iv.setLayoutX(col * cellW);
        iv.setLayoutY(row * cellH);
        return iv;
    }

    private void bind(Scene s) {
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
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double delta = (now - lastTime) / 1e9; lastTime = now;
                double dx = (movingRight?SPEED:0) - (movingLeft?SPEED:0);
                double dy = (movingDown?SPEED:0) - (movingUp?SPEED:0);
                moveWithCollision(dx*delta, dy*delta);
                logNearbyInteractable();
            }
        };
        timer.start();
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
            System.out.println("[DEBUG] Nearby: " + obj.getClass().getSimpleName());
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
        for (int r = 0; r < layout.length; r++) {
            for (int c = 0; c < layout[r].length; c++) {
                if (layout[r][c] == obj) {
                    layout[r][c] = null;
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
