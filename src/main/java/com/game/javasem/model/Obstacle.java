package com.game.javasem.model;

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

    public void setSprite(String sprite) {
        this.sprite = sprite;
    }
}