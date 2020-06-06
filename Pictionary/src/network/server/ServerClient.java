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
    private Thread thread;
    private boolean isConnected;

    public Thread getThread() { return this.thread; }
    public void setThread(Thread thread) { this.thread = thread; }
    public String getName() {
        return name;
    }

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
    public synchronized void run() {
        while(isConnected){
            try {
                String received = this.in.readUTF();
                if(received.equals("\\quit")){
                    isConnected = false;
                    this.server.removeClient(this);
                } else {
                    if(received.substring(0, 2).equals('\u0001' + ",")){
                        this.server.sendToAllClients(received);
                    } else {
                        this.server.sendToAllClients("<" + this.name + "> : " + received);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
