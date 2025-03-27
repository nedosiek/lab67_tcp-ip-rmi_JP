package org.example.client;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.server.Logger;
import org.example.server.RoomServiceInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientStart extends Application{
    public static RoomServiceInterface roomService;
    static String host = "localhost";
    static int port= 1001;
    public static String userToken;
    public static String userName;
    public static String roomToken;
    private static ServerService serverService;
    public static int clientPort;

    public static void main(String[] args) {
        parseArgs(args);
        launch();
    }

    @Override
    public void start(Stage stage) {
        try {

            clientPort = port + (int)(Math.random() * 10000);

            Registry registry = LocateRegistry.getRegistry(host, port);
            roomService = (RoomServiceInterface) registry.lookup("RoomService");
            serverService = new ServerService(roomService);

            FXMLLoader fxmlLoader = new FXMLLoader(ClientStart.class.getResource("ClientLoginView.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 964, 595);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            System.out.println("Failed to start client: " + e.getMessage());
            System.exit(0);
        }
    }

    @Override
    public void stop() {

        try {
            if (serverService != null) {
                serverService.closeClient();
            }
        } catch (Exception e) {
            System.err.println("Błąd podczas zamykania serwisu: " + e.getMessage());
            System.out.println(e.getMessage());
        }

        Platform.exit();
        System.exit(0);
    }

    public static ServerService getServerService() {
        return serverService;
    }


    public static void setUserToken(String userToken) {
        ClientStart.userToken = userToken;
    }

    public static void setUserName(String userName) {
        ClientStart.userName = userName;
    }

    public static void setRoomToken(String roomToken) {
        ClientStart.roomToken = roomToken;
    }

    private static void parseArgs(String[] args) {
        if(args.length == 2){
            try{
                port = Integer.parseInt(args[1]);
                host = args[0];
                System.out.println("Connected with\n>"+host+":"+port);
            }catch (NumberFormatException e){
                System.out.println("Port must be a number");
            }
        }else if(args.length == 1 && args[0].equals("--help")){
            System.out.println("""
                usage: <name_of_jar>.jar [port]
                        [port]: port number on which server will be hosted, default is 10001
                """);
            System.exit(0);
        }else if(args.length == 0){
            System.out.println("No ip and port specified, using default ip: " + host + " port: " + port);
        }
    }
}
