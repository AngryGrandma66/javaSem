package com.game.javasem.model;

public class Attack {
    private String name;
    private int damage;
    private double cooldown;
    public Attack(String name, int damage, double cooldown) {
        this.name = name; this.damage = damage; this.cooldown = cooldown;
    }
    public String getName() { return name; }
    public int getDamage() { return damage; }
    public double getCooldown() { return cooldown; }
}