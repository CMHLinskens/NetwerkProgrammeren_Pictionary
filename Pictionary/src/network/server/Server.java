package network.server;

import data.DataSingleton;
import game.Game;

import javax.xml.crypto.Data;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    private final int port;
    private ServerSocket serverSocket;
    private ArrayList<ServerClient> clients = new ArrayList<>();
    private ArrayList<Thread> clientThreads = new ArrayList<>();
    private boolean isRunning;
    private int playerTagCounter = 0;
    private Game game;

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

                if(this.clients.size() >= 2) {
                    this.game = new Game(this, 3);
                    game.run();
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
}
