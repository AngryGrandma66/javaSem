package com.game.javasem.model.mapObjects;

import com.game.javasem.controllers.RoomController;

public class Item extends MapObject {
    private String type;
    private String sprite;
    private boolean equippable;

    public String getType() {
        return type;
    }

    @Override
    public void onInteract(RoomController controller) {
        controller.pickupItem(this);
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

    public boolean isEquippable() {
        return equippable;
    }

    public void setEquippable(boolean equippable) {
        this.equippable = equippable;
    }
}