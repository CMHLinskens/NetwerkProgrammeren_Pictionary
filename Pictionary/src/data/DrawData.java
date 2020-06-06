package data;

import javafx.scene.paint.Color;

public class DrawData {
    private int xPos, yPos;
    private int size;
    private Color color;

    public DrawData(int xPos, int yPos, int size, Color color) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.size = size;
        this.color = color;
    }

    // Getters
    public int getxPos() {
        return xPos;
    }
    public int getyPos() {
        return yPos;
    }
    public int getSize() {
        return size;
    }
    public Color getColor() {
        return color;
    }
}
