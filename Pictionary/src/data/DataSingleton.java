package data;

import javafx.beans.property.SimpleBooleanProperty;
import network.client.Client;
import network.server.ServerClient;

import java.util.*;

public final class DataSingleton extends Observable {

    private static DataSingleton INSTANCE;
    private Client client;
    private DrawData drawData;
    private boolean isDrawing;
    private String message = "";
    private String sendMessage = "";

    // Game variables
    private String[] guessWords = {"fiets", "boot", "kaas", "worst", "yoghurt"};
    private String wordToGuess;
    private int rounds = 3;
    private int currentRound = 0;
    private ArrayList<ServerClient> clients;
    private boolean wordHasBeenGuessed = false;
    private int currentTime;
    private int currentTimeServer;
    private SimpleBooleanProperty turnSwitchIndicator = new SimpleBooleanProperty(false);
    private Queue<DrawData> drawQueue = new LinkedList<>();
    private ArrayList<String> players = new ArrayList<>();

    private DataSingleton() {
    }

    public synchronized static DataSingleton getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new DataSingleton();
        }

        return INSTANCE;
    }

    // getters and setters
    public Client getClient() {
        return client;
    }
    public void setClient(Client client) {
        this.client = client;
    }
    public DrawData getDrawData() {
        return drawData;
    }
    public void setDrawData(DrawData drawData) {
        this.drawData = drawData;
        setChanged();
        notifyObservers();
    }
    public boolean isDrawing() {
        return isDrawing;
    }
    public void setDrawing(boolean drawing) {
        isDrawing = drawing;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getSendMessage() {
        return sendMessage;
    }
    public void setSendMessage(String sendMessage) {
        this.sendMessage = sendMessage;
    }
    public String[] getGuessWords() {
        return guessWords;
    }
    public String getWordToGuess() {
        return wordToGuess;
    }
    public void setWordToGuess(String wordToGuess) {
        this.wordToGuess = wordToGuess;
    }
    public int getRounds() {
        return rounds;
    }
    public void setRounds(int rounds) {
        this.rounds = rounds;
    }
    public int getCurrentRound() {
        return currentRound;
    }
    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }
    public ArrayList<ServerClient> getClients() {
        return clients;
    }
    public void setClients(ArrayList<ServerClient> clients) {
        this.clients = clients;
    }
    public boolean wordHasBeenGuessed() {
        return wordHasBeenGuessed;
    }
    public void setWordHasBeenGuessed(boolean wordHasBeenGuessed) {
        this.wordHasBeenGuessed = wordHasBeenGuessed;
    }
    public int getCurrentTime() {
        return currentTime;
    }
    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
    }
    public SimpleBooleanProperty getTurnSwitchIndicator() {
        return turnSwitchIndicator;
    }
    public void setTurnSwitchIndicator(boolean switchTurnIndicator) {
        this.turnSwitchIndicator.set(switchTurnIndicator);
    }
    public int getCurrentTimeServer() {
        return currentTimeServer;
    }
    public void setCurrentTimeServer(int currentTimeServer) {
        this.currentTimeServer = currentTimeServer;
    }
    public Queue<DrawData> getDrawQueue() {
        return drawQueue;
    }
    public void setDrawQueue(Queue<DrawData> drawQueue) {
        this.drawQueue = drawQueue;
    }
    public ArrayList<String> getPlayers() {
        return players;
    }
    public void setPlayers(ArrayList<String> players) {
        this.players = players;
    }
}