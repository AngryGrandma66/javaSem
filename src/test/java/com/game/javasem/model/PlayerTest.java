package com.game.javasem.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerTest {
    private Player player;
    private GameItem mockWeapon;

    @BeforeEach
    void setUp() {
        // Vytvoříme hráče bez defaultního vybavení
        player = new Player(100, null, null, null);

        // Mock GameItem s typem WEAPON
        mockWeapon = mock(GameItem.class);
        when(mockWeapon.getType()).thenReturn(GameItem.Type.WEAPON);

        // Přidáme ho do inventáře, aby equipItem mohl fungovat
        player.getInventory().addItem(mockWeapon);
    }

    @Test
    void equipItem_ShouldMoveWeaponFromInventoryToEquippedSlot() {
        // Akce
        player.equipItem(mockWeapon);

        // Ověření: zbraň je v slotu a už není v inventáři
        assertEquals(mockWeapon, player.getEquippedWeapon(), "Weapon musí být v equip slotu");
        assertFalse(player.getInventory().getItems().contains(mockWeapon),
                "Weapon musí být odstraněna z inventáře");
    }

    @Test
    void unequipItem_ShouldReturnWeaponBackToInventory() {
        // Nejprve ji vyzbrojíme
        player.equipItem(mockWeapon);

        // Akce: odebereme zbraň
        player.unequipItem(GameItem.Type.WEAPON);

        // Ověření: slot je prázdný, zbraň je zpět v inventáři
        assertNull(player.getEquippedWeapon(), "Weapon slot musí být prázdný");
        assertTrue(player.getInventory().getItems().contains(mockWeapon),
                "Weapon musí být vrácena do inventáře");
    }

    @Test
    void equipItem_NewWeapon_ShouldSwapWithPreviouslyEquipped() {
        // Připravíme druhou mock zbraň
        GameItem newWeapon = mock(GameItem.class);
        when(newWeapon.getType()).thenReturn(GameItem.Type.WEAPON);
        player.getInventory().addItem(newWeapon);

        // 1) Equip první zbraň
        player.equipItem(mockWeapon);
        // 2) Equip druhou zbraň → první by se měla vrátit do inventáře
        player.equipItem(newWeapon);

        // Ověření:
        assertEquals(newWeapon, player.getEquippedWeapon(),
                "Slot musí obsahovat nově equipnutou zbraň");
        assertTrue(player.getInventory().getItems().contains(mockWeapon),
                "Původní zbraň musí být vrácena do inventáře");
        assertFalse(player.getInventory().getItems().contains(newWeapon),
                "Nová zbraň nesmí zůstat v inventáři");
    }
}