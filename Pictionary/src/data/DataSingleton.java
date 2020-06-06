package data;

import network.client.Client;

import java.util.Observable;

public final class DataSingleton extends Observable {

    private static DataSingleton INSTANCE;
    private Client client;
    private DrawData drawData;
    private boolean isDrawing;

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
        setChanged();
        notifyObservers();
    }
    public boolean isDrawing() {
        return isDrawing;
    }
    public void setDrawing(boolean drawing) {
        isDrawing = drawing;
    }
}