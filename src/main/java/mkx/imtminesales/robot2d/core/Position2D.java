package mkx.imtminesales.robot2d.core;

/**
 * Classe permettant de gérer une position 2D.
 *
 * @author Quent
 */
public class Position2D {

    private double x;
    private double y;
    private double lastX;
    private double lastY;

    public Position2D(double x, double y) {
        this.x = x;
        this.y = y;
        this.lastX = x;
        this.lastY = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getLastX() {
        return lastX;
    }

    public double getLastY() {
        return lastY;
    }

    public void deplacer(double dx, double dy) {
        lastX = x;
        lastY = y;

        x += dx;
        y += dy;
    }

    public void retourEnArriere() {
        x = lastX;
        y = lastY;
    }

    public double calculerDistance(Position2D autre) {
        double dx = x - autre.x;
        double dy = y - autre.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
