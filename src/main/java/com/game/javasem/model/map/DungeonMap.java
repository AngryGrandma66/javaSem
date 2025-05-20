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
        this.gridSize = gridSize;
        loadTemplates(loadAll());
        MapGenerator generator = new MapGenerator(gridSize, minRooms, maxRooms);
        this.rooms = generator.generate();
        // starting room is placed at the true center of the grid
        int startRow = gridSize / 2;
        int startCol = gridSize / 2;
        this.startIndex = startRow * gridSize + startCol;
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
