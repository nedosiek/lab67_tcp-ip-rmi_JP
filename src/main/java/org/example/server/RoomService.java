package org.example.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.UUID;

public class RoomService extends UnicastRemoteObject implements RoomServiceInterface {
    private static ArrayList<Room> Rooms = new ArrayList<>();
    private final ArrayList<ClientInfo> activeClients;

    private static final String DB_URL = "jdbc:sqlite:PlayersStats.db";
    private static Connection connection;

    public RoomService() throws RemoteException
    {
        super();
        initializeDatabase();
        activeClients = new ArrayList<>();
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }
    private void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String sql = """
                    CREATE TABLE IF NOT EXISTS PlayersStats (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nickname TEXT UNIQUE,
                        wins INTEGER DEFAULT 0,
                        losses INTEGER DEFAULT 0,
                        draws INTEGER DEFAULT 0
                    );
                    """;
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    @Override
    public void createRoom(String roomName) throws RemoteException {
            String roomToken = UUID.randomUUID() + "/" + roomName;
            Room room = new Room(roomToken);
            if (Rooms.contains(room)) {
                Logger.log("Room exited "+ roomToken);
                System.out.println("Room exited "+ roomToken);
            } else {
                Rooms.add(room);
                Logger.log("Created new room: " + roomToken);
                System.out.println("Created new room: " + roomToken);
            }

    }

    @Override
    public synchronized void deleteRoom(String roomToken) throws RemoteException {
        Iterator<Room> iterator = Rooms.iterator();
        while (iterator.hasNext()) {
            Room room = iterator.next();
            if (room.getToken().equals(roomToken)) {
                iterator.remove();
                Logger.log("Room deleted: " + roomToken);
                System.out.println("Room deleted: " + roomToken);
                break;
            }
        }
    }

    @Override
    public synchronized int joinRoom(String playerToken, String roomToken) throws RemoteException {
        for(Room room : Rooms){
            if(room.getToken().equals(roomToken)){
                if(room.getPlayerNumber() < 2) {
                    System.out.println("Player " + playerToken + " joined room " + roomToken);
                    Logger.log("Player " + playerToken + " joined room " + roomToken);
                    room.joinRoom(playerToken);
                    return 1;
                }
            }
        }
        return 0;

    }

    @Override
    public synchronized void leaveRoom(String playerToken, String roomToken) throws RemoteException {
        for (Room room : Rooms) {
            if (room.getToken().equals(roomToken)) {
                room.leaveRoom(playerToken);

                if (room.getPlayerNumber() == 0) {
                    deleteRoom(roomToken);
                }
                break;
            }
        }
    }

    @Override
    public synchronized void resetRoom(String playerToken, String roomToken) throws RemoteException {
        for (Room room : Rooms) {
            if (room.getToken().equals(roomToken)) {
                room.resetRoom(playerToken);
                return;
            }
        }
        throw new RemoteException("Room not found");
    }

    @Override
    public synchronized boolean isYourTurn(String playerToken, String roomToken) throws RemoteException {
        for(Room room : Rooms){
            if(room.getToken().equals(roomToken)){
                return room.isYourTurn(playerToken);
            }
        }
        return false;
    }

    @Override
    public synchronized void makeMove(String playerToken, String roomToken, int move) throws RemoteException {
        if(isYourTurn(playerToken,roomToken)){
            for(Room room : Rooms){
                if(room.getToken().equals(roomToken)){
                    room.makeMove(playerToken,move);
                }
            }
        }
    }

    @Override
    public synchronized String checkWinner(String roomToken) throws RemoteException {
        for(Room room : Rooms){
            if(room.getToken().equals(roomToken)){
                if(room.checkWinners() != null){
                    Logger.log("Room "+roomToken+" ended with winner: "+room.checkWinners());
                    return room.checkWinners();
                }else if(room.isBoardFull()){
                    Logger.log("Room "+roomToken+" ended with draw");
                    return "Draw";
                }
            }
        }
        return null;
    }


    @Override
    public ArrayList<String> getRooms() throws RemoteException {
        ArrayList<String> rooms = new ArrayList<>();
        for(Room room : Rooms){
            rooms.add(room.getToken());
        }
        return rooms;
    }
    @Override
    public synchronized int getPlayerNumber(String roomToken){
        for(Room room : Rooms){
            if(room.getToken().equals(roomToken)){
                return room.getPlayerNumber();
            }
        }
        return 0;
    }

