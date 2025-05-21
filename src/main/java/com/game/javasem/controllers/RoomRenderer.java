package com.game.javasem.controllers;

import com.game.javasem.model.map.Room;
import com.game.javasem.model.mapObjects.Door;
import com.game.javasem.model.mapObjects.MapObject;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RoomRenderer {
    private final Pane tileLayer;
    private final ImageView mapView;

    // all your JSON‐loaded lookup tables:
    private final Map<String, Map<String, Object>> obstacleDefs;
    private final Map<String, Map<String, Object>> itemDefs;
    private final Map<String, Map<String, Object>> enemyDefs;
    private final Map<String, Map<String, Object>> doorDefs;

    // most recent cell size, for anyone else who needs it:
    private double cellW, cellH;

    public RoomRenderer(Pane tileLayer,
                        ImageView mapView,
                        Map<String, Map<String, Object>> obstacleDefs,
                        Map<String, Map<String, Object>> itemDefs,
                        Map<String, Map<String, Object>> enemyDefs,
                        Map<String, Map<String, Object>> doorDefs) {
        this.tileLayer     = tileLayer;
        this.mapView       = mapView;
        this.obstacleDefs  = obstacleDefs;
        this.itemDefs      = itemDefs;
        this.enemyDefs     = enemyDefs;
        this.doorDefs      = doorDefs;
    }

    /** Draws the given Room onto the tileLayer, replacing whatever was there. */
    public void render(Room room) {
        tileLayer.getChildren().clear();

        List<List<MapObject>> layout = room.getLayout();
        int rows = layout.size(), cols = layout.get(0).size();

        cellW = mapView.getFitWidth()  / cols;
        cellH = mapView.getFitHeight() / rows;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                MapObject obj = layout.get(r).get(c);
                if (obj == null) continue;

                // if it’s a Door, pull direction from doorDefs
                if (obj instanceof Door door) {
                    Map<String,Object> def = doorDefs.get(door.getSprite());
                    if (def != null) {
                        door.setDirection((String)def.get("direction"));
                         door.setPosition(r,c);
                    }
                }

                String sprite = spriteFor(obj);
                if (sprite == null) continue;

                ImageView iv = new ImageView(
                        new Image(
                                Objects.requireNonNull(
                                        getClass().getResource("/com/game/javasem/images/" + sprite)
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
    }

    private String spriteFor(MapObject obj) {
        String key;
        if (obj instanceof com.game.javasem.model.mapObjects.Obstacle && obstacleDefs!=null) {
            key = obj.getSprite();
            var d = obstacleDefs.get(key);
            if (d!=null) return (String)d.get("sprite");
        }
        if (obj instanceof com.game.javasem.model.mapObjects.Item && itemDefs!=null) {
            key = obj.getType();
            var d = itemDefs.get(key);
            if (d!=null) return (String)d.get("sprite");
        }
        if (obj instanceof com.game.javasem.model.mapObjects.Enemy && enemyDefs!=null) {
            key = obj.getType();
            var d = enemyDefs.get(key);
            if (d!=null) return (String)d.get("sprite");
        }
        if (obj instanceof Door && doorDefs!=null) {
            key = obj.getSprite();
            var d = doorDefs.get(key);
            if (d!=null) return (String)d.get("sprite");
        }
        return null;
    }

    /** Expose to others who need to know tile size. */
    public double getCellWidth()  { return cellW; }
    public double getCellHeight() { return cellH; }
}
