package com.game.javasem.model.map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.game.javasem.model.mapObjects.MapObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)   // in case you add more props later
public class RoomTemplate {
    private String id;

    @JsonProperty("flags")
    private List<String> flags;                   // e.g. ["U","D","L","R","B"]
    private List<List<MapObject>> layout;         // your tile‐map

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getFlags() {
        return flags;
    }

    public void setFlags(List<String> flags) {
        this.flags = flags;
    }

    public List<List<MapObject>> getLayout() {
        return layout;
    }

    public void setLayout(List<List<MapObject>> layout) {
        this.layout = layout;
    }

    /**
     * Only the UDLR flags, not the B flag.
     */
    public Set<String> getDirectionFlags() {
        Set<String> dirs = new HashSet<>();
        for (String f : flags) if ("UDLR".contains(f)) dirs.add(f);
        return dirs;
    }

    public boolean allowsBoss() {
        return flags.contains("B");
    }

    /**
     * Create a new Room (same index/gridSize), copy flags+layout.
     */
    public Room instantiate(int index, int gridSize) {
        Room r = new Room(index, gridSize);
        r.clearLayoutFlags();
        r.addLayoutFlags(getDirectionFlags());
        r.setLayout(deepCopy(layout));
        return r;
    }

    private static List<List<MapObject>> deepCopy(List<List<MapObject>> original) {
        List<List<MapObject>> copy = new ArrayList<>(original.size());
        for (List<MapObject> row : original) {
            List<MapObject> rowCopy = new ArrayList<>(row.size());
            for (MapObject obj : row) {
                if (obj == null) {
                    rowCopy.add(null);
                } else {
                    rowCopy.add(obj.clone());
                }
            }
            copy.add(rowCopy);
        }
        return copy;
    }

    @Override
    public String toString() {
        return "RoomTemplate{" +
                "id='" + id + '\'' +
                ", flags=" + flags +
                '}';
    }


}
