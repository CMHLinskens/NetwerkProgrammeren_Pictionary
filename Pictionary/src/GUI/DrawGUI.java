package GUI;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jfree.fx.FXGraphics2D;

import java.awt.geom.Ellipse2D;

public class DrawGUI {
        private Canvas canvas;
        private BorderPane mainPane;
        private Button buttonVoteKick;
        private TextArea textAreaChat;
        private TextField textFieldChatInput;

        public void start() {

            Stage stage = new Stage();
            mainPane = getNewMainPane();

            canvas = new Canvas(800,720);
            mainPane.setCenter(canvas);
            FXGraphics2D g2d = new FXGraphics2D(canvas.getGraphicsContext2D());

            canvas.setOnMouseDragged(e ->
                    g2d.fill(new Ellipse2D.Double(e.getX(), e.getY(), 10, 10)));

            InvalidationListener listener = new InvalidationListener() {
                @Override
                public void invalidated(Observable o) {
                    textAreaChat.setPrefHeight(mainPane.getHeight() - 100);
                }};

            mainPane.widthProperty().addListener(listener);
            mainPane.heightProperty().addListener(listener);

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
            textFieldChatInput = new TextField();
            rightVBox.getChildren().addAll(textAreaChat, textFieldChatInput);
            rightVBox.setAlignment(Pos.BOTTOM_RIGHT);
            rightVBox.setPrefHeight(borderPane.getHeight());
            rightVBox.setPrefWidth(200);
            borderPane.setRight(rightVBox);

            borderPane.setPrefHeight(500);
            borderPane.setPrefWidth(1000);
            return borderPane;
        }
}
