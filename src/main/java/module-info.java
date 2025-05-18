module com.game.javasem {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens com.game.javasem to javafx.fxml;
    exports com.game.javasem;
}