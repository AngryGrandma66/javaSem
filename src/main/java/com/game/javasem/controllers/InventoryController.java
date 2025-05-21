package com.game.javasem.controllers;

import com.game.javasem.model.GameItem;
import com.game.javasem.model.Player;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import java.util.List;

public class InventoryController {
    @FXML private GridPane inventoryGrid;
    @FXML private ImageView weaponSlotIcon;
    @FXML private ImageView armorSlotIcon;
    @FXML private ImageView amuletSlotIcon;
    @FXML private Label healthLabel;
    @FXML private javafx.scene.layout.AnchorPane inventoryRoot;  // The root overlay pane

    private Player player;  // reference to the player (set from RoomController)

    // This method can be called by the main controller to inject the player data
    public void setPlayer(Player player) {
        this.player = player;
        refreshUI();
    }

    @FXML
    private void initialize() {
        // Set up click handlers for equipment slot icons to unequip items
        weaponSlotIcon.setOnMouseClicked(e -> unequipItem(GameItem.Type.WEAPON));
        armorSlotIcon.setOnMouseClicked(e -> unequipItem(GameItem.Type.ARMOR));
        amuletSlotIcon.setOnMouseClicked(e -> unequipItem(GameItem.Type.AMULET));
    }

    /** Refresh the entire inventory UI (call this when opening the overlay or after changes). */
    public void refreshUI() {
        if (player == null) return;
        // Update health label (current health and max health including equipment bonuses)
        healthLabel.setText("Health: " + player.getMaxHealth());

        // Update equipped slot icons (show equipped item icons or empty if none)
        if (player.getEquippedWeapon() != null) {
            weaponSlotIcon.setImage(player.getEquippedWeapon().getIcon());
        } else {
            weaponSlotIcon.setImage(null);  // or set a placeholder image for empty slot
        }
        if (player.getEquippedArmor() != null) {
            armorSlotIcon.setImage(player.getEquippedArmor().getIcon());
        } else {
            armorSlotIcon.setImage(null);
        }
        if (player.getEquippedAmulet() != null) {
            amuletSlotIcon.setImage(player.getEquippedAmulet().getIcon());
        } else {
            amuletSlotIcon.setImage(null);
        }

        // Populate inventory grid with backpack items
        inventoryGrid.getChildren().clear();  // remove old icons
        List<GameItem> items = player.getInventory().getItems();
        System.out.println("awdada"+items.toString());
        int columns = 4;  // for example, 4 columns in grid
        for (int i = 0; i < items.size(); i++) {
            GameItem item = items.get(i);
            ImageView itemIcon = new ImageView(item.getIcon());
            itemIcon.setFitWidth(32);
            itemIcon.setFitHeight(32);

            // Tooltip on hover to show item details
            Tooltip tooltip = new Tooltip(item.getDescription());
            Tooltip.install(itemIcon, tooltip);

            // Click handler: equip or unequip the item when clicked
            itemIcon.setOnMouseClicked((MouseEvent e) -> {
                equipOrToggleItem(item);
            });

            // Add the item icon to the grid at row/column
            int col = i % columns;
            int row = i / columns;
            inventoryGrid.add(itemIcon, col, row);
        }
    }

    /** Equip the given item if not equipped, or unequip it if it is currently equipped. */
    private void equipOrToggleItem(GameItem item) {
        if (item.getType() == GameItem.Type.WEAPON) {
            // If clicking a weapon
            if (player.getEquippedWeapon() == item) {
                // If this weapon is already equipped, unequip it
                player.unequipItem(GameItem.Type.WEAPON);
            } else {
                player.equipItem(item);  // will handle replacing any currently equipped weapon
            }
        } else if (item.getType() == GameItem.Type.ARMOR) {
            if (player.getEquippedArmor() == item) {
                player.unequipItem(GameItem.Type.ARMOR);
            } else {
                player.equipItem(item);
            }
        } else if (item.getType() == GameItem.Type.AMULET) {
            if (player.getEquippedAmulet() == item) {
                player.unequipItem(GameItem.Type.AMULET);
            } else {
                player.equipItem(item);
            }
        }
        refreshUI();  // update UI to reflect changes
    }

    /** Unequip item from a given slot (called when clicking equipped slot icons). */
    private void unequipItem(GameItem.Type slotType) {
        player.unequipItem(slotType);
        refreshUI();
    }
}