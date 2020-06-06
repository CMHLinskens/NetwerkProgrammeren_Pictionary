package network.client;

import network.server.Server;

public class ServerHost implements Runnable {
    private final int port;

    public ServerHost(int port){
        this.port = port;
    }
    @Override
    public void run() {
        Server server = new Server(port);
        server.connect();
    }
}
