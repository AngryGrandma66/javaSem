package com.game.javasem.model.mapObjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.game.javasem.controllers.RoomController;

public class Door extends MapObject {
    private String sprite;
    @JsonProperty("direction")
    private String direction;
    public String getSprite() {
        return sprite;
    }

    @Override
    public String getType() {
        return "";
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void onInteract(RoomController controller) {
        controller.changeRoom(direction);
    }

    public void setSprite(String sprite) {
        this.sprite = sprite;
    }
}