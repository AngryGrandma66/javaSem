package com.game.javasem.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.javasem.model.GameItem;
import com.game.javasem.model.GameItemFactory;
import com.game.javasem.model.Player;
import com.game.javasem.model.map.DungeonMap;
import com.game.javasem.model.map.Room;
import com.game.javasem.model.mapObjects.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private StackPane mapPane;
    private RoomRenderer renderer;
    private MovementController movement;
    private InteractionService interaction;
    private static final Logger log = LoggerFactory.getLogger(RoomController.class);

    private MapController mapController;
    private InventoryController inventoryController;

    private Scene roomScene;
    private Scene previousScene;
    private Stage primaryStage;
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

    public Room getCurrentRoom() {
        return currentRoom;
    }

    private Player player;
    private boolean inventoryVisible = false;

    @FXML
    public void initialize() {
        try {
            log.info("Initializing RoomController and loading overlays");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/game/javasem/fxml/MapView.fxml"));
            Parent mapRoot = loader.load();
            mapController = loader.getController();
            mapPane.getChildren().add(mapRoot);

            FXMLLoader invLoader = new FXMLLoader(getClass().getResource("/com/game/javasem/fxml/InventoryView.fxml"));
            Parent invRoot = invLoader.load();
            inventoryPane.getChildren().add(invRoot);
            inventoryController = invLoader.getController();
        } catch (Exception e) {
            log.error("Failed to load map/inventory overlays", e);
            throw new RuntimeException(e);
        }
        loadDefinitions();
        mapPane.setVisible(false);
        mapPane.setManaged(false);
        inventoryPane.setVisible(false);
        inventoryPane.setManaged(false);

        renderer = new RoomRenderer(tileLayer, mapView, obstacleDefs, itemDefs, enemyDefs, doorDefs, chestDefs);
        movement = new MovementController(character, tileLayer);
        interaction = new InteractionService(tileLayer);

        inventoryController.setPlayer(this.player);
        interaction.updateCellSize(renderer.getCellWidth(), renderer.getCellHeight());
        cellH = interaction.getCellH();
        cellW = interaction.getCellW();

        movement.start();
    }

    private void bindKeys(Scene s) {
        s.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE -> { log.debug("ESC pressed: showing pause menu"); showPauseMenu(); }
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

    public Player getPlayer() {
        return player;
    }

    public DungeonMap getDungeonMap() {
        return dungeonMap;
    }

    private void showPauseMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/game/javasem/fxml/PauseMenu.fxml")
            );
            Parent pauseRoot = loader.load();            // ← use Parent, not StackPane
            PauseMenuController pmc = loader.getController();
            pmc.init(this, primaryStage, roomScene);

            Scene pauseScene = new Scene(pauseRoot,
                    roomScene.getWidth(),
                    roomScene.getHeight());
            primaryStage.setScene(pauseScene);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void setDungeonMap(DungeonMap dungeonMap) {
        this.dungeonMap = dungeonMap;
        log.info("DungeonMap injected with {} rooms", dungeonMap.getRooms().length);
    }


    public void setScene(Scene scene) {
        this.roomScene = scene;
        this.primaryStage = (Stage) scene.getWindow();
        bindKeys(scene);
        log.debug("Key bindings attached to scene");

    }

    public void setPlayer(Player p) {
        this.player = p;
        log.info("Player injected: {}", p);
    }


    public void showRoom(Room room) {
        this.currentRoom = room;
        log.info("Showing room index {}", room.getIndex());
        movement.stop();

        renderer.render(room);
        interaction.updateCellSize(renderer.getCellWidth(), renderer.getCellHeight());
        cellH = interaction.getCellH();
        cellW = interaction.getCellW();

        if (firstShow) {
            movement.centerCharacter();
            firstShow = false;
            log.debug("Centered character on first show");
        }
        movement.reset();
        movement.start();

    }

    public void changeRoom(Door usedDoor) {
        log.info("Interacting with door at ({}, {}) direction {}", usedDoor.getRow(), usedDoor.getCol(), usedDoor.getDirection());
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
            movement.start();
            return;
        }

        movement.placeAtDoor(entry, cellW, cellH, 2);
        movement.start();
    }

    public void pickupItem(Item item) {
        log.info("Picked up item {}", item.getType());
        GameItem gameItem = gameItemDefs.get(item.getType());
        if (gameItem != null) {
            player.getInventory().addItem(gameItem);
        }
        interaction.removeTileAt(currentRoom, item);
    }

    public void openChest(Chest chest) {
        log.info("Attempting to open chest {}", chest.getSprite());
        GameItem keyItem = gameItemDefs.get("key");
        if (keyItem == null || !player.getInventory().getItems().contains(keyItem)) {
            log.warn("No key in inventory, cannot open chest");
            return;
        }
        player.getInventory().removeItem(keyItem);
        log.debug("Key consumed");
        @SuppressWarnings("unchecked") List<String> pool = (List<String>) chestDefs.get(chest.getSprite()).get("lootPool");
        if (pool != null && !pool.isEmpty()) {
            String lootId = pool.get(new Random().nextInt(pool.size()));
            GameItem loot = gameItemDefs.get(lootId);
            if (loot != null) {
                player.getInventory().addItem(loot);
                log.info("Chest yielded {}", lootId);
            }
        }
        interaction.removeTileAt(currentRoom, chest);
    }

    public void fightEnemy(Enemy enemy) {
        try {
            log.info("Starting battle with {} at room {}", enemy.getType(), currentRoom.getIndex());
            movement.stop();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/game/javasem/fxml/BattleView.fxml"));
            Parent battleRoot = loader.load();
            BattleController bc = loader.getController();
            primaryStage = (Stage) roomPane.getScene().getWindow();
            previousScene = roomPane.getScene();
            bc.startBattle(player, enemy, this, previousScene);
            primaryStage.setScene(new Scene(battleRoot));
            primaryStage.setTitle("Battle!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void handleBattleEnd(Enemy enemy) {
        log.info("Battle ended. Boss? {}. Awarding loot.", enemy.isBoss());
        List<String> pool = enemy.getLootPool();
        interaction.removeTileAt(currentRoom, enemy);
        movement.reset();
        movement.start();
        if (pool != null && !pool.isEmpty()) {
            String lootId = pool.get(new Random().nextInt(pool.size()));
            if (gameItemDefs != null && gameItemDefs.containsKey(lootId)) {
                GameItem loot = gameItemDefs.get(lootId);
                player.getInventory().addItem(loot);
            }
        }

        if (enemy.isBoss()) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/com/game/javasem/fxml/Victory.fxml"));
                primaryStage.setScene(new Scene(root));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (previousScene != null) {
                primaryStage.setScene(previousScene);
            }
        }
    }


    private void toggleMap() {
        boolean showing = mapPane.isVisible();
        mapPane.setVisible(!showing);
        mapPane.setManaged(!showing);
        if (!showing) {
            movement.stop();
            mapController.showMap(dungeonMap, currentRoom.getIndex());
            mapPane.toFront();
        } else {
            mapPane.toBack();

            movement.reset();
            movement.start();
        }
    }

    private void toggleInventory() {
        if (inventoryPane == null) return;
        inventoryVisible = !inventoryVisible;
        log.debug("{} inventory overlay", inventoryVisible ? "Showing" : "Hiding");
        inventoryPane.setVisible(inventoryVisible);
        if (inventoryVisible) {
            inventoryController.refreshUI();
            inventoryPane.toFront();
        } else {
            inventoryPane.toBack();
        }
    }


    private void loadDefinitions() {
        ObjectMapper mapper = new ObjectMapper();
        log.debug("Loading JSON definitions for obstacles, items, enemies, doors, chests");
        try {
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

    public void backToMainMenu() {
        log.info("Returning to Main Menu from RoomController");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/game/javasem/fxml/MainMenu.fxml"));
            Parent menuRoot = loader.load();
            Scene menuScene = new Scene(menuRoot, roomScene.getWidth(), roomScene.getHeight());
            primaryStage.setScene(menuScene);
            primaryStage.setTitle("Main Menu");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}