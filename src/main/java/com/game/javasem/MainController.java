package com.game.javasem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.game.javasem.model.Enemy;
import com.game.javasem.model.Item;
import com.game.javasem.model.MapObject;
import com.game.javasem.model.Obstacle;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainController {
    @FXML
    private ImageView mapView;
    @FXML
    private ImageView character;
    @FXML
    private Pane tileLayer;

    private double SPEED = 200;
    private boolean movingUp, movingDown, movingLeft, movingRight;
    private AnimationTimer timer;
    private long lastTime;

    private Map<String, Map<String, Object>> obstacleDefs;
    private Map<String, Map<String, Object>> itemDefs;
    private Map<String, Map<String, Object>> enemyDefs;
    private MapObject[][] layout;

    @SuppressWarnings("unchecked")
    public void initialize(Scene scene) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Load definitions from JSON files
            InputStream obsStream = getClass().getResourceAsStream("data/obstacles.json");
            if (obsStream == null) {
                System.err.println("Could not load obstacles.json (null stream)");
            } else {
                obstacleDefs = mapper.readValue(obsStream,
                        new TypeReference<Map<String, Map<String, Object>>>() {
                        });
            }

            InputStream itemStream = getClass().getResourceAsStream("data/items.json");
            if (itemStream == null) {
                System.err.println("Could not load items.json (null stream)");
            } else {
                itemDefs = mapper.readValue(itemStream,
                        new TypeReference<Map<String, Map<String, Object>>>() {
                        });
            }

            InputStream enemyStream = getClass().getResourceAsStream("data/enemies.json");
            if (enemyStream == null) {
                System.err.println("Could not load enemies.json (null stream)");
            } else {
                enemyDefs = mapper.readValue(enemyStream,
                        new TypeReference<Map<String, Map<String, Object>>>() {
                        });
            }

            // Load layout of MapObjects
            InputStream layoutStream = getClass().getResourceAsStream("layouts/Room1.json");
            if (layoutStream == null) {
                System.err.println("Could not load room_layout.json (null stream)");
                return;
            }
            List<List<MapObject>> raw = mapper.readValue(
                    layoutStream,
                    new TypeReference<>() {
                    }
            );

            int rows = raw.size();
            int cols = raw.get(0).size();
            layout = new MapObject[rows][cols];
            double cellW = mapView.getFitWidth() / cols;
            double cellH = mapView.getFitHeight() / rows;

            // Instantiate tiles based on MapObjects
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    MapObject obj = raw.get(r).get(c);
                    layout[r][c] = obj;
                    if (obj != null) {
                        // Get sprite filename via definition maps
                        String spriteFile = null;
                        if (obj instanceof Obstacle) {
                            // Obstacle: cell JSON provided the sprite key
                            String key = obj.getSprite();
                            Map<String, Object> def = obstacleDefs.get(key);
                            if (def != null) {
                                spriteFile = (String) def.get("sprite");
                            }
                        } else if (obj instanceof Item) {
                            // Item: cell JSON provided the type key
                            String key = ((Item) obj).getType();
                            Map<String, Object> def = itemDefs.get(key);
                            if (def != null) {
                                spriteFile = (String) def.get("sprite");
                            }
                        } else if (obj instanceof Enemy) {
                            // Enemy: cell JSON provided the type key
                            String key = ((Enemy) obj).getType();
                            Map<String, Object> def = enemyDefs.get(key);
                            if (def != null) {
                                spriteFile = (String) def.get("sprite");
                            }
                        }
                        if (spriteFile != null) {
                            // Resolve image resource URL
                            java.net.URL imageUrl = getClass().getResource("images/" + spriteFile);
                            if (imageUrl == null) {
                                System.err.println("Image resource not found: /images/" + spriteFile);
                            } else {
                                Image img = new Image(imageUrl.toExternalForm());
                                ImageView iv = new ImageView(img);
                                iv.setFitWidth(cellW);
                                iv.setFitHeight(cellH);
                                iv.setLayoutX(c * cellW);
                                iv.setLayoutY(r * cellH);
                                tileLayer.getChildren().add(iv);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        bind(scene);
    }

    private void bind(Scene s) {
        s.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case UP, W -> movingUp = true;
                case DOWN, S -> movingDown = true;
                case LEFT, A -> movingLeft = true;
                case RIGHT, D -> movingRight = true;
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

    private boolean collides() {
        for (Node t : tileLayer.getChildren()) {
            if (character.getBoundsInParent().intersects(t.getBoundsInParent())) {
                return true;
            }
        }
        return false;
    }
}