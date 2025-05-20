package com.game.javasem.model.map;

import com.game.javasem.model.mapObjects.MapObject;

import java.util.*;

public class Room {
    private final int index;
    private final int gridSize;
    private boolean exists;
    private boolean boss;
    private final List<String> doors = new ArrayList<>();

    // Which door directions this layout supports: "U", "D", "L", "R"
    private final Set<String> layoutFlags = new HashSet<>();
    // The actual tile blueprint for this room (parsed JSON or custom object)
    private List<List<MapObject>> layout;

    public Room(int index, int gridSize) {
        this.index = index;
        this.gridSize = gridSize;
    }

    public int getIndex() {
        return index;
    }

    public int getRow() {
        return index / gridSize;
    }

    public int getCol() {
        return index % gridSize;
    }

    public boolean exists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public boolean isBoss() {
        return boss;
    }

    public void setBoss(boolean boss) {
        this.boss = boss;
    }

    public List<String> getDoors() {
        return Collections.unmodifiableList(doors);
    }

    public Set<String> getLayoutFlags() {
        return Collections.unmodifiableSet(layoutFlags);
    }

    public void addLayoutFlag(String flag) {
        if ("UDLR".contains(flag)) layoutFlags.add(flag);
    }

    public List<List<MapObject>> getLayout() {
        return layout;
    }

    public void setLayout(List<List<MapObject>> layout) {
        this.layout = layout;
    }

    /**
     * Check if this room's layout supports all required door directions.
     */
    public boolean matchesFlags(Set<String> required) {
        return layoutFlags.containsAll(required);
    }

    void clearLayoutFlags() {
        layoutFlags.clear();
    }

    void addLayoutFlags(Collection<String> flags) {
        for (String f : flags) if ("UDLR".contains(f)) layoutFlags.add(f);
    }

    /**
     * Determine actual doors by checking adjacent rooms.
     */
    public void computeDoors(Room[] allRooms) {
        doors.clear();
        int row = getRow(), col = getCol();
        // Up
        if (row > 0 && allRooms[index - gridSize].exists()) doors.add("U");
        // Down
        if (row < gridSize - 1 && allRooms[index + gridSize].exists()) doors.add("D");
        // Left
        if (col > 0 && allRooms[index - 1].exists()) doors.add("L");
        // Right
        if (col < gridSize - 1 && allRooms[index + 1].exists()) doors.add("R");
    }
}

