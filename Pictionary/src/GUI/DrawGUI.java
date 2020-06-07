package GUI;

import data.DataSingleton;
import data.DrawData;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jfree.fx.FXGraphics2D;

import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Observer;

import static javafx.collections.FXCollections.observableArrayList;

public class DrawGUI {
    private Canvas drawCanvas;
    private Canvas guessCanvas;
    private Label[] playerLabels = new Label[6];
    private BorderPane mainPane;
    private TextArea textAreaChat;
    private TextField textFieldChatInput;
    private Color selectedColor = Color.black;
    private int selectedWidth = 20;
    private ObservableList<String> playerList;

    public void start() {
        SimpleBooleanProperty isNextTurnProperty = DataSingleton.getInstance().getTurnSwitchIndicator();
        SimpleBooleanProperty isDrawingProperty = DataSingleton.getInstance().isDrawing();
        playerList = DataSingleton.getInstance().getPlayers();


         DataSingleton.getInstance().getPlayers().addListener((ListChangeListener<String>) c -> {
             Platform.runLater(new Runnable() {
                 @Override
                 public void run() {

                     int i = 0;
                     for (String player : playerList) {
                         playerLabels[i].setText(player);
                         System.out.println(player + " : " + i);
                         i++;
                     }
                 }
             });
        });

        System.out.println(playerList.toString());
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

        Thread timerThread = new Thread( () -> {
            int lastCurrentTime = DataSingleton.getInstance().getCurrentTime();

            while(true){
                if(DataSingleton.getInstance().getCurrentTime() != lastCurrentTime){
                    lastCurrentTime = DataSingleton.getInstance().getCurrentTime();
                    drawTimer(guessG2d, DataSingleton.getInstance().getCurrentTime());
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        timerThread.start();

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
            if (newValue)
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


        VBox playerVBox = new VBox();
        playerVBox.setAlignment(Pos.TOP_LEFT);
        for (int i = 0; i < playerLabels.length; i++) {
            playerLabels[i] = new Label();
            playerVBox.getChildren().add(playerLabels[i]);
        }
        leftVBox.getChildren().add(playerVBox);

        HBox hBox = new HBox();
        hBox.setMinHeight(445);
        leftVBox.getChildren().add(hBox);

        HBox colorBox = new HBox();
        Button blackButton = new Button("Black");
        blackButton.setOnAction(e -> {selectedColor = Color.black;});
        Button greyButton = new Button("Grey");
        greyButton.setOnAction(e -> {selectedColor = Color.gray;});
        Button whiteButton = new Button("Eraser");
        whiteButton.setOnAction(e -> {selectedColor = Color.white;});
        colorBox.getChildren().addAll(blackButton, greyButton, whiteButton);

        HBox colorBox2 = new HBox();
        Button redButton = new Button("Red");
        redButton.setOnAction(e -> {selectedColor = Color.red;});
        Button greenButton = new Button("Green");
        greenButton.setOnAction(e -> {selectedColor = Color.green;});
        Button blueButton = new Button("Blue");
        blueButton.setOnAction(e -> {selectedColor = Color.blue;});
        colorBox2.getChildren().addAll(redButton, greenButton, blueButton);

        HBox sizeBox = new HBox();
        Button smallButton = new Button("Small");
        smallButton.setOnAction(e -> {selectedWidth = 10;});
        Button mediumButton = new Button("Medium");
        mediumButton.setOnAction(e -> {selectedWidth = 20;});
        Button bigButton = new Button("Big");
        bigButton.setOnAction(e -> {selectedWidth = 32;});
        sizeBox.getChildren().addAll(smallButton, mediumButton, bigButton);


        leftVBox.getChildren().addAll(colorBox, colorBox2, sizeBox);
        leftVBox.setAlignment(Pos.BOTTOM_LEFT);
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
                drawG2d.setColor(selectedColor);
                drawG2d.fill(new Ellipse2D.Double(e.getX(), e.getY(), selectedWidth, selectedWidth));
                DataSingleton.getInstance().setDrawData(new DrawData((int) e.getX(), (int) e.getY(), selectedWidth, selectedColor));
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

    public void drawTimer(FXGraphics2D guessG2d, int currentTime){
        guessG2d.clearRect(0, 0, 200, 55);
        Font font = new Font("Comic Sans MS", Font.BOLD, 50);
        guessG2d.setFont(font);

        guessG2d.drawString(String.valueOf(currentTime), 100, 50);
    }
}
