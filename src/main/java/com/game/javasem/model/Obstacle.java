package com.game.javasem.model;

import com.game.javasem.RoomController;

public class Obstacle extends MapObject {
    private String sprite;

    // getter used by Jackson
    public String getSprite() {
        return sprite;
    }

    @Override
    public String getType() {
        return "";
    }

    @Override
    public void onInteract(RoomController controller) {

    }

    public void setSprite(String sprite) {
        this.sprite = sprite;
    }
}