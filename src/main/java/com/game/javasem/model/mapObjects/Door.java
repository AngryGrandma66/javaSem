package com.game.javasem.model.mapObjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.game.javasem.controllers.RoomController;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Door extends MapObject {
    private String sprite;
    @JsonProperty("direction")
    private String direction;


    private int row, col;

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setPosition(int r, int c) {
        this.row = r;
        this.col = c;
    }

    @Override
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

    public void setDirection(String d) {
        this.direction = d;
    }

    @Override
    public void onInteract(RoomController controller) {
        controller.changeRoom(this);
    }
}