package com.game.javasem.controllers;

import com.game.javasem.model.Attack;
import com.game.javasem.model.map.Room;
import com.game.javasem.model.mapObjects.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class RoomRenderer {
    private static final Logger log = LoggerFactory.getLogger(RoomRenderer.class);

    private final Pane tileLayer;
    private final ImageView mapView;

    private final Map<String, Map<String, Object>> obstacleDefs;
    private final Map<String, Map<String, Object>> itemDefs;
    private final Map<String, Map<String, Object>> enemyDefs;
    private final Map<String, Map<String, Object>> doorDefs;
    private final Map<String, Map<String, Object>> chestDefs;

    private double cellW, cellH;

    public RoomRenderer(Pane tileLayer,
                        ImageView mapView,
                        Map<String, Map<String, Object>> obstacleDefs,
                        Map<String, Map<String, Object>> itemDefs,
                        Map<String, Map<String, Object>> enemyDefs,
                        Map<String, Map<String, Object>> doorDefs,
                        Map<String, Map<String, Object>> chestDefs) {
        this.tileLayer    = tileLayer;
        this.mapView      = mapView;
        this.obstacleDefs = obstacleDefs;
        this.itemDefs     = itemDefs;
        this.enemyDefs    = enemyDefs;
        this.doorDefs     = doorDefs;
        this.chestDefs    = chestDefs;
        log.debug("RoomRenderer initialized");
    }

    public void render(Room room) {
        log.info("Rendering room #{} ({} x {})", room.getIndex(),
                room.getLayout().size(), room.getLayout().get(0).size());
        tileLayer.getChildren().clear();

        List<List<MapObject>> layout = room.getLayout();
        int rows = layout.size(), cols = layout.get(0).size();

        cellW = mapView.getFitWidth() / cols;
        cellH = mapView.getFitHeight() / rows;
        log.debug("Cell size set to {} x {}", cellW, cellH);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                MapObject obj = layout.get(r).get(c);
                if (obj == null) continue;

                if (obj instanceof Door door) {
                    Map<String, Object> def = doorDefs.get(door.getSprite());
                    if (def != null) {
                        door.setDirection((String) def.get("direction"));
                        door.setPosition(r, c);
                        log.trace("Placed door '{}' at {},{}", door.getSprite(), r, c);
                    } else {
                        log.warn("No door definition for '{}'", door.getSprite());
                    }
                }

                if (obj instanceof Enemy e) {
                    Map<String,Object> def = enemyDefs.get(e.getType());
                    if (def != null) {
                        e.setSprite((String)def.get("sprite"));
                        int hp = ((Number)def.get("health")).intValue();
                        e.setHealth(hp);
                        e.setCurrentHealth(hp);
                        @SuppressWarnings("unchecked")
                        List<Map<String,Object>> atks = (List<Map<String,Object>>) def.get("attacks");
                        List<Attack> atkList = atks.stream().map(m ->
                                new Attack(
                                        (String)    m.get("name"),
                                        ((Number)   m.get("damage")).intValue(),
                                        ((Number)   m.get("cooldown")).intValue()
                                )
                        ).collect(Collectors.toList());
                        e.setAttacks(atkList);
                        @SuppressWarnings("unchecked")
                        List<String> loot = (List<String>) def.get("lootPool");
                        e.setLootPool(loot);
                        e.setBoss((Boolean) def.get("boss"));
                        log.trace("Configured enemy '{}' with {} HP, {} attacks",
                                e.getType(), hp, atkList.size());
                    } else {
                        log.warn("No enemy definition for type '{}'", e.getType());
                    }
                }

                String sprite = spriteFor(obj);
                if (sprite == null) {
                    log.debug("No sprite found for object {}", obj);
                    continue;
                }

                ImageView iv = new ImageView(
                        new Image(
                                Objects.requireNonNull(
                                        getClass().getResource("/com/game/javasem/images/" + sprite),
                                        "Missing image resource: " + sprite
                                ).toExternalForm()
                        )
                );
                iv.setFitWidth(cellW);
                iv.setFitHeight(cellH);
                iv.setLayoutX(c * cellW);
                iv.setLayoutY(r * cellH);
                iv.setUserData(obj);
                tileLayer.getChildren().add(iv);
            }
        }
        log.info("Finished rendering room #{}", room.getIndex());
    }

    private String spriteFor(MapObject obj) {
        String key, sprite;
        if (obj instanceof Obstacle && obstacleDefs != null) {
            key = obj.getSprite();
            sprite = getDefSprite(obstacleDefs, key, "obstacle");
            if (sprite != null) return sprite;
        }
        if (obj instanceof Item && itemDefs != null) {
            key = obj.getType();
            sprite = getDefSprite(itemDefs, key, "item");
            if (sprite != null) return sprite;
        }
        if (obj instanceof Enemy && enemyDefs != null) {
            key = obj.getType();
            sprite = getDefSprite(enemyDefs, key, "enemy");
            if (sprite != null) return sprite;
        }
        if (obj instanceof Door && doorDefs != null) {
            key = obj.getSprite();
            sprite = getDefSprite(doorDefs, key, "door");
            if (sprite != null) return sprite;
        }
        if (obj instanceof Chest && chestDefs != null) {
            key = obj.getSprite();
            sprite = getDefSprite(chestDefs, key, "chest");
            return sprite;
        }
        return null;
    }

    private String getDefSprite(Map<String, Map<String,Object>> defs,
                                String key, String category) {
        Map<String, Object> d = defs.get(key);
        if (d == null) {
            log.warn("No {} definition for key '{}'", category, key);
            return null;
        }
        return (String) d.get("sprite");
    }

    public double getCellWidth() {
        return cellW;
    }

    public double getCellHeight() {
        return cellH;
    }
}
