
import org.jfree.fx.FXGraphics2D;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class DrawCanvas extends Rectangle2D {

    public DrawCanvas(){
        super();
    }

    public void drawOnCanvas(Point2D mouse, FXGraphics2D g2d){
        Ellipse2D newEllipse = new Ellipse2D.Double(mouse.getX(),mouse.getY(),10,10);
        g2d.fill(newEllipse);
    }

    @Override
    public void setRect(double x, double y, double w, double h) {

    }

    @Override
    public int outcode(double x, double y) {
        return 0;
    }

    @Override
    public Rectangle2D createIntersection(Rectangle2D r) {
        return null;
    }

    @Override
    public Rectangle2D createUnion(Rectangle2D r) {
        return null;
    }

    @Override
    public double getX() {
        return 0;
    }

    @Override
    public double getY() {
        return 0;
    }

    @Override
    public double getWidth() {
        return 0;
    }

    @Override
    public double getHeight() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
