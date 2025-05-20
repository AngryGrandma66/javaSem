module com.game.javasem {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.fasterxml.jackson.databind;


    opens com.game.javasem.controllers to javafx.fxml;

    opens com.game.javasem to javafx.fxml;
    exports com.game.javasem;
    exports com.game.javasem.model.mapObjects to com.fasterxml.jackson.databind;
    opens com.game.javasem.model.mapObjects to com.fasterxml.jackson.databind;
    exports com.game.javasem.controllers;
    exports com.game.javasem.model.map to com.fasterxml.jackson.databind;
    opens com.game.javasem.model.map to com.fasterxml.jackson.databind;
}