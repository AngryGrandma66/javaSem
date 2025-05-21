package com.game.javasem.controllers;

import com.game.javasem.model.Attack;
import com.game.javasem.model.Player;
import com.game.javasem.model.mapObjects.Enemy;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.util.*;

public class BattleController {
    @FXML
    private ProgressBar playerHealthBar;
    @FXML
    private ProgressBar enemyHealthBar;
    @FXML
    private ImageView playerSprite;
    @FXML
    private ImageView enemySprite;
    @FXML
    private HBox attackButtons;

    private Player player;
    private Enemy enemy;
    private Scene previousScene;
    private Stage stage;
    private RoomController roomController;

    private Map<Attack, Integer> playerCooldowns = new HashMap<>();
    private Map<Attack, Integer> enemyCooldowns = new HashMap<>();


    public void startBattle(Player player, Enemy enemy, RoomController rc, Scene prevScene) {
        this.player = player;
        this.enemy = enemy;
        this.roomController = rc;
        this.previousScene = prevScene;
        this.stage = (Stage) prevScene.getWindow();

        // init cooldowns
        for (Attack atk : player.getEquippedWeapon().getAttacks()) playerCooldowns.put(atk, 0);
        for (Attack atk : enemy.getAttacks()) enemyCooldowns.put(atk, 0);

        // set sprites
        playerSprite.setImage(player.getEquippedWeapon().getIcon());
        enemySprite.setImage(new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/com/game/javasem/images/" + enemy.getSprite())
                )
        ));
        player.setCurrentHealth(player.getMaxHealth());
        updateHealthBars();
        renderAttackButtons();
    }

    private void renderAttackButtons() {
        attackButtons.getChildren().clear();
        for (Attack atk : player.getEquippedWeapon().getAttacks()) {
            int cd = playerCooldowns.getOrDefault(atk, 0);
            Button btn = new Button(atk.getName() + (cd > 0 ? " (CD:" + cd + ")" : ""));
            btn.setDisable(cd > 0);
            btn.setOnAction(e -> playerTurn(atk));
            attackButtons.getChildren().add(btn);
        }
    }

    private void playerTurn(Attack atk) {
        // Player attacks
        enemy.setCurrentHealth(enemy.getCurrentHealth() - atk.getDamage());
        playerCooldowns.put(atk, atk.getCooldown());
        updateHealthBars();
        if (enemy.getCurrentHealth() <= 0) {
            endBattle(true);
            return;
        }

        // Enemy attacks
        enemyTurn();
        if (player.getCurrentHealth() <= 0) return;

        // decrement cooldowns
        playerCooldowns.replaceAll((a, c) -> Math.max(0, c - 1));
        enemyCooldowns.replaceAll((a, c) -> Math.max(0, c - 1));
        renderAttackButtons();
    }

    private void enemyTurn() {
        List<Attack> available = new ArrayList<>();
        for (Attack atk : enemy.getAttacks()) {
            if (enemyCooldowns.getOrDefault(atk, 0) == 0) available.add(atk);
        }
        if (!available.isEmpty()) {
            Attack choice = available.get(new Random().nextInt(available.size()));
            player.setCurrentHealth(player.getCurrentHealth() - choice.getDamage());
            enemyCooldowns.put(choice, choice.getCooldown());
            updateHealthBars();
            if (player.getCurrentHealth() <= 0) endBattle(false);
        }
    }

    private void updateHealthBars() {
        playerHealthBar.setProgress((double) player.getCurrentHealth() / player.getMaxHealth());
        enemyHealthBar.setProgress((double) enemy.getCurrentHealth() / enemy.getHealth());
    }

    private void endBattle(boolean playerWon) {
        if (playerWon) {
            // award loot and handle boss
            roomController.handleBattleEnd(enemy);
        } else {
            try {
                Parent root = FXMLLoader.load(
                        getClass().getResource("/com/game/javasem/fxml/GameOver.fxml")
                );
                stage.setScene(new Scene(root));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}