package network.client;

import GUI.DrawGUI;
import GUI.LoginGUI;
import data.DataSingleton;
import data.DrawData;
import javafx.collections.ObservableList;

import javax.xml.crypto.Data;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

import static javafx.application.Application.launch;
import static javafx.collections.FXCollections.observableArrayList;

public class Client {

    private String hostname;
    private int port;
    private boolean isConnected = true;
    private Socket socket;
    private DrawData currentDrawData;
    private int tag;
    private Timer timer = new Timer();
    private int turnTime = 60;

    public static void main(String[] args){
        Client client = new Client();
        DataSingleton.getInstance().setClient(client);
        launch(LoginGUI.class);
    }

    public Client(){
    }

    public boolean clientSetup(String nickname, int port, String hostname){
        this.hostname = hostname;
        this.port = port;

        return connect(nickname);
    }

    private boolean connect(String nickName){
        System.out.println("Connecting to server: " + this.hostname + " on port " + this.port);

        try {
            socket = new Socket(this.hostname, this.port);
            if(!socket.isConnected()) { return false; }

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            out.writeUTF(nickName);

            tag = Integer.parseInt(in.readUTF());

            System.out.println("You are now connected as " + nickName);

            DrawGUI drawGUI = new DrawGUI();
            drawGUI.start();

            Thread readSocketThread = new Thread( () -> {
                if(isConnected) {
                    receiveDataFromSocket(in);
                }
            });

            readSocketThread.start();

            Thread writeSocketThread = new Thread( () -> {
                if(isConnected) {
                    sendDataFromSocket(out);
                }
            });

            writeSocketThread.start();
            writeSocketThread.setPriority(6);

            Thread writeDrawDataSocketThread = new Thread( () -> {
                if(isConnected){
                    sendDrawDataFromSocket(out);
                }
            });

            writeDrawDataSocketThread.start();

            Thread exitThread = new Thread( () -> {
                while(true) {
                    if (!isConnected) {
                        disconnect(readSocketThread, writeSocketThread, writeDrawDataSocketThread);
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            exitThread.start();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void startDisconnect(){
        isConnected = false;
    }

    private void disconnect(Thread readThread, Thread writeThread, Thread drawDataThread) {
        try {
            socket.close();
            readThread.join();
            writeThread.join();
            drawDataThread.join();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveDataFromSocket(DataInputStream in){
        String received = "";
        while(!socket.isClosed()) {
            try {
                received = in.readUTF();
                switch (received.substring(0, 1)) {
                    case "\u0001":
                        if (!DataSingleton.getInstance().isDrawing().getValue()) {
                            Scanner scanner = new Scanner(received);
                            scanner.useDelimiter(",");
                            scanner.next();
                            DataSingleton.getInstance().setDrawData(new DrawData(Integer.parseInt(scanner.next()),
                                    Integer.parseInt(scanner.next()),
                                    Integer.parseInt(scanner.next()),
                                    new Color(Integer.parseInt(scanner.next()))));
                        }
                        break;
                    case "\u0002": {
                        Scanner scanner = new Scanner(received);
                        scanner.useDelimiter(",");
                        scanner.next();
                        int nextPlayer = Integer.parseInt(scanner.next());

                        DataSingleton.getInstance().setWordToGuess(scanner.next());
                        System.out.println("Draw: " + DataSingleton.getInstance().getWordToGuess());
                        if (scanner.hasNext())
                            DataSingleton.getInstance().setCurrentRound(Integer.parseInt(scanner.next()));

                        DataSingleton.getInstance().setCurrentTime(this.turnTime);
                        setUpTimer();

                        // Indicate the turn has been switch to another player
                        DataSingleton.getInstance().setTurnSwitchIndicator(!DataSingleton.getInstance().getTurnSwitchIndicator().get());

                        if (nextPlayer == this.tag)
                            DataSingleton.getInstance().setDrawing(true);
                        else
                            DataSingleton.getInstance().setDrawing(false);

                        break;
                    }
                    case "\u0003":
                        System.out.println("You guessed correctly!");
                        break;
                    case "\u0004": {
                        Scanner scanner = new Scanner(received);
                        scanner.useDelimiter(",");
                        scanner.next();

                        DataSingleton.getInstance().setCurrentTime(Integer.parseInt(scanner.next()));
                        setUpTimer();

                        LinkedList<DrawData> drawQueue = new LinkedList<>();
                        while (scanner.hasNext()) {
                            scanner.next();
                            drawQueue.add(new DrawData(Integer.parseInt(scanner.next()),
                                    Integer.parseInt(scanner.next()),
                                    Integer.parseInt(scanner.next()),
                                    new Color(Integer.parseInt(scanner.next()))));
                        }
                        DataSingleton.getInstance().setDrawQueue(drawQueue);

                        break;
                    }
                    case "\u0005": {
                        Scanner scanner = new Scanner(received);
                        scanner.useDelimiter(",");
                        scanner.next();

                        ObservableList<String> players = observableArrayList();

                        while (scanner.hasNext()) {
                            players.add(scanner.next());
                            System.out.println("SCNANERRRRR");
                        }
                        DataSingleton.getInstance().addPlayers(players);

                        break;
                    }
                    default:
                        System.out.println(received);
                        DataSingleton.getInstance().setMessage(received);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendDataFromSocket(DataOutputStream out) {
        String input = "";
        while(!input.equals("\\quit") && !socket.isClosed()) {
            try {
                if(!input.equals(DataSingleton.getInstance().getSendMessage())) {
                    input = DataSingleton.getInstance().getSendMessage();
                    out.writeUTF(input);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        startDisconnect();
    }

    private void sendDrawDataFromSocket(DataOutputStream out){
        while(!socket.isClosed()) {
            while (DataSingleton.getInstance().isDrawing().get()){
                try {
                    if(DataSingleton.getInstance().getDrawData() != currentDrawData) {
                        currentDrawData = DataSingleton.getInstance().getDrawData();
                        out.writeUTF(DataSingleton.getInstance().getDrawData().toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean hostSession(String nickname, int port, String hostname){
        ServerHost serverHost = new ServerHost(port);
        Thread hostingThread = new Thread(serverHost);
        hostingThread.start();
        return clientSetup(nickname, port, hostname);
    }

    private void setUpTimer(){
        this.timer.cancel();
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(DataSingleton.getInstance().getCurrentTime() > 0)
                    DataSingleton.getInstance().setCurrentTime(DataSingleton.getInstance().getCurrentTime() - 1);
            }
        }, 0, 1000);
    }
}
