package com.game.javasem.model;

public class Attack {
    private String name;
    private int damage;
    private int cooldown;

    public Attack() {
    }

    public Attack(String name, int damage, int cooldown) {
        this.name = name;
        this.damage = damage;
        this.cooldown = cooldown;
    }

    public String getName() {
        return name;
    }

    public int getDamage() {
        return damage;
    }

    public int getCooldown() {
        return cooldown;
    }

    @Override
    public String toString() {
        return name + "(" + damage + " dmg, " + cooldown + " turns)";
    }
}