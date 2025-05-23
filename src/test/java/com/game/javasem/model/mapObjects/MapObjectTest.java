package com.game.javasem.model.mapObjects;

import com.game.javasem.controllers.RoomController;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MapObjectTest {
    @Test
    void chestOnInteract_shouldCallOpenChest() {
        // připravíme mock controller
        RoomController rc = mock(RoomController.class);

        // vytvoříme chest (lootPool tu neřešíme, stačí konstruktor Jacksonu)
        Chest chest = new Chest();
        // zavoláme onInteract
        chest.onInteract(rc);

        // ověříme, že se volá openChest(this)
        verify(rc, times(1)).openChest(chest);
    }

    @Test
    void doorOnInteract_shouldCallChangeRoom() {
        RoomController rc = mock(RoomController.class);

        Door door = new Door();
        // onInteract by měl zavolat changeRoom(sám sebe)
        door.onInteract(rc);

        verify(rc, times(1)).changeRoom(door);
    }
}