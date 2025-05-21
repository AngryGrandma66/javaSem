package com.game.javasem.model;

import com.game.javasem.model.map.Room;
import com.game.javasem.model.mapObjects.MapObject;

import java.util.List;
import java.util.Set;

public class RoomState {
    public int index;
    public boolean exists;
    public int gridSize;
    public boolean boss;
    public List<String> doors;
    public Set<String> layoutFlags;
    public List<List<MapObject>> layout;

    public Room toRoom() {
        Room r = new Room(this);  // uses the new Room(RoomState) ctor above
        // if you don’t want that ctor, inline the field copies here
        return r;
    }
}