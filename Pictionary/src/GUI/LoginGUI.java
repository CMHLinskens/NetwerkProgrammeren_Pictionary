package GUI;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginGUI extends Application{

        @Override
        public void start(Stage stage) {
            BorderPane mainPane = new BorderPane();

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

            mainPane.setCenter(details);

            Scene scene = new Scene(mainPane);
            
            stage.setScene(scene);
            stage.setTitle("Pictionary");
            stage.show();
    }
}
