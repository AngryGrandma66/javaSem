package com.game.javasem.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.javasem.model.GameItem;
import com.game.javasem.model.GameItemFactory;
import com.game.javasem.model.Player;
import com.game.javasem.model.map.DungeonMap;
import com.game.javasem.model.map.Room;
import com.game.javasem.model.mapObjects.Chest;
import com.game.javasem.model.mapObjects.Door;
import com.game.javasem.model.mapObjects.Item;
import com.game.javasem.model.mapObjects.MapObject;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RoomController {
    @FXML
    public StackPane inventoryPane;
    @FXML
    private ImageView mapView;
    @FXML
    private ImageView character;
    @FXML
    private Pane tileLayer;
    @FXML
    private AnchorPane roomPane;
    @FXML
    private StackPane mapPane;    // collaborators—each in its own class:
    private RoomRenderer renderer;
    private MovementController movement;
    private InteractionService interaction;

    private MapController mapController;
    private InventoryController inventoryController;

    private DungeonMap dungeonMap;
    private Room currentRoom;
    private boolean firstShow = true;
    private Map<String, Map<String, Object>> obstacleDefs;
    private Map<String, Map<String, Object>> itemDefs;
    private Map<String, Map<String, Object>> enemyDefs;
    private Map<String, Map<String, Object>> doorDefs;
    private Map<String, Map<String, Object>> chestDefs;

    private double cellW, cellH;

    private Map<String, GameItem> gameItemDefs;
    private Player player;
    private boolean inventoryVisible = false;

    @FXML
    public void initialize() {
        // 1) load the map‐overview FXML and grab its controller
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/game/javasem/fxml/MapView.fxml")
            );
            Parent mapRoot = loader.load();
            mapController = loader.getController();
            mapPane.getChildren().add(mapRoot);

            FXMLLoader invLoader = new FXMLLoader(
                    getClass().getResource("/com/game/javasem/fxml/InventoryView.fxml")
            );
            Parent invRoot = invLoader.load();
            inventoryPane.getChildren().add(invRoot);
            inventoryController = invLoader.getController();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        loadDefinitions();
        // 2) initially hide the map overlay
        mapPane.setVisible(false);
        mapPane.setManaged(false);
        inventoryPane.setVisible(false);
        inventoryPane.setManaged(false);

        // 3) instantiate your four helper classes
        renderer = new RoomRenderer(tileLayer, mapView, obstacleDefs, itemDefs, enemyDefs, doorDefs,chestDefs);
        movement = new MovementController(character, tileLayer);
        interaction = new InteractionService(tileLayer);

        inventoryController.setPlayer(this.player);
// after renderer.render(room):
        interaction.updateCellSize(renderer.getCellWidth(), renderer.getCellHeight());
        cellH = interaction.getCellH();
        cellW = interaction.getCellW();

        movement.start();
    }

    private void bindKeys(Scene s) {
        s.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case I -> toggleInventory();
                case M -> toggleMap();
                case UP, W -> movement.setMovingUp(true);
                case DOWN, S -> movement.setMovingDown(true);
                case LEFT, A -> movement.setMovingLeft(true);
                case RIGHT, D -> movement.setMovingRight(true);
                case E -> interaction.handleInteraction(character, this);
            }
        });
        s.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case UP, W -> movement.setMovingUp(false);
                case DOWN, S -> movement.setMovingDown(false);
                case LEFT, A -> movement.setMovingLeft(false);
                case RIGHT, D -> movement.setMovingRight(false);
            }
        });
    }

    /**
     * Called by your App/MainMenu to hand over a new dungeon.
     */
    public void setDungeonMap(DungeonMap dungeonMap) {
        this.dungeonMap = dungeonMap;
    }

    /**
     * Hook up key handling (including M → toggle map).
     */
    public void setScene(Scene scene) {
        bindKeys(scene);

    }

    public void setPlayer(Player p) {
        this.player = p;
    }

    /**
     * Display a specific room.
     * Stops movement, renders, centers player once, then resumes.
     */
    public void showRoom(Room room) {
        this.currentRoom = room;

        // pause movement while we swap rooms
        movement.stop();

        // draw the new room
        renderer.render(room);
        interaction.updateCellSize(renderer.getCellWidth(), renderer.getCellHeight());
        cellH = interaction.getCellH();
        cellW = interaction.getCellW();

        // clear any pending input & resume
        if (firstShow) {
            movement.centerCharacter();
            firstShow = false;
        }
        movement.reset();
        movement.start();

    }

    public void changeRoom(Door usedDoor) {
        String dir = usedDoor.getDirection();
        int idx = currentRoom.getIndex();
        int sz = dungeonMap.getGridSize();
        int newIdx = switch (dir) {
            case "U" -> idx >= sz ? idx - sz : -1;
            case "D" -> idx < sz * (sz - 1) ? idx + sz : -1;
            case "L" -> idx % sz != 0 ? idx - 1 : -1;
            case "R" -> idx % sz != sz - 1 ? idx + 1 : -1;
            default -> -1;
        };
        if (newIdx < 0) return;

        Room next = dungeonMap.getRooms()[newIdx];
        if (!next.exists()) return;

        movement.stop();
        showRoom(next);

        String opposite = switch (dir) {
            case "U" -> "D";
            case "D" -> "U";
            case "L" -> "R";
            case "R" -> "L";
            default -> throw new IllegalArgumentException(dir);
        };

        // find the entry‐door via a labeled break
        Door entry = null;
        for (int r = 0; r < next.getLayout().size(); r++) {
            for (int c = 0; c < next.getLayout().get(r).size(); c++) {
                MapObject mo = next.getLayout().get(r).get(c);
                if (mo instanceof Door d && opposite.equals(d.getDirection())) {
                    entry = d;
                    break;
                }
            }
        }
        if (entry == null) {
            System.err.println("No opposite door found in " + opposite);
            movement.start();
            return;
        }

        // drop the player just outside that door
        movement.placeAtDoor(entry, cellW, cellH, 2);
        movement.start();
    }

    public void pickupItem(Item item) {
        // update your model

        GameItem gameItem = gameItemDefs.get(item.getType());
        if (gameItem != null) {
            player.getInventory().addItem(gameItem);
            System.out.println(player.getInventory().toString());
        }
        interaction.removeTileAt(currentRoom, item);
        System.out.println("Picked up " + item.getType());
    }

    public void openChest(Chest chest) {
        // require a key to open
        GameItem keyItem = gameItemDefs.get("key");
        if (keyItem == null || !player.getInventory().getItems().contains(keyItem)) {
            System.out.println("You need a key to open this chest!");
            return;
        }
        // remove one key from inventory
        player.getInventory().removeItem(keyItem);

        // lookup loot pool
        @SuppressWarnings("unchecked")
        List<String> pool = (List<String>) chestDefs.get(chest.getSprite()).get("lootPool");
        if (pool != null && !pool.isEmpty()) {
            String lootId = pool.get(new Random().nextInt(pool.size()));
            GameItem loot = gameItemDefs.get(lootId);
            if (loot != null) {
                player.getInventory().addItem(loot);
                System.out.println("You found " + loot.getName() + "!");
            }
        }
        interaction.removeTileAt(currentRoom, chest);
    }


    /**
     * Flip between the room‐view and the full‐map overlay.
     */
    private void toggleMap() {
        boolean showing = mapPane.isVisible();
        mapPane.setVisible(!showing);
        mapPane.setManaged(!showing);
        if (!showing) {
            // turning ON the map overlay
            movement.stop();
            mapController.showMap(dungeonMap, currentRoom.getIndex());
            mapPane.toFront();
        } else {
            mapPane.toBack();

            // turning OFF the map overlay
            movement.reset();
            movement.start();
        }

    }

    private void toggleInventory() {
        if (inventoryPane == null) return;
        inventoryVisible = !inventoryVisible;
        inventoryPane.setVisible(inventoryVisible);
        if (inventoryVisible) {
            // Refresh the inventory UI each time it's shown, in case of changes
            inventoryController.refreshUI();
            inventoryPane.toFront();  // bring overlay to top if using StackPane
        } else {
            inventoryPane.toBack();   // optional: send it behind when hidden
        }
    }


    private void loadDefinitions() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // load all GameItem objects
            gameItemDefs = GameItemFactory.loadAll();
        } catch (Exception e) {
            e.printStackTrace();
            gameItemDefs = Collections.emptyMap();
        }
        try (InputStream in = getClass().getResourceAsStream("/com/game/javasem/data/obstacles.json")) {
            if (in != null) obstacleDefs = mapper.readValue(in, new TypeReference<>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (InputStream in = getClass().getResourceAsStream("/com/game/javasem/data/items.json")) {
            if (in != null) itemDefs = mapper.readValue(in, new TypeReference<>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (InputStream in = getClass().getResourceAsStream("/com/game/javasem/data/enemies.json")) {
            if (in != null) enemyDefs = mapper.readValue(in, new TypeReference<>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (InputStream in = getClass().getResourceAsStream("/com/game/javasem/data/doors.json")) {
            if (in != null) doorDefs = mapper.readValue(in, new TypeReference<>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (InputStream in = getClass().getResourceAsStream("/com/game/javasem/data/chests.json")) {
            if (in != null) chestDefs = mapper.readValue(in, new TypeReference<>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}