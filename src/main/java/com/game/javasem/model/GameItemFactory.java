package com.game.javasem.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.image.Image;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class GameItemFactory {
    private static final String ITEMS_JSON = "/com/game/javasem/data/items.json";
    private static final String IMAGE_BASE  = "/com/game/javasem/images/";

    /**
     * @return a map from the JSON key (e.g. "excalibur") to a fully constructed GameItem
     */
    public static Map<String, GameItem> loadAll() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // 1) Load the items.json
        String itemsJsonPath = "/com/game/javasem/data/items.json";
        InputStream jsonIn =
                GameItemFactory.class.getResourceAsStream(itemsJsonPath);
        if (jsonIn == null) {
            throw new IOException("Could not load resource: " + itemsJsonPath);
        }

        // 2) Parse it
        TypeReference<Map<String, Map<String,Object>>> typeRef =
                new TypeReference<>() {};
        Map<String, Map<String,Object>> defs = mapper.readValue(jsonIn, typeRef);

        Map<String, GameItem> items = new HashMap<>();
        // Make sure IMAGE_BASE *ends* with a slash
        String IMAGE_BASE = "/com/game/javasem/images/";

        for (var entry : defs.entrySet()) {
            String id  = entry.getKey();
            Map<String,Object> def = entry.getValue();

            String sprite = (String) def.get("sprite");
            String type   = (String) def.get("type");

            // 3) Load the icon with the correct path
            String imgPath = IMAGE_BASE + sprite;
            InputStream imgIn = GameItemFactory.class.getResourceAsStream(imgPath);
            if (imgIn == null) {
                throw new IOException("Could not load image resource: " + imgPath);
            }
            Image icon = new Image(imgIn);

            // 4) Dispatch by type
            switch (type.toLowerCase()) {
                case "weapon" -> {
                    //noinspection unchecked
                    var atkDefs = (List<Map<String,Object>>) def.get("attacks");
                    List<Attack> attacks = atkDefs.stream()
                            .map(m -> new Attack(
                                    (String)  m.get("name"),
                                    ((Number) m.get("damage")).intValue(),
                                    ((Number) m.get("cooldown")).doubleValue()
                            ))
                            .collect(Collectors.toList());
                    items.put(id, GameItem.createWeapon(id, icon, attacks));
                }
                case "armor" -> {
                    int hb = ((Number) def.get("healthBonus")).intValue();
                    items.put(id, GameItem.createArmor(id, icon, hb));
                }
                case "amulet" -> {
                    double hm = ((Number) def.get("healthMultiplier")).doubleValue();
                    double am = ((Number) def.get("attackMultiplier")).doubleValue();
                    items.put(id, GameItem.createAmulet(id, icon, hm, am));
                }
                case "key", "consumable" -> {
                    items.put(id, GameItem.createConsumable(id, icon));
                }
                default -> {
                    // unknown type: skip or log
                }
            }
        }

        return items;
    }
}