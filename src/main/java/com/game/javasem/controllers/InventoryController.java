package com.game.javasem.controllers;

import com.game.javasem.model.GameItem;
import com.game.javasem.model.Player;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class InventoryController {
    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);

    @FXML private GridPane inventoryGrid;
    @FXML private ImageView weaponSlotIcon;
    @FXML private ImageView armorSlotIcon;
    @FXML private ImageView amuletSlotIcon;
    @FXML private Label healthLabel;
    @FXML private AnchorPane inventoryRoot;

    private Player player;

    public void setPlayer(Player player) {
        this.player = player;
        log.debug("Player set in InventoryController: {}", player);
        refreshUI();
    }

    @FXML
    private void initialize() {
        weaponSlotIcon.setOnMouseClicked(e -> unequipItem(GameItem.Type.WEAPON));
        armorSlotIcon.setOnMouseClicked(e -> unequipItem(GameItem.Type.ARMOR));
        amuletSlotIcon.setOnMouseClicked(e -> unequipItem(GameItem.Type.AMULET));
        log.debug("InventoryController initialized");
    }

    public void refreshUI() {
        if (player == null) {
            log.warn("refreshUI called with null player");
            return;
        }
        log.info("Refreshing inventory UI for player {}", player);
        healthLabel.setText("Health: " + player.getMaxHealth());

        // Equipment slots
        if (player.getEquippedWeapon() != null) {
            weaponSlotIcon.setImage(player.getEquippedWeapon().getIcon());
            log.debug("Weapon slot icon set to {}", player.getEquippedWeapon().getName());
        } else {
            weaponSlotIcon.setImage(null);
            log.debug("Weapon slot cleared");
        }
        if (player.getEquippedArmor() != null) {
            armorSlotIcon.setImage(player.getEquippedArmor().getIcon());
            log.debug("Armor slot icon set to {}", player.getEquippedArmor().getName());
        } else {
            armorSlotIcon.setImage(null);
            log.debug("Armor slot cleared");
        }
        if (player.getEquippedAmulet() != null) {
            amuletSlotIcon.setImage(player.getEquippedAmulet().getIcon());
            log.debug("Amulet slot icon set to {}", player.getEquippedAmulet().getName());
        } else {
            amuletSlotIcon.setImage(null);
            log.debug("Amulet slot cleared");
        }

        // Inventory grid
        inventoryGrid.getChildren().clear();
        List<GameItem> items = player.getInventory().getItems();
        log.debug("Populating inventory grid with {} items", items.size());

        int columns = 4;
        for (int i = 0; i < items.size(); i++) {
            GameItem item = items.get(i);
            ImageView itemIcon = new ImageView(item.getIcon());
            itemIcon.setFitWidth(32);
            itemIcon.setFitHeight(32);

            Tooltip tooltip = new Tooltip(item.getDescription());
            Tooltip.install(itemIcon, tooltip);

            itemIcon.setOnMouseClicked((MouseEvent e) -> {
                log.info("Clicked on inventory item {}", item.getName());
                equipOrToggleItem(item);
            });

            int col = i % columns;
            int row = i / columns;
            inventoryGrid.add(itemIcon, col, row);
        }
    }

    private void equipOrToggleItem(GameItem item) {
        log.info("equipOrToggleItem: {}", item.getName());
        switch (item.getType()) {
            case WEAPON -> {
                if (player.getEquippedWeapon() == item) {
                    log.debug("Unequipping weapon {}", item.getName());
                    player.unequipItem(GameItem.Type.WEAPON);
                } else {
                    log.debug("Equipping weapon {}", item.getName());
                    player.equipItem(item);
                }
            }
            case ARMOR -> {
                if (player.getEquippedArmor() == item) {
                    log.debug("Unequipping armor {}", item.getName());
                    player.unequipItem(GameItem.Type.ARMOR);
                } else {
                    log.debug("Equipping armor {}", item.getName());
                    player.equipItem(item);
                }
            }
            case AMULET -> {
                if (player.getEquippedAmulet() == item) {
                    log.debug("Unequipping amulet {}", item.getName());
                    player.unequipItem(GameItem.Type.AMULET);
                } else {
                    log.debug("Equipping amulet {}", item.getName());
                    player.equipItem(item);
                }
            }
            default -> log.warn("Clicked item of unsupported type: {}", item.getType());
        }
        refreshUI();
    }

    private void unequipItem(GameItem.Type slotType) {
        log.info("unequipItem slot {}", slotType);
        player.unequipItem(slotType);
        refreshUI();
    }
}
