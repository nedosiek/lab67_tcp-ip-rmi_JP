package org.example.client.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.client.ClientStart;

import java.io.IOException;

public class SceneController {

    private final Stage stage;

    public SceneController(Stage stage) {
        this.stage = stage;
    }

    public void switchTo(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(ClientStart.class.getResource(fxmlFile));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            System.out.println("Failed to load scene: " + e.getMessage());
        }
    }
}