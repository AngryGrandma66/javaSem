package com.game.javasem.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Inventory {
    private List<GameItem> items = new ArrayList<>();
    public List<GameItem> getItems() { return items; }
    public void addItem(GameItem item) { items.add(item); }
    public void removeItem(GameItem item) { items.remove(item); }

    @Override
    public String toString() {
        if (items.isEmpty()) {
            return "Inventory: [empty]";
        }
        String joined = items.stream()
                .map(GameItem::getName)
                .collect(Collectors.joining(", "));
        return "Inventory: [" + joined + "]";
    }
}