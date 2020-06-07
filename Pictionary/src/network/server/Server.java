package network.server;

import data.DataSingleton;
import data.DrawData;
import game.Game;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class Server {

    private final int port;
    private ServerSocket serverSocket;
    private ArrayList<ServerClient> clients = new ArrayList<>();
    private ArrayList<Thread> clientThreads = new ArrayList<>();
    private boolean isRunning;
    private int playerTagCounter = 0;
    private Game game;
    private Queue<DrawData> drawQueue = new LinkedList<>();

    public static void main(String[] args) {
        System.out.println("Server setting up");
        Server server = new Server(25000);
        server.connect();
    }

    public Server(int port){
        this.port = port;
    }

    public void connect() {
        try {
            this.serverSocket = new ServerSocket(port);

            isRunning = true;

            while(isRunning) {
                System.out.println("Waiting for clients...");
                Socket socket = this.serverSocket.accept();

                System.out.println("Client connected via address: " + socket.getInetAddress().getHostName());

                DataInputStream in = new DataInputStream(socket.getInputStream());
                String nickName = in.readUTF();

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(String.valueOf(playerTagCounter));

                ServerClient serverClient = new ServerClient(socket, nickName, this, playerTagCounter);
                Thread thread = new Thread(serverClient);
                thread.start();
                serverClient.setThread(thread);
                this.clientThreads.add(thread);
                synchronized (this.clients) {
                    this.clients.add(serverClient);
                }

                playerTagCounter++;

                System.out.println("Client connected via address: " + socket.getInetAddress().getHostName());
                System.out.println("Connected clients: " + this.clients.size());

                sendToAllClients("<" + nickName + "> : " + "Connected");

                DataSingleton.getInstance().setClients(this.clients);
                if(this.clients.size() >= 2)
                    sendGameInfo(serverClient);

                synchronized (this.clients) {
                    sendUpdatePlayerList();
                }

                if(this.clients.size() >= 2 && this.game == null) {
                    this.game = new Game(this, 3, 60);
                    Thread gameThread = new Thread(this.game);
                    gameThread.start();
                }
            }
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendToAllClients(String text){
        synchronized (this.clients) {
            for (ServerClient c : this.clients) {
                c.writeUTF(text);
            }
        }
    }

    public void removeClient(ServerClient serverClient) {
        synchronized (this.clients) {
            this.clients.remove(serverClient);
            DataSingleton.getInstance().setClients(this.clients);
            sendUpdatePlayerList();
        }

        Thread t = serverClient.getThread();
        try{
            this.clientThreads.remove(serverClient.getThread());
            System.out.println("Client disconnected.");
            sendToAllClients("<" + serverClient.getName() + "> : " + "Disconnected");
            System.out.println("Connected clients: " + this.clients.size());
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void terminate(){
        this.isRunning = false;
    }

    public void checkGuess(String received, ServerClient player) {
        this.game.checkGuess(received, player);
    }

    public void addToDrawQueue(String drawData){
        Scanner scanner = new Scanner(drawData);
        scanner.useDelimiter(",");
        scanner.next();
        this.drawQueue.add(new DrawData(Integer.parseInt(scanner.next()), Integer.parseInt(scanner.next()), Integer.parseInt(scanner.next()), Color.black));
    }

    public void resetDrawQueue(){
        this.drawQueue.clear();
    }

    private void sendGameInfo(ServerClient client){
        StringBuilder joinString = new StringBuilder("\u0004,");
        joinString.append(DataSingleton.getInstance().getCurrentTimeServer()).append(",");
        for(DrawData dD : drawQueue){
            joinString.append(dD.toString()).append(",");
        }
        joinString.delete(joinString.length(), joinString.length()+1);
        client.writeUTF(joinString.toString());
    }

    private void sendUpdatePlayerList(){
        StringBuilder playerListBuilder = new StringBuilder("\u0005,");
        for(ServerClient client : DataSingleton.getInstance().getClients()){
            playerListBuilder.append(client.getName()).append(",");
        }
        playerListBuilder.delete(playerListBuilder.length(), playerListBuilder.length()+1);
        sendToAllClients(playerListBuilder.toString());
    }
}
