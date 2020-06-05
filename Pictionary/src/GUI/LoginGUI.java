package GUI;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import network.client.Client;

public class LoginGUI extends Application{

        @Override
        public void start(Stage stage) {
            VBox mainPane = new VBox();
            mainPane.setSpacing(25);
            mainPane.setAlignment(Pos.CENTER);

            Label nickNameLabel = new Label("Nickname:");
            Label hostNameLabel = new Label("Host name:");
            Label portLabel = new Label("Port:");
            VBox labels = new VBox();
            labels.getChildren().addAll(nickNameLabel, hostNameLabel, portLabel);
            labels.setSpacing(20);

            TextField nickNameTextField = new TextField();
            TextField hostNameTextField = new TextField();
            TextField portTextField = new TextField();
            VBox textFields = new VBox();
            textFields.getChildren().addAll(nickNameTextField, hostNameTextField, portTextField);
            textFields.setSpacing(10);

            HBox details = new HBox();
            details.getChildren().addAll(labels, textFields);
            details.setSpacing(25);
            details.setAlignment(Pos.CENTER);

            Button joinButton = new Button("Join");
            joinButton.setOnAction(e -> {

            });
            Button hostButton = new Button("Host");
            hostButton.setOnAction(e -> {

            });

            HBox buttonBox = new HBox();
            buttonBox.getChildren().addAll(joinButton, hostButton);
            buttonBox.setSpacing(20);
            buttonBox.setAlignment(Pos.CENTER);

            mainPane.getChildren().addAll(details, buttonBox);

            Scene scene = new Scene(mainPane);

            stage.setScene(scene);
            stage.setTitle("Pictionary");
            stage.show();
    }
}
