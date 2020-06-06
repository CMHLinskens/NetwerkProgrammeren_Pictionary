package data;

import network.client.Client;

import java.util.ArrayList;

public final class DataSingleton {

    private static DataSingleton INSTANCE;
    private Client client;

    private DataSingleton() {
    }

    public static DataSingleton getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new DataSingleton();
        }

        return INSTANCE;
    }

    // getters and setters
    public Client getClient() {
        return client;
    }
    public void setClient(Client client) {
        this.client = client;
    }
}