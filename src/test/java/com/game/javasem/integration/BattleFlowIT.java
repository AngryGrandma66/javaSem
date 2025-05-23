package com.game.javasem.integration;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.Optional;
import static org.testfx.assertions.api.Assertions.assertThat;

public class BattleFlowIT extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        // launch your main app (or only the battle scene)
        new com.game.javasem.App().start(stage);
    }

    @BeforeEach
    public void setUp() {
        // navigate to the room/battle screen,
        // e.g. click on “Enter Room” button, etc.
        clickOn("#enterRoomButton");
    }

    @Test
    public void whenPlayerAttacksEnemy_thenEnemyHpDecreases() {
        // assume there’s a label showing enemy HP with fx:id="enemyHpLabel"
        Label before = lookup("#enemyHpLabel").query();
        int hpBefore = Integer.parseInt(before.getText());

        // click the attack button:
        clickOn("#attackPunchButton");

        Label after = lookup("#enemyHpLabel").query();
        int hpAfter = Integer.parseInt(after.getText());

        assertThat(hpAfter).isLessThan(hpBefore);
    }

    @Test
    public void whenEnemyDies_thenBackToRoom() {
        // repeatedly click the heavy-attack until enemy dies:
        while (!lookup("#battleEndDialog").tryQuery().isPresent()) {
            clickOn("#attackHeavyButton");
        }
        Node roomView = lookup("#roomViewRoot").query();
        assertThat(roomView).isVisible();    }
}
