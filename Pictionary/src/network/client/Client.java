package network.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private String hostname;
    private int port;


    public static void main(String[] args){
        Client client = new Client("MSI", 25000);
        client.connect();
    }

    public Client(String hostname, int port){
        this.hostname = hostname;
        this.port = port;
    }

    public void connect(){
        System.out.println("Connecting to server: " + this.hostname + " on port " + this.port);

        Scanner scanner = new Scanner(System.in);
        try {
            Socket socket = new Socket(this.hostname, this.port);

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            String input = "";

            Thread readSocketThread = new Thread( () -> {
                String received = "";
                while(true){
                    try {
                        received = in.readUTF();
                        System.out.println("Client received: " + received);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            readSocketThread.start();

            while(!input.equals("quit")){
                input = scanner.nextLine();
                out.writeUTF(input);
                System.out.println("Sended: " + input);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
