package mkx.imtminesales.robot2d.core;

/**
 * Classe permettant de gérer une position 2D.
 *
 * @author Quent
 */
public class Position2D {

    private int x;
    private int y;
    private int lastX;
    private int lastY;

    public Position2D(int x, int y) {
        this.x = x;
        this.y = y;
        this.lastX = x;
        this.lastY = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getLastX() {
        return lastX;
    }

    public int getLastY() {
        return lastY;
    }

    public void deplacer(int dx, int dy) {
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
        int dx = x - autre.x;
        int dy = y - autre.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
