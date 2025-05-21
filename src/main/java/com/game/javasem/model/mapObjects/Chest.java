package com.game.javasem.model.mapObjects;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.game.javasem.controllers.RoomController;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Chest extends MapObject {
    private String sprite;
    @JsonProperty("lootPool")
    private List<String> lootPool;
    private int row, col;

    @Override
    public String getSprite() {
        return sprite;
    }
    @Override
    public String getType() {
        return "chest";
    }

    public List<String> getLootPool() {
        return lootPool;
    }


    @Override
    public void onInteract(RoomController controller) {
        controller.openChest(this);
    }
}