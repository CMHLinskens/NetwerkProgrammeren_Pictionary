package game;

import data.DataSingleton;
import network.server.Server;
import network.server.ServerClient;

import java.util.*;

public class Game implements Runnable {
    private Server server;
    private ArrayList<ServerClient> players;
    private HashMap<ServerClient, Boolean> playersWhoGuessed = new HashMap<>();
    private boolean timeIsOver;
    private int turnTimer;
    private int currentTime;
    private Timer timer;

    public Game(Server server, int rounds, int turnTimer){
        this.server = server;
        this.turnTimer = turnTimer;
        this.currentTime = this.turnTimer;
        this.timer = new Timer();
        DataSingleton.getInstance().setRounds(rounds);
        this.players = DataSingleton.getInstance().getClients();
        for(ServerClient player : DataSingleton.getInstance().getClients()){
            playersWhoGuessed.put(player, false);
        }
    }

    @Override
    public void run() {
        Random random = new Random();
        // Setting up the turn timer
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(currentTime <= 0){
                    timeIsOver = true;
                } else {
                    currentTime--;
                }
                System.out.println(currentTime);
            }
        }, 0, 1000);

        while(DataSingleton.getInstance().getCurrentRound() <= DataSingleton.getInstance().getRounds()){
            DataSingleton.getInstance().setWordHasBeenGuessed(false);
            DataSingleton.getInstance().setWordToGuess(DataSingleton.getInstance().getGuessWords()[random.nextInt(DataSingleton.getInstance().getGuessWords().length)]);

            // Reset the timer
            this.timeIsOver = false;
            this.currentTime = this.turnTimer;

            // Check for new players who could have joined
            for(ServerClient player : DataSingleton.getInstance().getClients()){
                synchronized (this.players) {
                    if (!this.players.contains(player))
                        this.players.add(player);
                }
                if(!this.playersWhoGuessed.containsKey(player))
                    this.playersWhoGuessed.put(player, false);
            }
            nextTurn();

            // Loop while a turn is played and wait for time to end or the word to be guessed by everyone.
            while(!DataSingleton.getInstance().wordHasBeenGuessed() && !this.timeIsOver){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void nextTurn() {
        int tag;
        for(ServerClient player : this.players){
            if(!player.hasDrawn()) {
                tag = player.getTag();
                this.server.sendToAllClients('\u0002' + ","  + tag + "," + DataSingleton.getInstance().getWordToGuess());
                player.setHasDrawn(true);
                // Resetting the guesses.
                for(ServerClient playerGuess : playersWhoGuessed.keySet()){
                    playersWhoGuessed.put(playerGuess, false);
                }
                return;
            }
        }
        nextRound();
    }

    private void nextRound(){
        if(DataSingleton.getInstance().getCurrentRound() + 1 <= DataSingleton.getInstance().getRounds()) {
            DataSingleton.getInstance().setCurrentRound(DataSingleton.getInstance().getCurrentRound() + 1);
            int tag = this.players.get(0).getTag();
            this.server.sendToAllClients('\u0002' + "," + tag + "," + DataSingleton.getInstance().getWordToGuess() + "," + DataSingleton.getInstance().getCurrentRound());
            // Resetting the guesses and drawn booleans
            for(ServerClient playerGuess : playersWhoGuessed.keySet()){
                playerGuess.setHasDrawn(false);
                playersWhoGuessed.put(playerGuess, false);
            }
        }
    }

    public void checkGuess(String received, ServerClient player) {
        if(received.toLowerCase().equals(DataSingleton.getInstance().getWordToGuess())){
            this.server.sendToAllClients("<" + player.getName() + "> : Guessed the word!");
            player.writeUTF("\u0003");
            playersWhoGuessed.put(player, true);
            endTurn();
        }
    }

    private void endTurn(){
        for (Boolean guessed : this.playersWhoGuessed.values()){
            if(guessed.equals(false))
                return;
        }
        DataSingleton.getInstance().setWordHasBeenGuessed(true);
        endRound();
    }

    private void endRound(){
        for (ServerClient player : this.players){
            if(!player.hasDrawn())
                return;
        }
        DataSingleton.getInstance().setWordHasBeenGuessed(true);
    }
}
