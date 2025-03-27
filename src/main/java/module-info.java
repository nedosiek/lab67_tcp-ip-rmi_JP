module org.example.java {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;
    requires java.sql;

    exports org.example.client;
    opens org.example.client to javafx.fxml;
    opens org.example.client.controllers to javafx.fxml;

    exports org.example.server to java.rmi;
    exports org.example.client.controllers;
}