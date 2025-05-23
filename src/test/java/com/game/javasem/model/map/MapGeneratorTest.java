package com.game.javasem.model.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class MapGeneratorTest {

    @Test
    void carveRoomsCountBetweenMinAndMax() {
        int gridSize = 5, minRooms = 3, maxRooms = 7;
        MapGenerator gen = new MapGenerator(gridSize, minRooms, maxRooms);
        Room[] rooms = gen.generate();

        long existsCount = java.util.Arrays.stream(rooms)
                .filter(Room::exists)
                .count();
        assertTrue(existsCount >= minRooms && existsCount <= maxRooms,
                "Počet existujících místností musí být mezi " +
                        minRooms + " a " + maxRooms);
    }

    @Test
    void assignBossRoomExactlyOne() {
        int gridSize = 5, minRooms = 3, maxRooms = 7;
        MapGenerator gen = new MapGenerator(gridSize, minRooms, maxRooms);
        Room[] rooms = gen.generate();

        long bossCount = java.util.Arrays.stream(rooms)
                .filter(Room::isBoss)
                .count();
        assertEquals(1, bossCount, "Musí být právě jedna boss místnost");
    }
}