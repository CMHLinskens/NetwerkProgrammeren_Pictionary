package network.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private final int port = 25000;
    private ServerSocket serverSocket;

    public static void main(String[] args) {

        System.out.println("Server setting up");
        Server server = new Server();
        server.connect();
    }

    public void connect() {
        try {
            this.serverSocket = new ServerSocket(port);

            Socket socket = this.serverSocket.accept();

            System.out.println("Client connected via address: " + socket.getInetAddress().getHostName());

            while(true){
                int c = socket.getInputStream().read();
                System.out.println("Received: " + c);
                if(c == '1') { break; }
            }

            socket.close();
            this.serverSocket.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