    @Override
    public synchronized String[] getBoard(String roomToken) {
        for(Room room : Rooms){
            if(room.getToken().equals(roomToken)){
                return room.getBoard();
            }
        }
        return null;
    }

    @Override
    public ArrayList<String> getPlayerStats() throws RemoteException {
        ArrayList<String> stats = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT nickname, wins, losses, draws FROM PlayersStats")) {

            while (rs.next()) {
                String nickname = rs.getString("nickname");
                int wins = rs.getInt("wins");
                int losses = rs.getInt("losses");
                int draws = rs.getInt("draws");
                stats.add(String.format("Nick: %s | Wins: %d | Losses: %d | Draws: %d", nickname, wins, losses, draws));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return stats;
    }

    @Override
    public synchronized void updatePlayerStats(String playerToken, boolean isWin, boolean isDraw) throws RemoteException {
        String nickname = extractNicknameFromToken(playerToken);
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String insertPlayer = """
            INSERT OR IGNORE INTO PlayersStats (nickname, wins, losses, draws) VALUES (?, 0, 0, 0)
        """;
            try (PreparedStatement insertStmt = conn.prepareStatement(insertPlayer)) {
                insertStmt.setString(1, nickname);
                insertStmt.executeUpdate();
            }

            String updateStats = """
            UPDATE PlayersStats
            SET wins = wins + ?, losses = losses + ?, draws = draws + ?
            WHERE nickname = ?
        """;
            try (PreparedStatement updateStmt = conn.prepareStatement(updateStats)) {
                updateStmt.setInt(1, isWin ? 1 : 0);
                updateStmt.setInt(2, (!isWin && !isDraw) ? 1 : 0);
                updateStmt.setInt(3, isDraw ? 1 : 0);
                updateStmt.setString(4, nickname);
                updateStmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    @Override
    public synchronized boolean isWin(String playerToken, String roomToken) throws RemoteException {
        for(Room room : Rooms){
            if(room.getToken().equals(roomToken)){
                if(room.checkWinners() != null && room.checkWinners().equals(playerToken)){
                    return true;
                }

            }
        }
        return false;
    }
    @Override
    public synchronized boolean isDraw(String roomToken) throws RemoteException {
        for(Room room : Rooms){
            if(room.getToken().equals(roomToken)){
                if(room.checkWinners().equals("draw")){
                    return true;
                }
            }
        }
        return false;
    }

    private String extractNicknameFromToken(String token) {
        return token.substring(token.indexOf("@") + 1);
    }
    public void closeRoomService(){
        Rooms.clear();
        closeConnection();
        System.out.println("Server closed");
        Logger.log("Server closed");
    }

    @Override
    public void registerClient(String username, String ipAddress, int port) throws RemoteException {
        ClientInfo client = new ClientInfo(username, ipAddress, port);
        activeClients.add(client);
        System.out.println("Registered client: " + client);
        Logger.log("Registered client: " + client);
    }
    @Override
    public void unregisterClient(String username) throws RemoteException {
        Iterator<ClientInfo> iterator = activeClients.iterator();
        while (iterator.hasNext()) {
            ClientInfo client = iterator.next();
            if (client.getUsername().equals(username)) {
                iterator.remove();
                System.out.println("unregistered client: " + client);
                Logger.log("unregistered client: " + client);
                activeClients.remove(client);
            }
        }

    }

    @Override
    public synchronized String getOponent(String roomToken, String playerToken) throws RemoteException {
        for(Room room : Rooms){
            if(room.getToken().equals(roomToken)){
                String oponentToken = room.getOpponentToken(playerToken);
                System.out.println("oponentToken: "+oponentToken);
                if (oponentToken == null) {
                    return null;
                }
                for (ClientInfo client : activeClients) {
                    if (client.getUsername().equals(oponentToken)) {
                        String nickname = extractNicknameFromToken(oponentToken);
                        return client.getIpAddress()+"/"+client.getPort()+"/"+nickname;
                    }
                }
            }
        }
        System.out.println("No oponent found");
        return null;
    }

}
