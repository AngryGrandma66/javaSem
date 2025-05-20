package com.game.javasem.model.mapObjects;

import com.game.javasem.controllers.RoomController;

public class Enemy extends MapObject {
    private String type;
    private String sprite;
    private boolean fly;

    public String getType() {
        return type;
    }

    @Override
    public void onInteract(RoomController controller) {

    }

    public void setType(String type) {
        this.type = type;
    }
    public String getSprite() {
        return sprite;
    }
    public void setSprite(String sprite) {
        this.sprite = sprite;
    }
    public boolean isFly() {
        return fly;
    }
    public void setFly(boolean fly) {
        this.fly = fly;
    }
}
