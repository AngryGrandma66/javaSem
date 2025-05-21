package com.game.javasem.model.mapObjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.game.javasem.controllers.RoomController;
import com.game.javasem.model.Attack;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Enemy extends MapObject {
    // this field will be populated from your layout’s "type": "dragon"
    @JsonProperty("type")
    private String enemyId;

    @JsonProperty("sprite")
    private String sprite;
    @JsonProperty("health")
    private int health;
    @JsonProperty("attacks")
    private List<Attack> attacks;
    @JsonProperty("lootPool")
    private List<String> lootPool;
    @JsonProperty("boss")
    private boolean boss;
    private int currentHealth;

    // Jackson will set enemyId from the JSON "type" property
    @Override
    public String getType() {
        return enemyId;
    }
    public void setSprite(String sprite) {
        this.sprite = sprite;
    }

    public void setAttacks(List<Attack> attacks) {
        this.attacks = attacks;
    }

    public void setLootPool(List<String> lootPool) {
        this.lootPool = lootPool;
    }

    public void setBoss(boolean boss) {
        this.boss = boss;
    }

    @Override
    public String getSprite() {
        return sprite;
    }
    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }

    public int getHealth() { return health; }
    public List<Attack> getAttacks() { return attacks; }
    public List<String> getLootPool() { return lootPool; }
    public boolean isBoss() { return boss; }


    public void setHealth(int health) {
        this.health = health;
    }

    @Override
    public void onInteract(RoomController controller) {
        controller.fightEnemy(this);
    }
}
