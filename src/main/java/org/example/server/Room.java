package org.example.server;

import java.util.*;

public class Room {
    private String token;

    private HashMap<String, String> players;
    private ArrayList<String> playerVotesReset;
    private String playerTurn;
    boolean firstMoveFlag;

    private String[] board = new String[9];

    public Room(String token) {
        this.setToken(token);
        this.players = new HashMap<>();
        this.playerVotesReset = new ArrayList<>();
        Arrays.fill(this.board, null);
    }
    protected synchronized void joinRoom(String playerToken) {
        if (getPlayerNumber() < 2) {
            firstMoveFlag = false;
            if (players.isEmpty()) {
                players.put(playerToken, "X");
            } else {
                if (!players.containsKey(playerToken))
                {
                    String playerSymbol = players.containsValue("X") ? "O" : "X";
                    players.put(playerToken, playerSymbol);
                    System.out.println("Gracz dołączył: " + playerToken);
                }

                playerVotesReset.clear();
                Logger.log("Player joined " + playerToken + " joined the "+ token + " room.");
            }
        }
    }

    protected synchronized void leaveRoom(String playerToken) {
        if (players.containsKey(playerToken)) {
            players.remove(playerToken);
            Logger.log("Player " + playerToken + " left the room. Room reset.");
            System.out.println("Player " + playerToken + " left the room.");
        }
        playerVotesReset.remove(playerToken);
        resetGame();

    }

    protected boolean isYourTurn(String playerToken){
        if(!firstMoveFlag){
            this.playerTurn = firstMove();
            firstMoveFlag = true;
        }
        return playerToken.equals(playerTurn);
    }

    protected String firstMove(){
        Random r = new Random();
        if (players.size() == 2) {
            return players.keySet().toArray()[r.nextInt(2)].toString();
        }
        System.out.println("Cannot assign first move: Not enough players.");
        return "";
    }

    protected String checkWinners() {
        for (int a = 0; a < 8; a++) {
            String line = switch (a) {
                case 0 -> board[0] + board[1] + board[2];
                case 1 -> board[3] + board[4] + board[5];
                case 2 -> board[6] + board[7] + board[8];
                case 3 -> board[0] + board[3] + board[6];
                case 4 -> board[1] + board[4] + board[7];
                case 5 -> board[2] + board[5] + board[8];
                case 6 -> board[0] + board[4] + board[8];
                case 7 -> board[2] + board[4] + board[6];
                default -> null;
            };

            switch (line) {
                case "XXX" -> {
                    for (Map.Entry<String, String> entry : players.entrySet()) {
                        if (entry.getValue().equals("X")) {
                            return entry.getKey();
                        }
                    }
                }
                case "OOO" -> {
                    for (Map.Entry<String, String> entry : players.entrySet()) {
                        if (entry.getValue().equals("O")) {
                            return entry.getKey();
                        }
                    }
                }
            }
            if(isBoardFull())
            {
                return "draw";
            }
        }
        if(players.size() != 2){
            for (Map.Entry<String, String> entry : players.entrySet()) {
                return entry.getKey();
            }
        }
        return null;
    }
    protected void makeMove(String playerToken, int move){
        if(board[move] == null) {
            board[move] = players.get(playerToken);
        }
        for(String player : players.keySet()){
            if(!player.equals(playerToken)){
                this.playerTurn = player;
                break;
            }
        }
    }
    protected synchronized void resetRoom(String playerToken) {
        if (playerVotesReset == null) {
            playerVotesReset = new ArrayList<>();
        }

        if (!playerVotesReset.contains(playerToken)) {
            playerVotesReset.add(playerToken);
        }

        if (playerVotesReset.size() == players.size()) {
            resetGame();
            playerVotesReset.clear();
        }
    }

    private void resetGame() {
        Arrays.fill(board, null);
        this.playerVotesReset.clear();
        if (players.size() == 2) {
            this.playerTurn = firstMove();
        } else {
            this.playerTurn = null;
        }
        firstMoveFlag = false;
        Logger.log("Room " + token + " was reset.");
    }

    public boolean isBoardFull() {
        for (String position : board) {
            if (position == null) {
                return false;
            }
        }
        return true;
    }

    protected int getPlayerNumber(){
        return this.players.size();

    }

    protected synchronized String getOpponentToken(String playerToken) {
        if (players == null || players.isEmpty()) {
            System.err.println("Lista graczy jest pusta.");
            return null;
        }

        if (players.size() < 2) {
            System.err.println("Nie ma wystarczającej liczby graczy w pokoju do znalezienia przeciwnika. Obecni gracze: " + players.keySet());
            return null;
        }

        for (String token : players.keySet()) {
            if (!token.equals(playerToken)) {
                System.out.println("Przeciwnik znaleziony: " + token);
                return token;
            }
        }

        System.err.println("Nie znaleziono przeciwnika dla gracza: " + playerToken);
        return null;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String[] getBoard() {
        return board;
    }
}
