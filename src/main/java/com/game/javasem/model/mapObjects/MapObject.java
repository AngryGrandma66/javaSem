package com.game.javasem.model.mapObjects;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.game.javasem.controllers.RoomController;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "category")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Obstacle.class, name = "obstacle"),
        @JsonSubTypes.Type(value = Item.class, name = "item"),
        @JsonSubTypes.Type(value = Enemy.class, name = "enemy"),
        @JsonSubTypes.Type(value = Door.class, name = "door"),
        @JsonSubTypes.Type(value = Chest.class, name = "chest")

})
public abstract class MapObject implements Cloneable {
    @Override
    public MapObject clone() {
        try {
            // super.clone() does a field‐by‐field copy
            return (MapObject) super.clone();
        } catch (CloneNotSupportedException e) {
            // impossible, since we implement Cloneable
            throw new AssertionError(e);
        }
    }

    public abstract String getSprite();

    public abstract String getType();

    public abstract void onInteract(RoomController controller);


}