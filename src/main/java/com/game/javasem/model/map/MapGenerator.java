package com.game.javasem.model.map;


import java.util.*;

/**
 * Translates the Unity MapGenerator algorithm into Java.
 * Generates a floor plan on a 10×10 grid by expanding from a start cell.
 * Ensures total rooms between minRooms and maxRooms, marks one as the boss room
 * with exactly one connected door, prints the grid, and indicates available doors.
 */
public class MapGenerator {
    private final int gridSize;
    private final Room[] rooms;
    private final int minRooms;
    private final int maxRooms;

    public MapGenerator(int gridSize, int minRooms, int maxRooms) {
        this.gridSize = gridSize;
        this.minRooms = minRooms;
        this.maxRooms = maxRooms;
        this.rooms = new Room[gridSize * gridSize];
        for (int i = 0; i < rooms.length; i++) {
            rooms[i] = new Room(i, gridSize);
        }
    }

    public Room[] generate() {
        carveRooms();
        assignBossRoom();
        assignLayouts();
        return rooms;
    }

    private void carveRooms() {
        List<Integer> frontier = new ArrayList<>();
        Random rnd = new Random();

        // 1) seed exactly in the center cell
        int startRow = gridSize/2;
        int startCol = gridSize/2;
        int startIdx = startRow * gridSize + startCol;
        rooms[startIdx].setExists(true);
        frontier.add(startIdx);

        // helper for checking bounds
        int[][] DIRS = {
                {-1,  0}, // up
                { 1,  0}, // down
                { 0, -1}, // left
                { 0,  1}  // right
        };

        // Phase 1: FORCE-grow until minRooms hit (no randomness)
        while (totalRooms() < minRooms && !frontier.isEmpty()) {
            int idx = frontier.remove(rnd.nextInt(frontier.size()));
            int r = idx / gridSize, c = idx % gridSize;

            for (var d : DIRS) {
                int nr = r + d[0], nc = c + d[1];
                if (nr < 0 || nr >= gridSize || nc < 0 || nc >= gridSize) continue;
                int nIdx = nr*gridSize + nc;
                if (!rooms[nIdx].exists()) {
                    rooms[nIdx].setExists(true);
                    frontier.add(nIdx);
                    if (totalRooms() >= minRooms) break;
                }
            }
        }

        // Phase 2: random-grow up to maxRooms
        while (totalRooms() < maxRooms && !frontier.isEmpty()) {
            int idx = frontier.remove(rnd.nextInt(frontier.size()));
            int r = idx / gridSize, c = idx % gridSize;

            for (var d : DIRS) {
                int nr = r + d[0], nc = c + d[1];
                if (nr < 0 || nr >= gridSize || nc < 0 || nc >= gridSize) continue;
                int nIdx = nr*gridSize + nc;
                if (!rooms[nIdx].exists()
                        && rnd.nextDouble() < 0.5
                        && countNeighbors(nIdx) < 2) {
                    rooms[nIdx].setExists(true);
                    frontier.add(nIdx);
                }
            }
        }
    }

    private int countNeighbors(int idx) {
        int count = 0;
        int row = idx / gridSize, col = idx % gridSize;
        if (row > 0 && rooms[idx - gridSize].exists()) count++;
        if (row < gridSize - 1 && rooms[idx + gridSize].exists()) count++;
        if (col > 0 && rooms[idx - 1].exists()) count++;
        if (col < gridSize - 1 && rooms[idx + 1].exists()) count++;
        return count;
    }

    private int totalRooms() {
        int sum = 0;
        for (Room r : rooms) if (r.exists()) sum++;
        return sum;
    }

    private void assignBossRoom() {
        List<Room> ends = new ArrayList<>();
        for (Room r : rooms) {
            if (r.exists() && countNeighbors(r.getIndex()) == 1) ends.add(r);
        }
        if (!ends.isEmpty()) {
            ends.get(new Random().nextInt(ends.size())).setBoss(true);
        }
    }

    private void assignLayouts() {
        for (Room r : rooms) {
            if (!r.exists()) continue;
            r.computeDoors(rooms);
            // collect the actual door–dirs
            Set<String> need = new HashSet<>(r.getDoors());
            boolean needBoss = false;

            if (r.isBoss()) {
                // pick exactly one of its existing doors + require B
                String dir = r.getDoors().get(new Random().nextInt(r.getDoors().size()));
                need.clear();
                need.add(dir);
                needBoss = true;
                need.add("B");
            }

            List<RoomTemplate> choices = RoomLibrary.findByFlags(need, needBoss);
            if (!choices.isEmpty()) {
                RoomTemplate tpl = choices.get(new Random().nextInt(choices.size()));
                Room copy = tpl.instantiate(r.getIndex(), gridSize);
                // copy layout + flags back into r
                r.clearLayoutFlags();
                r.addLayoutFlags(copy.getLayoutFlags());
                r.setLayout(copy.getLayout());
            }
        }
    }
}