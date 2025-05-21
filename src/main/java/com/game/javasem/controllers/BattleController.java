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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BattleController {
    private static final Logger log = LoggerFactory.getLogger(BattleController.class);

    @FXML private ProgressBar playerHealthBar;
    @FXML private ProgressBar enemyHealthBar;
    @FXML private ImageView playerSprite;
    @FXML private ImageView enemySprite;
    @FXML private HBox attackButtons;

    private Player player;
    private Enemy enemy;
    private Stage stage;
    private RoomController roomController;

    private final Map<Attack, Integer> playerCooldowns = new HashMap<>();
    private final Map<Attack, Integer> enemyCooldowns  = new HashMap<>();

    public void startBattle(Player player, Enemy enemy, RoomController rc, Scene prevScene) {
        this.player = player;
        this.enemy  = enemy;
        this.roomController = rc;
        this.stage = (Stage) prevScene.getWindow();

        log.info("Battle started: player HP={} vs enemy '{}' HP={}",
                player.getCurrentHealth(), enemy.getType(), enemy.getHealth());

        player.getEquippedWeapon().getAttacks()
                .forEach(atk -> playerCooldowns.put(atk, 0));
        enemy.getAttacks()
                .forEach(atk -> enemyCooldowns.put(atk, 0));

        playerSprite.setImage(player.getEquippedWeapon().getIcon());
        enemySprite.setImage(new Image(
                Objects.requireNonNull(getClass()
                        .getResourceAsStream("/com/game/javasem/images/" + enemy.getSprite()))
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
        log.info("Player uses '{}' for {} damage", atk.getName(), atk.getDamage());
        enemy.setCurrentHealth(enemy.getCurrentHealth() - atk.getDamage());
        playerCooldowns.put(atk, atk.getCooldown());
        updateHealthBars();

        if (enemy.getCurrentHealth() <= 0) {
            log.info("Enemy '{}' defeated!", enemy.getType());
            endBattle(true);
            return;
        }

        enemyTurn();
        if (player.getCurrentHealth() <= 0) {
            // enemyTurn already logs defeat
            return;
        }

        // tick down cooldowns
        playerCooldowns.replaceAll((a, c) -> Math.max(0, c - 1));
        enemyCooldowns.replaceAll((a, c) -> Math.max(0, c - 1));
        renderAttackButtons();
    }

    private void enemyTurn() {
        List<Attack> available = new ArrayList<>();
        for (Attack atk : enemy.getAttacks()) {
            if (enemyCooldowns.getOrDefault(atk, 0) == 0) {
                available.add(atk);
            }
        }
        if (available.isEmpty()) {
            log.debug("Enemy '{}' has no attacks available (all on cooldown)", enemy.getType());
            return;
        }

        Attack choice = available.get(new Random().nextInt(available.size()));
        log.info("Enemy '{}' uses '{}' for {} damage", enemy.getType(),
                choice.getName(), choice.getDamage());
        player.setCurrentHealth(player.getCurrentHealth() - choice.getDamage());
        enemyCooldowns.put(choice, choice.getCooldown());
        updateHealthBars();

        if (player.getCurrentHealth() <= 0) {
            log.info("Player defeated by enemy '{}'", enemy.getType());
            endBattle(false);
        }
    }

    private void updateHealthBars() {
        double pPct = (double) player.getCurrentHealth() / player.getMaxHealth();
        double ePct = (double) enemy.getCurrentHealth() / enemy.getHealth();
        playerHealthBar.setProgress(pPct);
        enemyHealthBar.setProgress(ePct);
        log.debug("Health update → player: {}%, enemy: {}%",
                Math.round(pPct*100), Math.round(ePct*100));
    }

    private void endBattle(boolean playerWon) {
        if (playerWon) {
            roomController.handleBattleEnd(enemy);
        } else {
            try {
                Parent root = FXMLLoader.load(
                        getClass().getResource("/com/game/javasem/fxml/GameOver.fxml")
                );
                stage.setScene(new Scene(root));
            } catch (Exception ex) {
                log.error("Error loading GameOver screen", ex);
            }
        }
    }
}
