package GUI;

import data.DataSingleton;
import data.DrawData;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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


        SimpleBooleanProperty isNextTurnProperty = DataSingleton.getInstance().getTurnSwitchIndicator();
        SimpleBooleanProperty isDrawingProperty = DataSingleton.getInstance().isDrawing();

        Stage stage = new Stage();
        mainPane = getNewMainPane();

        FXGraphics2D drawG2d = new FXGraphics2D(drawCanvas.getGraphicsContext2D());
        clearDrawCanvas(drawG2d);
        FXGraphics2D guessG2d = new FXGraphics2D(guessCanvas.getGraphicsContext2D());
        clearGuessCanvas(guessG2d);

        isNextTurnProperty.addListener((observable, oldValue, newValue) -> {
            if (oldValue != newValue) {
                DataSingleton.getInstance().setDrawData(new DrawData(1, 1, 0, Color.white));
                clearDrawCanvas(drawG2d);
                clearGuessCanvas(guessG2d);
                drawGuessableWord(guessG2d, DataSingleton.getInstance().getWordToGuess());
                System.out.println("Cleared Canvas");
            }
        });

        applyPaintableMouse(drawG2d);

        mainPane.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ENTER)) {
                if (textFieldChatInput.getText().length() > 0) {
                    DataSingleton.getInstance().setSendMessage(textFieldChatInput.getText());
                    textFieldChatInput.setText("");
                }
            }
        });

        InvalidationListener listener = o -> textAreaChat.setPrefHeight(mainPane.getHeight() - 95);

        mainPane.widthProperty().addListener(listener);
        mainPane.heightProperty().addListener(listener);

        Observer observer = (o, arg) -> {
            if (!DataSingleton.getInstance().isDrawing().getValue()) {
                DrawData drawData = DataSingleton.getInstance().getDrawData();
                drawG2d.setColor(drawData.getColor());
                drawG2d.fill(new Ellipse2D.Double(drawData.getxPos(), drawData.getyPos(), drawData.getSize(), drawData.getSize()));
            }
        };

        isDrawingProperty.addListener((observable, oldValue, newValue) -> {
            System.out.println("isDrawingPorperty: " + newValue);
            if(newValue)
                drawGuessedWord(guessG2d, DataSingleton.getInstance().getWordToGuess());
            else
                drawGuessableWord(guessG2d, DataSingleton.getInstance().getWordToGuess());
        });
        DataSingleton.getInstance().addObserver(observer);

        Thread messageThread = new Thread(() -> {
            String lastMessage = "";
            while (true) {
                if (!DataSingleton.getInstance().getMessage().equals(lastMessage)) {
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

        drawCanvas = new Canvas(800, 620);
        borderPane.setCenter(drawCanvas);

        guessCanvas = new Canvas(1150, 70);
        borderPane.setTop(guessCanvas);
        return borderPane;
    }

    public void clearDrawCanvas(FXGraphics2D g2d) {
        g2d.setBackground(Color.BLACK);
        g2d.clearRect(0, 0, (int) drawCanvas.getWidth(), (int) drawCanvas.getHeight());
        g2d.setColor(Color.WHITE);
        g2d.fill(new Rectangle2D.Double(5, 0, drawCanvas.getWidth() - 10, drawCanvas.getHeight()));
        g2d.setColor(Color.BLACK);
    }

    public void clearGuessCanvas(FXGraphics2D g2d) {
        g2d.setBackground(Color.white);
        g2d.clearRect(0, 0, (int) guessCanvas.getWidth(), (int) guessCanvas.getHeight());
        g2d.setPaint(Color.BLACK);
        g2d.fill(new Rectangle2D.Double(0, guessCanvas.getHeight() - 5, guessCanvas.getWidth(), guessCanvas.getHeight()));
    }

    public void applyPaintableMouse(FXGraphics2D drawG2d) {
        drawCanvas.setOnMouseDragged(e -> {
            if (DataSingleton.getInstance().isDrawing().get()) {
                drawG2d.fill(new Ellipse2D.Double(e.getX(), e.getY(), 10, 10));
                DataSingleton.getInstance().setDrawData(new DrawData((int) e.getX(), (int) e.getY(), 10, Color.black));
            }
        });
    }

    public void drawGuessedWord(FXGraphics2D guessG2d, String guessWord) {
        Font font = new Font("Comic Sans MS", Font.BOLD, 50);
        guessG2d.setFont(font);
        for (int i = 0; i < guessWord.length(); i++) {
            int xPos = (1150 / 2) - guessWord.length() * 20 + i * 40;
            guessG2d.drawString(String.valueOf(guessWord.charAt(i)), xPos, 35);
        }
    }

    public void drawGuessableWord(FXGraphics2D guessG2d, String guessWord) {
        Font font = new Font("Comic Sans MS", Font.BOLD, 50);
        guessG2d.setFont(font);

        for (int i = 0; i < guessWord.length(); i++) {
            int xPos = (1150 / 2) - guessWord.length() * 20 + i * 40;
            guessG2d.drawString("_", xPos, 50);
        }
    }
}
