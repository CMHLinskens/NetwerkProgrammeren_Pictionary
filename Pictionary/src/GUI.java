import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.jfree.fx.FXGraphics2D;
import org.jfree.fx.ResizableCanvas;

import java.awt.geom.Point2D;


public class GUI extends Application {
     private ResizableCanvas canvas;

     @Override
     public void start(Stage stage) {

         BorderPane mainPane = new BorderPane();
         canvas = new ResizableCanvas(g -> draw(g), mainPane);
         mainPane.setCenter(canvas);
         FXGraphics2D g2d = new FXGraphics2D(canvas.getGraphicsContext2D());
         DrawCanvas drawCanvas = new DrawCanvas();

         canvas.setOnMouseDragged(e -> onMouseDragged(e, drawCanvas, g2d));

         new AnimationTimer() {
             long last = -1;

             @Override
             public void handle(long now) {
                 if (last == -1)
                     last = now;
                 update((now - last) / 1000000000.0);
                 last = now;
                 draw(g2d);
             }
         }.start();

         stage.setScene(new Scene(mainPane));
         stage.setTitle("Pictionary");
         stage.show();
         draw(g2d);
    }


    public void draw(FXGraphics2D g2d) {

    }


    public void update(double deltaTime) {

    }

    private void onMouseDragged(MouseEvent e, DrawCanvas drawCanvas, FXGraphics2D g2d) {
         drawCanvas.drawOnCanvas(new Point2D.Double(e.getX(), e.getY()), g2d);
    }
}
