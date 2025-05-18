module com.game.javasem {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.fasterxml.jackson.databind;

    opens com.game.javasem.model to com.fasterxml.jackson.databind;
    exports com.game.javasem.model to com.fasterxml.jackson.databind;
    opens com.game.javasem to javafx.fxml;
    exports com.game.javasem;
}