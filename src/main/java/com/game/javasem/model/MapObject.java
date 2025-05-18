package com.game.javasem.model;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "category")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Obstacle.class, name = "obstacle"),
        @JsonSubTypes.Type(value = Item.class,     name = "item"),
        @JsonSubTypes.Type(value = Enemy.class,    name = "enemy")
})
public abstract class MapObject {
    public abstract String getSprite();

    public abstract String getType() ;

}