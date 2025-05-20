package com.game.javasem.model.mapObjects;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.game.javasem.controllers.RoomController;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "category")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Obstacle.class, name = "obstacle"),
        @JsonSubTypes.Type(value = Item.class, name = "item"),
        @JsonSubTypes.Type(value = Enemy.class, name = "enemy"),
        @JsonSubTypes.Type(value = Door.class, name = "door")
})
public abstract class MapObject {
    private boolean interacted = false;                // to prevent repeated triggers

    public abstract String getSprite();

    public abstract String getType();

    public abstract void onInteract(RoomController controller);

    public boolean hasInteracted() {
        return interacted;
    }

    public void markInteracted() {
        interacted = true;
    }
}