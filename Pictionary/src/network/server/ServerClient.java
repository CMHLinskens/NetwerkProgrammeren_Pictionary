package network.server;

import data.DataSingleton;

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
    private Thread thread;
    private boolean isConnected;
    private boolean hasDrawn;
    private final int tag;

    public Thread getThread() { return this.thread; }
    public void setThread(Thread thread) { this.thread = thread; }
    public String getName() {
        return name;
    }
    public boolean hasDrawn() {
        return hasDrawn;
    }
    public void setHasDrawn(boolean hasDrawn) {
        this.hasDrawn = hasDrawn;
    }
    public int getTag() {
        return tag;
    }

    public ServerClient(Socket socket, String name, Server server, int tag) {
        this.socket = socket;
        this.name = name;
        this.server = server;
        this.hasDrawn = false;
        this.tag = tag;

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
                if(received.equals("\\quit")){
                    isConnected = false;
                    this.server.removeClient(this);
                } else {
                    if(received.substring(0, 1).equals("\u0001")){
                        this.server.sendToAllClients(received);
                    } else {
                        this.server.sendToAllClients("<" + this.name + "> : " + received);
                        this.server.checkGuess(received, this);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
