package com.game.javasem.model.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DungeonMapTest {
    @Nested
    @DisplayName("Validace parametrů")
    class ParameterValidation {

        @Test
        @DisplayName("gridSize mimo rozsah [3,100] → IllegalArgumentException")
        void gridSizeOutOfBounds() {
            // menší než 3
            IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                    () -> new DungeonMap(2, 1, 1));
            assertTrue(ex1.getMessage().contains("gridSize must be between 3 and 100"));

            // větší než 100
            IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                    () -> new DungeonMap(101, 1, 1));
            assertTrue(ex2.getMessage().contains("gridSize must be between 3 and 100"));
        }

        @Test
        @DisplayName("minRooms mimo rozsah [1, gridSize*gridSize] → IllegalArgumentException")
        void minRoomsOutOfBounds() {
            int n = 5;
            int maxPossible = n * n;
            // menší než 1
            IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                    () -> new DungeonMap(n, 0, 2));
            assertTrue(ex1.getMessage().contains("minRooms must be between 1 and " + maxPossible));

            // větší než maxPossible
            IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                    () -> new DungeonMap(n, maxPossible + 1, maxPossible + 1));
            assertTrue(ex2.getMessage().contains("minRooms must be between 1 and " + maxPossible));
        }

        @Test
        @DisplayName("maxRooms mimo rozsah [minRooms, gridSize*gridSize] → IllegalArgumentException")
        void maxRoomsOutOfBounds() {
            int n = 4;
            int maxPossible = n * n;
            // menší než minRooms
            IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                    () -> new DungeonMap(n, 5, 4));
            assertTrue(ex1.getMessage().contains("maxRooms must be between minRooms (5) and " + maxPossible));

            // větší než maxPossible
            IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                    () -> new DungeonMap(n, 2, maxPossible + 1));
            assertTrue(ex2.getMessage().contains("maxRooms must be between minRooms (2) and " + maxPossible));
        }
    }

    @Nested
    @DisplayName("Generování mapy a startIndex")
    class MapGeneration {

        @Test
        @DisplayName("Správná velikost a střední startIndex pro validní parametry")
        void testValidConstructionAndProperties() throws Exception {
            int gridSize = 7;
            int minRooms = 3;
            int maxRooms = 5;

            DungeonMap dm = new DungeonMap(gridSize, minRooms, maxRooms);

            // 1) gridSize
            assertEquals(gridSize, dm.getGridSize(), "getGridSize() musí vracet zadané gridSize");

            // 2) rooms.length == gridSize*gridSize
            assertEquals(gridSize * gridSize, dm.getRooms().length,
                    "getRooms() musí vracet pole o velikosti gridSize^2");

            // 3) počet existujících místností mezi minRooms a maxRooms
            long existsCount = java.util.Arrays.stream(dm.getRooms())
                    .filter(Room::exists)
                    .count();
            assertTrue(existsCount >= minRooms && existsCount <= maxRooms,
                    "Počet existujících místností musí být v intervalu [" + minRooms + "," + maxRooms + "]");

            // 4) startIndex = střed (gridSize/2, gridSize/2)
            int expectedCenter = (gridSize / 2) * gridSize + (gridSize / 2);
            assertEquals(expectedCenter, dm.getStartIndex(),
                    "getStartIndex() musí ukazovat na centrální buňku");

            // 5) getStartRoom() odpovídá rooms[startIndex]
            assertSame(dm.getRooms()[dm.getStartIndex()], dm.getStartRoom(),
                    "getStartRoom() musí vracet instanci rooms[startIndex]");

            // 6) přesně jedna boss místnost
            long bossCount = java.util.Arrays.stream(dm.getRooms())
                    .filter(Room::isBoss)
                    .count();
            assertEquals(1, bossCount, "Musí být přesně jedna boss místnost");
        }
    }
}