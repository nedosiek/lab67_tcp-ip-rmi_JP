package org.example.client;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import javafx.stage.Stage;
import org.example.client.controllers.SceneController;
import org.example.server.RoomServiceInterface;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerService {

    private final RoomServiceInterface roomService;
    private final Map<String, ScheduledExecutorService> schedulerMap = new ConcurrentHashMap<>();

    public ServerService(RoomServiceInterface roomService) {
        this.roomService = roomService;
    }

    public Task<ArrayList<String>> loadRoomsTask() {
        return new Task<>() {
            @Override
            protected ArrayList<String> call() throws RemoteException {
                return roomService.getRooms();
            }
        };
    }
    public Task<ArrayList<String>> loadStats(){
        return new Task<>() {
            @Override
            protected ArrayList<String> call() throws RemoteException {
                return roomService.getPlayerStats();
            }
        };
    }

    public void createRoom(String roomName) throws RemoteException {
        roomService.createRoom(roomName);
    }

    public synchronized int joinRoom(String userToken, String roomToken) throws RemoteException {
        return roomService.joinRoom(userToken, roomToken);
    }

    public synchronized boolean leaveRoom(String playerToken, String roomToken) {
        try {
            roomService.leaveRoom(playerToken, roomToken);
            try (ScheduledExecutorService scheduler = schedulerMap.remove(roomToken)) {
                if (scheduler != null) {
                    scheduler.shutdownNow();
                }
            }
            return true;
        } catch (RemoteException e) {
            System.out.println("Error leaving room: " + e.getMessage());
            return false;
        }
    }

    public int getPlayerNumber(String roomToken) throws RemoteException {
        return roomService.getPlayerNumber(roomToken);
    }

    public void handleGame(String userToken, String roomToken, Label waitingLabel, GridPane gridPane) throws RemoteException {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                try {

                    int playerCount = ClientStart.roomService.getPlayerNumber(roomToken);
                    boolean isYourTurn = ClientStart.roomService.isYourTurn(userToken, roomToken);
                    String winner = ClientStart.roomService.checkWinner(roomToken);

                    if (playerCount < 2) {
                        Platform.runLater(() -> {
                            gridPane.getChildren().clear();
                            waitingLabel.setText("Opponent left the room. Waiting for a new opponent...");
                        });
                        return;
                    }


                    if (winner != null) {
                        updatePlayerStats(userToken,roomToken);
                        if (winner.contains("@")) {
                            winner = winner.split("@")[1];
                        }
                        String finalWinner = winner;

                        Platform.runLater(() -> {
                            gridPane.getChildren().clear();
                            waitingLabel.setText("Winner: " + finalWinner);

                            Button rematchButton = new Button("Rematch");
                            rematchButton.setPrefSize(100, 50);
                            rematchButton.setOnAction(e -> {
                                try {
                                    ClientStart.roomService.resetRoom(userToken, roomToken);
                                    SceneController sceneController = new SceneController((Stage) waitingLabel.getScene().getWindow());
                                    sceneController.switchTo("GameView.fxml");
                                    Thread.sleep(1000);
                                } catch (RemoteException ex) {
                                    System.out.println("Remote exception: " + ex.getMessage());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            });


                            gridPane.add(rematchButton, 1, 1);
                        });

                        scheduler.shutdown();
                        return;
                    }


                    if (isYourTurn && playerCount == 2) {
                        String[] board = ClientStart.roomService.getBoard(roomToken);
                        Platform.runLater(() -> refreshBoard(gridPane, board, "Your turn", waitingLabel,true));
                    } else if (playerCount == 2) {
                        String[] board = ClientStart.roomService.getBoard(roomToken);
                        Platform.runLater(() -> {
                                waitingLabel.setText("Waiting for opponent's move...");
                                refreshBoard(gridPane, board, "Waiting...", waitingLabel,false);
                        });
                    }
                } catch (RemoteException e) {
                    System.out.println("Remote exception: " + e.getMessage());
                }
            }, 0, 1, TimeUnit.SECONDS);

    }


    private void refreshBoard(GridPane gridPane, String[] board, String message, Label waitingLabel,boolean isYourTurn) {
        gridPane.getChildren().clear();
        for (int i = 0; i < board.length; i++) {
            if ((board[i] == null || board[i].isEmpty()) && isYourTurn) {
                Button button = getButton(gridPane, waitingLabel, i);
                gridPane.add(button, i % 3, i / 3);
                GridPane.setHalignment(button, HPos.CENTER);
                GridPane.setValignment(button, VPos.CENTER);
            } else {
                if (board[i] == null || board[i].isEmpty()) {
                    continue;
                }
                Label label = new Label(board[i]);
                label.setPrefSize(100, 100);
                label.setStyle("-fx-font-size: 50px; -fx-font-weight: bold; -fx-background-color: #000000; -fx-text-fill: #ffffff;");
                label.setAlignment(javafx.geometry.Pos.CENTER);
                gridPane.add(label, i % 3, i / 3);
                GridPane.setHalignment(label, HPos.CENTER);
                GridPane.setValignment(label, VPos.CENTER);
            }
        }
        waitingLabel.setText(message);
    }

    private Button getButton(GridPane gridPane, Label waitingLabel, int i) {
        Button button = new Button();
        button.setPrefSize(60, 60);
        final int position = i;
        button.setOnAction(e -> {
            try {
                ClientStart.roomService.makeMove(ClientStart.userToken, ClientStart.roomToken, position);
                String[] updatedBoard = ClientStart.roomService.getBoard(ClientStart.roomToken);
                refreshBoard(gridPane, updatedBoard, "Waiting...", waitingLabel,true);
            } catch (RemoteException ex) {
                System.out.println("Remote exception: " + ex.getMessage());
            }
        });
        button.setStyle("-fx-background-color: #000000; -fx-text-fill: #ffffff; -fx-radius: 10px; -fx-border-color: #ffffff; -fx-border-width: 2px;");
        return button;
    }

    public void waitForAnotherPlayer(String userToken, String roomToken, Label waitingLabel, GridPane gridPane) {
        Platform.runLater(() -> waitingLabel.setText("Waiting for another player..."));
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        schedulerMap.put(roomToken, executor);
        executor.scheduleAtFixedRate(() -> {
            try {
                int playerNumber = getPlayerNumber(roomToken);

                if (playerNumber == 2) {
                    Platform.runLater(() -> {
                        try {
                            handleGame(userToken, roomToken, waitingLabel, gridPane);
                        } catch (RemoteException e) {
                            System.out.println("Error in handleGame: " + e.getMessage());
                        }
                    });
                    executor.shutdown();
                }
            } catch (RemoteException e) {
                System.out.println("Error fetching player count: " + e.getMessage());
                executor.shutdown();
            }
        }, 0, 1, TimeUnit.SECONDS);

        executor.schedule(() -> {
            if (!executor.isShutdown()) {
                Platform.runLater(() -> {
                    ClientStart.setRoomToken(null);
                    Stage stage = (Stage) waitingLabel.getScene().getWindow();
                    SceneController sceneController = new SceneController(stage);
                    sceneController.switchTo("RoomListView.fxml");
                });
                executor.shutdown();
            }
        }, 30, TimeUnit.SECONDS);
    }

    public void updatePlayerStats(String userToken, String roomToken) throws RemoteException {
        roomService.updatePlayerStats(userToken,roomService.isWin(userToken,roomToken),roomService.isDraw(roomToken));
    }

    public void closeClient() {
        unregisterClient(ClientStart.userToken);
        if(ClientStart.roomToken != null)
            leaveRoom(ClientStart.userToken,ClientStart.roomToken);
        for (ScheduledExecutorService scheduler : schedulerMap.values()) {
            if (scheduler != null) {
                scheduler.shutdownNow();
            }
        }

    }
    public void unregisterClient(String userToken) {
        try {
            roomService.unregisterClient(userToken);
        } catch (RemoteException e) {
            System.out.println("Error unregistering client: " + e.getMessage());
        }
    }

}