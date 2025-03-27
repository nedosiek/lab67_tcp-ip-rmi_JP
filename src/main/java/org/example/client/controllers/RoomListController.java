package org.example.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.client.ClientStart;
import org.example.client.ServerService;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class RoomListController {

    @FXML
    private VBox roomContainer;
    @FXML
    private VBox roomContainer1;
    @FXML
    private Label usernameLabel;
    @FXML
    private TextField roomName;

    private ServerService serverService;

    @FXML
    public void initialize() {
        this.serverService = ClientStart.getServerService();
        loadRooms();
        loadStats();
        loadUsername();
    }
    @FXML
    private void loadRooms() {
        roomContainer.getChildren().clear();
        var task = serverService.loadRoomsTask();

        task.setOnSucceeded(event -> {
            ArrayList<String> rooms = task.getValue();
            rooms.forEach(token -> {
                Button button = new Button(getRoomDisplayText(token));
                button.setOnAction(e -> handleJoinRoom(token));
                roomContainer.getChildren().add(button);
            });
        });

        task.setOnFailed(event -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Failed to load rooms.");
            alert.show();
        });

        new Thread(task).start();
    }


    private String getRoomDisplayText(String token) {
        try {
            return token.split("/")[1] + " " + serverService.getPlayerNumber(token) + "/2";
        } catch (RemoteException e) {
            return "Error";
        }
    }
    @FXML
    private void handleJoinRoom(String token) {
        try {
            if(serverService.getPlayerNumber(token) == 2)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Room is full.");
                alert.show();
                return;
            }

            if(serverService.joinRoom(ClientStart.userToken, token) == 1) {
                ClientStart.setRoomToken(token);

                Stage stage = (Stage) usernameLabel.getScene().getWindow();
                SceneController sceneController = new SceneController(stage);
                sceneController.switchTo("GameView.fxml");
            }else{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Room does not exist.");
                alert.show();
                loadRooms();
            }

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Failed to join room.");
            alert.show();
        }
    }

    @FXML
    private void handleCreateRoom() {
        try {
            if (roomName.getLength() < 3) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Room name must be at least 3 characters long.");
                alert.showAndWait();
            }else {
                serverService.createRoom(roomName.getText());
                loadRooms();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Could not create room.");
            alert.show();
        }
    }
    private void loadUsername()
    {
        usernameLabel.setText("Username: "+ClientStart.userName);
    }

    @FXML
    private void loadStats() {
        roomContainer1.getChildren().clear();
        var task = serverService.loadStats();

        task.setOnSucceeded(event -> {
            ArrayList<String> playerStats = task.getValue();
            for (String stat : playerStats) {
                Label label = new Label(stat);
                roomContainer1.getChildren().add(label);
            }
        });

        task.setOnFailed(event -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Failed to load stats.");
            alert.show();
        });

        new Thread(task).start();
    }
}