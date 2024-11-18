module org.tsamper.proyecto_final {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires org.json;
    requires javafx.web;
    requires java.desktop;

    opens org.tsamper.proyecto_final.modelo to javafx.fxml;
    exports org.tsamper.proyecto_final.modelo;
    exports org.tsamper.proyecto_final.vista;
    opens org.tsamper.proyecto_final.vista to javafx.fxml;
    exports org.tsamper.proyecto_final.controlador;
    opens org.tsamper.proyecto_final.controlador to javafx.fxml;
    exports org.tsamper.proyecto_final.modelo.enums;
    opens org.tsamper.proyecto_final.modelo.enums to javafx.fxml;
    exports org.tsamper.proyecto_final.modelo.bbdd;
    opens org.tsamper.proyecto_final.modelo.bbdd to javafx.fxml;
    exports org.tsamper.proyecto_final.modelo.constantes;
    opens org.tsamper.proyecto_final.modelo.constantes to javafx.fxml;
}