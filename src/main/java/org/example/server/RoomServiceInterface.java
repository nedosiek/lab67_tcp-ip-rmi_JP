package org.example.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;



public interface RoomServiceInterface extends Remote {
    void createRoom(String roomName) throws RemoteException;
    void deleteRoom(String roomToken) throws RemoteException;
    int joinRoom(String playerToken, String roomToken) throws RemoteException;
    void leaveRoom(String playerToken, String roomToken) throws RemoteException;
    void resetRoom(String playerToken, String roomToken) throws RemoteException;
    boolean isYourTurn(String playerToken, String roomToken) throws RemoteException;
    void makeMove(String playerToken, String roomToken, int move) throws RemoteException;
    String checkWinner(String roomToken) throws RemoteException;
    String getOponent(String roomToken, String playerToken) throws RemoteException;
    ArrayList<String> getRooms() throws RemoteException;
    int getPlayerNumber(String roomToken) throws RemoteException;
    String[] getBoard(String roomToken) throws RemoteException;
    ArrayList<String> getPlayerStats() throws RemoteException;
    void updatePlayerStats(String playerToken, boolean isWin, boolean isDraw) throws RemoteException;
    boolean isWin(String playerToken, String roomToken) throws RemoteException;
    boolean isDraw(String roomToken) throws RemoteException;
    //Bloker TCP
    void registerClient(String username, String ipAddress, int port) throws RemoteException;
    void unregisterClient(String username) throws RemoteException;

}
