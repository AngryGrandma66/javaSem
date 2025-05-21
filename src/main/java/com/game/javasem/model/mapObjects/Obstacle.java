package com.game.javasem.model.mapObjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.game.javasem.controllers.RoomController;
@JsonIgnoreProperties(ignoreUnknown = true)
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