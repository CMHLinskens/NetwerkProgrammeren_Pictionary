package network.client;

import GUI.DrawGUI;
import GUI.LoginGUI;
import data.DataSingleton;
import data.DrawData;

import javax.xml.crypto.Data;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

import static javafx.application.Application.launch;

public class Client {

    private String hostname;
    private int port;
    private boolean isConnected = true;
    private Socket socket;
    private DrawData currentDrawData;
    private int tag;

    public static void main(String[] args){
        Client client = new Client();
        DataSingleton.getInstance().setClient(client);
        launch(LoginGUI.class);
    }

    public Client(){
    }

    public boolean clientSetup(String nickname, int port){
        this.hostname = "localhost";
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
        while(isConnected) {
            try {
                received = in.readUTF();
                if(received.substring(0,1).equals("\u0001")){
                    if(!DataSingleton.getInstance().isDrawing()) {
                        Scanner scanner = new Scanner(received);
                        scanner.useDelimiter(",");
                        scanner.next();
                        DataSingleton.getInstance().setDrawData(new DrawData(Integer.parseInt(scanner.next()), Integer.parseInt(scanner.next()), Integer.parseInt(scanner.next()), Color.black));
                    }
                } else if (received.substring(0, 1).equals("\u0002")) {
                    Scanner scanner = new Scanner(received);
                    scanner.useDelimiter(",");
                    scanner.next();
                    int nextPlayer = Integer.parseInt(scanner.next());
                    if(nextPlayer == this.tag)
                        DataSingleton.getInstance().setDrawing(true);
                    else
                        DataSingleton.getInstance().setDrawing(false);

                    DataSingleton.getInstance().setWordToGuess(scanner.next());
                    System.out.println("Draw: " + DataSingleton.getInstance().getWordToGuess());
                    if(scanner.hasNext())
                        DataSingleton.getInstance().setCurrentRound(Integer.parseInt(scanner.next()));
                } else if (received.substring(0, 1).equals("\u0003")) {
                    System.out.println("You guessed correctly!");
                } else {
                    System.out.println(received);
                    DataSingleton.getInstance().setMessage(received);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendDataFromSocket(DataOutputStream out) {
        String input = "";
        while(!input.equals("\\quit")) {
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
        while(isConnected) {
            while (DataSingleton.getInstance().isDrawing()){
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

    public boolean hostSession(String nickname, int port){
        ServerHost serverHost = new ServerHost(port);
        Thread hostingThread = new Thread(serverHost);
        hostingThread.start();
        DataSingleton.getInstance().setDrawing(true);
        return clientSetup(nickname, port);
    }
}
