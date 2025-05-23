package com.game.javasem.controllers;

import com.game.javasem.model.mapObjects.MapObject;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class InteractionServiceTest {

    private Pane tileLayer;
    private InteractionService service;
    private ImageView character;

    @BeforeEach
    void setUp() {
        tileLayer = new Pane();
        service   = new InteractionService(tileLayer);

        // nastavení velikosti buněk na 10×10 px
        service.updateCellSize(10, 10);

        // vytvoříme postavu s 10×10 rozměrem umístěnou na (0,0),
        // takže její střed je v (5,5)
        character = new ImageView();
        character.setFitWidth(10);
        character.setFitHeight(10);
        character.setLayoutX(0);
        character.setLayoutY(0);
    }

    @Test
    void findNearbyObject_ShouldReturnNearestWithinRadius() {
        // připravíme dva MapObjecty: near a far
        MapObject nearObj = mock(MapObject.class);
        ImageView ivNear = new ImageView();
        ivNear.setFitWidth(10);
        ivNear.setFitHeight(10);
        // pozice tak, že střed bude v (5+10, 5) = (15,5) → relativně (15−5)/10 = 1 < 1.2
        ivNear.setLayoutX(10);
        ivNear.setLayoutY(0);
        ivNear.setUserData(nearObj);

        MapObject farObj = mock(MapObject.class);
        ImageView ivFar = new ImageView();
        ivFar.setFitWidth(10);
        ivFar.setFitHeight(10);
        // pozice středem v (35,5) → relativně (35−5)/10 = 3 > 1.2
        ivFar.setLayoutX(30);
        ivFar.setLayoutY(0);
        ivFar.setUserData(farObj);

        tileLayer.getChildren().addAll(ivNear, ivFar);

        MapObject result = service.findNearbyObject(character);
        assertSame(nearObj, result, "Má vrátit nejbližší objekt v dosahu");
    }

    @Test
    void findNearbyObject_ShouldReturnNullWhenNoneWithinRadius() {
        MapObject farOnly = mock(MapObject.class);
        ImageView ivFar = new ImageView();
        ivFar.setFitWidth(10);
        ivFar.setFitHeight(10);
        ivFar.setLayoutX(30);
        ivFar.setLayoutY(0);
        ivFar.setUserData(farOnly);

        tileLayer.getChildren().add(ivFar);

        assertNull(service.findNearbyObject(character),
                "Když není žádný objekt v dosahu, vrací null");
    }
}
