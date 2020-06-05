package network.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private String hostname;
    private int port;
    private boolean isConnected = true;


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

            System.out.println("Enter a nickname: ");
            String nickName = scanner.nextLine();
            out.writeUTF(nickName);

            System.out.println("You are now connected as " + nickName);

            String input = "";

            Thread readSocketThread = new Thread( () -> {
                receiveDataFromSocket(in);
            });

            readSocketThread.start();

            while(!input.equals("\\quit")){
                input = scanner.nextLine();
                out.writeUTF(input);
            }
        isConnected = false;

            socket.close();

            try {
                readSocketThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
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
}
