package org.example.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import static org.example.server.Logger.closeLogger;

public class Main {
    static RoomService RoomService;
    static int port = 1001;
    
    public static void main(String[] args) {
        try {
            parseArgs(args);

            RoomService = new RoomService();
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("RoomService", RoomService);
            System.out.println("Tic-Tac-Toe Server is ready on port " + port);
            Logger.log("Server started on port " + port);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                Logger.log("Server is shutting down.");
                System.out.println("Server is shutting down...");
                RoomService.closeRoomService();
                RoomService = null;
                closeLogger();
            }));
            Socket clientSocket;
            try (ServerSocket serverSocket = new ServerSocket(port + 1)) {
                clientSocket = serverSocket.accept();
            }
            System.out.println(clientSocket.getInetAddress().getHostAddress());

        } catch (Exception e) {
            System.out.println("Server failed " + e.getMessage());
            Logger.log("Server failed: " + e.getMessage());
        }
    }

    private static void parseArgs(String[] args) {
        if (args.length > 0) {
            if(args[0].equals("--help"))
            {
                System.out.println("""
                    usage: <name_of_jar>.jar [port]
                            [port]: port number on which server will be hosted, default is 10001
                    """);
                System.exit(0);
            }else{
                try{
                    port = Integer.parseInt(args[0]);
                    System.out.println("Port set to: " + port);
                    Logger.log("Port set to: " + port);
                }catch (NumberFormatException e){
                    System.out.println("Port must be a number");
                }
            }
        }else {
            System.out.println("No port specified, using default port: " + port);
            Logger.log("No port specified, using default port: " + port);
        }
    }
}
