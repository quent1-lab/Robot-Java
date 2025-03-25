package mkx.imtminesales.robot2d.core;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Classe permettant de gérer une balle.
 *
 * @author Quent
 */
public class Balle {

    public Position2D position;
    private boolean estAttrapee;

    private static final int TAILLE_BALLE = 20;

    public Balle(int x, int y) {
        this.position = new Position2D(x, y);
    }

    public Position2D getPosition() {
        return position;
    }

    public void setPosition(Position2D position) {
        this.position = position;
    }

    public int getTaille() {
        return TAILLE_BALLE;
    }

    public void deplacer(int dx, int dy) {
        position.deplacer(dx, dy);
    }

    public double calculerDistance(Position2D autre) {
        return position.calculerDistance(autre);
    }

    public void dessinerBalle(GraphicsContext gc) {
        // Dessiner la balle rouge si elle est attrapée sinon verte
        if (estAttrapee) {
            gc.setFill(Color.RED);
        } else {
            gc.setFill(Color.GREEN);
        }
        gc.fillOval(position.getX() - TAILLE_BALLE / 2, position.getY() - TAILLE_BALLE / 2, TAILLE_BALLE, TAILLE_BALLE);
    }

    public boolean estAttrapee() {
        return estAttrapee;
    }

    public void attraper() {
        estAttrapee = true;
    }

    public void relacher() {
        estAttrapee = false;
    }

}
