package com.game.javasem.model.map;


import static com.game.javasem.model.map.RoomLibrary.loadTemplates;
import static com.game.javasem.model.map.RoomTemplateLoader.loadAll;

public class DungeonMap {
    private final Room[] rooms;
    private final int startIndex;
    private final int gridSize;

    /**
     * Constructs a new DungeonMap of the given size, runs the generator,
     * and records the central starting index.
     * @param gridSize number of rows/columns in the square map grid
     */
    public DungeonMap(int gridSize, int minRooms, int maxRooms) throws Exception {
        // Validate parameters
        if (gridSize < 3 || gridSize > 100) {
            throw new IllegalArgumentException("gridSize must be between 3 and 100, but was " + gridSize);
        }
        int maxPossible = gridSize * gridSize;
        if (minRooms < 1 || minRooms > maxPossible) {
            throw new IllegalArgumentException("minRooms must be between 1 and " + maxPossible + ", but was " + minRooms);
        }
        if (maxRooms < minRooms || maxRooms > maxPossible) {
            throw new IllegalArgumentException("maxRooms must be between minRooms (" + minRooms + ") and " + maxPossible + ", but was " + maxRooms);
        }

        this.gridSize = gridSize;
        loadTemplates(loadAll());
        MapGenerator generator = new MapGenerator(gridSize, minRooms, maxRooms);
        this.rooms = generator.generate();
        // starting room is placed at the true center of the grid
        int startRow = gridSize / 2;
        int startCol = gridSize / 2;
        this.startIndex = startRow * gridSize + startCol;
    }

    public DungeonMap(int gridSize, Room[] rooms,int startIndex) {
        this.gridSize = gridSize;
        this.rooms    = rooms;
        this.startIndex =startIndex ;
    }

    /** @return the flat array of all cells/rooms */
    public Room[] getRooms() {
        return rooms;
    }

    /** @return the array index corresponding to the starting room */
    public int getStartIndex() {
        return startIndex;
    }

    /** @return the Room object at the starting index */
    public Room getStartRoom() {
        return rooms[startIndex];
    }

    /** @return the number of rows/columns in this map */
    public int getGridSize() {
        return gridSize;
    }
}
