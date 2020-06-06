package network.client;

import GUI.DrawGUI;
import GUI.LoginGUI;
import data.DataSingleton;
import data.DrawData;
import javafx.scene.paint.Color;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import static javafx.application.Application.launch;

public class Client {

    private String hostname;
    private int port;
    private boolean isConnected = true;
    private Socket socket;
    private boolean isDrawing = false;

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

        Scanner scanner = new Scanner(System.in);
        try {
            socket = new Socket(this.hostname, this.port);
            if(!socket.isConnected()) { return false; }

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            out.writeUTF(nickName);

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
                    sendDataFromSocket(out, scanner);
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
                if(received.substring(0,2).equals('\u0001' + ",")){
                    if(!isDrawing) {
                        Scanner scanner = new Scanner(received);
                        scanner.useDelimiter(",");
                        scanner.next();
                        DataSingleton.getInstance().setDrawData(new DrawData(Integer.parseInt(scanner.next()), Integer.parseInt(scanner.next()), Integer.parseInt(scanner.next()), Color.BLACK));
                        //System.out.println(newDrawData.toString());
                    }
                } else {
                    System.out.println(received);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendDataFromSocket(DataOutputStream out, Scanner scanner) {
        String input = "";
        while(!input.equals("\\quit")) {
            try {
                input = scanner.nextLine();

                out.writeUTF(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        startDisconnect();
    }

    private void sendDrawDataFromSocket(DataOutputStream out){
        DataSingleton.getInstance().setDrawData(new DrawData(50, 100, 20, Color.BLACK));
        while(isConnected) {
            while (isDrawing){
                try {
                    out.writeUTF(DataSingleton.getInstance().getDrawData().toString());
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
        isDrawing = true;
        return clientSetup(nickname, port);
    }
}
