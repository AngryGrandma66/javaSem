package com.game.javasem.model.mapObjects;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.game.javasem.controllers.RoomController;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY, // look for the type‐id in the JSON property
        property = "category"               // <-- this must match your JSON
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Door.class,     name = "door"),
        @JsonSubTypes.Type(value = Obstacle.class, name = "obstacle"),
        @JsonSubTypes.Type(value = Chest.class,    name = "chest"),
        @JsonSubTypes.Type(value = Enemy.class,    name = "enemy"),
        @JsonSubTypes.Type(value = Item.class,     name = "item")
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