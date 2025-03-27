package org.example.client.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.client.ChatService;
import org.example.client.ClientStart;
import org.example.client.ServerService;

import java.rmi.RemoteException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class GameController {
    @FXML
    private Label whosTurn;

    @FXML
    private GridPane gridPane;

    @FXML
    private VBox chatBox;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private TextArea messageInput;

    protected ChatService chatService;

    @FXML
    public void initialize()  {
        ServerService serverService = ClientStart.getServerService();
        serverService.waitForAnotherPlayer(ClientStart.userToken, ClientStart.roomToken, whosTurn,gridPane);
        initializeChat();
    }

    @FXML
    public void handleLeave() {
        try {

            if (ClientStart.userToken == null || ClientStart.roomToken == null) {
                System.out.println("Invalid tokens: userToken=" + ClientStart.userToken + ", roomToken=" + ClientStart.roomToken);
                return;
            }

            boolean leftRoom = ClientStart.getServerService().leaveRoom(ClientStart.userToken, ClientStart.roomToken);

            if (leftRoom) {
                ClientStart.setRoomToken(null);
                Stage stage = (Stage) gridPane.getScene().getWindow();
                SceneController sceneController = new SceneController(stage);
                sceneController.switchTo("RoomListView.fxml");
            }
            if (chatService != null) {
                chatService.stopServer();
            }

        } catch (Exception e) {
            System.out.println("Leave room error: " + e.getMessage());
        }
    }
    private void initializeChat() {
        if (chatService == null) {
            chatService = new ChatService();
        }
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (ClientStart.getServerService().getPlayerNumber(ClientStart.roomToken) == 2) {
                    scheduler.shutdown();
                    Platform.runLater(this::startChatServer);
                }
            } catch (RemoteException e) {
                System.err.println("Błąd sprawdzania gotowości pokoju: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void startChatServer() {
        try {
            chatService = new ChatService();
            chatService.startServer(message -> Platform.runLater(() -> displayReceivedMessage(message)));

        } catch (Exception e) {
            System.err.println("Błąd podczas uruchamiania serwera czatu: " + e.getMessage());
        }
    }
    private void displayReceivedMessage(String message) {
        Text receivedMessage = new Text("["+chatService.getOponentNick()+"] "+message);
        receivedMessage.setStyle("-fx-fill: black;");
        chatBox.getChildren().add(receivedMessage);

        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }

    private void displaySentMessage(String username, String message) {
        Text sentMessage = new Text("[" + username + "] " + message);
        sentMessage.setStyle("-fx-fill: green;");
        chatBox.getChildren().add(sentMessage);

        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }
    @FXML
    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        new Thread(() -> {
            try {
                chatService.sendMessage(message);
                Platform.runLater(() -> displaySentMessage(ClientStart.userName, message));
            } catch (Exception e) {
                System.err.println("Error sending message: " + e.getMessage());
            }
        }).start();

        messageInput.clear();
    }
}
