package network.client;

import GUI.DrawGUI;
import GUI.LoginGUI;
import data.DataSingleton;

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

            Thread exitThread = new Thread( () -> {
                while(true) {
                    if (!isConnected) {
                        disconnect(readSocketThread, writeSocketThread);
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

    private void disconnect(Thread readThread, Thread writeThread) {
        try {
            socket.close();
            readThread.join();
            writeThread.join();
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
                System.out.println(received);
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

    public boolean hostSession(String nickname, int port){
        ServerHost serverHost = new ServerHost(port);
        Thread hostingThread = new Thread(serverHost);
        hostingThread.start();
        return clientSetup(nickname, port);
    }
}
