package com.game.javasem.model;

public class Enemy extends MapObject {
    private String type;
    private String sprite;
    private boolean fly;

    public String getType() {
        return type;
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
