package network.server;

import java.io.DataInputStream;
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

                ServerClient serverClient = new ServerClient(socket, nickName, this);
                Thread thread = new Thread(serverClient);
                thread.start();
                serverClient.setThread(thread);
                this.clientThreads.add(thread);
                synchronized (this.clients) {
                    this.clients.add(serverClient);
                }

                System.out.println("Connected clients: " + this.clients.size());

                for(ServerClient c : this.clients){
                    c.writeUTF("Client connected via address: " + socket.getInetAddress().getHostName());
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
}
