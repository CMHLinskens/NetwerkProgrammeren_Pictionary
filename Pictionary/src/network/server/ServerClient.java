package network.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerClient implements Runnable{
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;
    private Server server;
    private boolean isConnected;

    public ServerClient(Socket socket, String name, Server server) {
        this.socket = socket;
        this.name = name;
        this.server = server;

        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        isConnected = true;
    }

    public void writeUTF(String text){
        try {
            this.out.writeUTF(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(isConnected){
            try {
                String received = this.in.readUTF();
                this.server.sendToAllClients("Received from: " + this.name + ": " + received);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
