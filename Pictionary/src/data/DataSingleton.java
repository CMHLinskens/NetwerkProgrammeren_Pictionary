package data;

import network.client.Client;

public final class DataSingleton {

    private static DataSingleton INSTANCE;
    private Client client;
    private DrawData drawData;

    private DataSingleton() {
    }

    public synchronized static DataSingleton getInstance() {
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
    public DrawData getDrawData() {
        return drawData;
    }
    public void setDrawData(DrawData drawData) {
        this.drawData = drawData;
    }
}