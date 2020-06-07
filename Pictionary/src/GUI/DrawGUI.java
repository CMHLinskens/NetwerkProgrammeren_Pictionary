package GUI;

import data.DataSingleton;
import data.DrawData;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jfree.fx.FXGraphics2D;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Observer;

public class DrawGUI {
    private Canvas drawCanvas;
    private Canvas guessCanvas;
    private BorderPane mainPane;
    private Button buttonVoteKick;
    private TextArea textAreaChat;
    private TextField textFieldChatInput;

    public void start() {

        Stage stage = new Stage();
        mainPane = getNewMainPane();

        FXGraphics2D drawG2d = new FXGraphics2D(drawCanvas.getGraphicsContext2D());
        clearDrawCanvas(drawG2d);
        FXGraphics2D guessG2d = new FXGraphics2D(guessCanvas.getGraphicsContext2D());
        clearGuessCanvas(guessG2d);

        applyPaintableMouse(drawG2d);

        mainPane.setOnKeyPressed(e -> {
            if(e.getCode().equals(KeyCode.ENTER)) {
                if(textFieldChatInput.getText().length() > 0) {
                    DataSingleton.getInstance().setSendMessage(textFieldChatInput.getText());
                    textFieldChatInput.setText("");
                }
            }
        });

        InvalidationListener listener = new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                textAreaChat.setPrefHeight(mainPane.getHeight() - 95);
            }};

        mainPane.widthProperty().addListener(listener);
        mainPane.heightProperty().addListener(listener);

        Observer observer = new Observer() {
            @Override
            public void update(java.util.Observable o, Object arg) {
                if(!DataSingleton.getInstance().isDrawing()) {
                    DrawData drawData = DataSingleton.getInstance().getDrawData();
                    drawG2d.setColor(drawData.getColor());
                    drawG2d.fill(new Ellipse2D.Double(drawData.getxPos(), drawData.getyPos(), drawData.getSize(), drawData.getSize()));
                }
            }
        };
        DataSingleton.getInstance().addObserver(observer);

        Thread messageThread = new Thread( () -> {
            String lastMessage = "";
            while(true){
                if(!DataSingleton.getInstance().getMessage().equals(lastMessage)){
                    lastMessage = DataSingleton.getInstance().getMessage();
                    textAreaChat.setText(textAreaChat.getText() + '\n' + DataSingleton.getInstance().getMessage());
                }
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        messageThread.start();

        stage.setScene(new Scene(mainPane));
        stage.setTitle("Pictionary");
        stage.show();
    }

    private BorderPane getNewMainPane() {

        BorderPane borderPane = new BorderPane();
        VBox leftVBox = new VBox();
        buttonVoteKick = new Button("VoteKick");
        buttonVoteKick.setPrefWidth(150);
        leftVBox.getChildren().add(buttonVoteKick);
        leftVBox.setAlignment(Pos.BOTTOM_LEFT);
        leftVBox.setPrefWidth(150);
        borderPane.setLeft(leftVBox);

        VBox rightVBox = new VBox();
        textAreaChat = new TextArea();
        textAreaChat.setEditable(false);
        textAreaChat.setWrapText(true);
        textFieldChatInput = new TextField();
        textFieldChatInput.setPromptText("Type your guess here...");
        rightVBox.getChildren().addAll(textAreaChat, textFieldChatInput);
        rightVBox.setAlignment(Pos.BOTTOM_RIGHT);
        rightVBox.setPrefHeight(borderPane.getHeight());
        rightVBox.setPrefWidth(200);
        borderPane.setRight(rightVBox);

        drawCanvas = new Canvas(800,620);
        borderPane.setCenter(drawCanvas);

        guessCanvas = new Canvas(1150, 70);
        borderPane.setTop(guessCanvas);
        borderPane.setPrefHeight(500);
        borderPane.setPrefWidth(1000);
        return borderPane;
    }

    public void clearDrawCanvas(FXGraphics2D g2d){
        g2d.setBackground(Color.BLACK);
        g2d.clearRect(0, 0, (int) drawCanvas.getWidth(), (int) drawCanvas.getHeight());
        g2d.setColor(Color.WHITE);
        g2d.fill(new Rectangle2D.Double(5, 0, drawCanvas.getWidth() - 10 , drawCanvas.getHeight()));
        g2d.setColor(Color.BLACK);
    }

    public void clearGuessCanvas(FXGraphics2D g2d){
        g2d.setBackground(Color.white);
        g2d.clearRect(0, 0, (int) guessCanvas.getWidth(), (int) guessCanvas.getHeight());
        g2d.setPaint(Color.BLACK);
        g2d.fill(new Rectangle2D.Double(0,guessCanvas.getHeight() - 5, guessCanvas.getWidth(), guessCanvas.getHeight()));
    }

    public void applyPaintableMouse(FXGraphics2D drawG2d) {
        drawCanvas.setOnMouseDragged(e -> {
            if(DataSingleton.getInstance().isDrawing()) {
                drawG2d.fill(new Ellipse2D.Double(e.getX(), e.getY(), 10, 10));
                DataSingleton.getInstance().setDrawData(new DrawData((int) e.getX(), (int) e.getY(), 10, Color.black));
            }
        });
    }
}
