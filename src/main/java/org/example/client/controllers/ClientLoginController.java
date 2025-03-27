package org.example.client.controllers;


import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.client.ClientStart;

import java.util.UUID;

public class ClientLoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private void handleLogin() {
        try {
            if (usernameField.getLength() < 3) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Username must be at least 3 characters long.");
                alert.showAndWait();
                return;
            }

            String username = usernameField.getText();
            ClientStart.setUserToken(UUID.randomUUID() + "@" + username);
            ClientStart.setUserName(username);
            String localIP = java.net.InetAddress.getLocalHost().getHostAddress();
            ClientStart.roomService.registerClient(ClientStart.userToken,localIP,ClientStart.clientPort);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            SceneController sceneController = new SceneController(stage);
            sceneController.switchTo("RoomListView.fxml");

        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
        }
    }
}