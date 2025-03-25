package mkx.imtminesales.robot2d.core;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import mkx.imtminesales.robot2d.physics.CollisionManager;

/**
 * Classe permettant de gérer un robot.
 * 
 * @author Quent
 */
public class Robot {

    public Position2D position;
    private List<Balle> balles;
    private int capaciteMax;
    private int capaciteActuelle;
    private int score;

    private int lastDX;
    private int lastDY;

    private static final double VITESSE = 3.0;
    private static final int TAILLE_ROBOT = 40;
    private static final Color COULEUR_ROBOT = Color.BLUE;

    public Robot(int x, int y) {
        this.position = new Position2D(x, y);
        this.balles = new ArrayList<>();
        this.capaciteMax = 3;
    }

    public Position2D getPosition() {
        return position;
    }

    public void setPosition(Position2D position) {
        this.position = position;
    }

    public void setXY(int x, int y) {
        this.position.setX(x);
        this.position.setY(y);
    }

    public int getTaille() {
        return TAILLE_ROBOT;
    }

    public List<Balle> getBalles() {
        return balles;
    }

    public int getScore() {
        return this.score;
    }

    public void incrementerScore() {
        this.score++;
    }

    public void deplacer(int dx, int dy) {
        dx = (int) (dx * VITESSE);
        dy = (int) (dy * VITESSE);
        this.lastDX = dx;
        this.lastDY = dy;
        position.deplacer(dx, dy);
    }

    public void deplacerBalles() {
        for (Balle balle : this.balles) {
            balle.deplacer(this.lastDX, this.lastDY);
        }
    }

    public double calculerDistance(Position2D autre) {
        return position.calculerDistance(autre);
    }

    public boolean attraperBalle(List<Balle> balles) {
        // Vérifier si le robot est en collision avec une balle
        for (Balle balle : balles) {
            if (CollisionManager.collisionBalleAvecRobot(balle, this)) {
                if (this.capaciteActuelle < this.capaciteMax && !balle.estAttrapee()) {
                    this.capaciteActuelle++;
                    this.balles.add(balle);
                    balle.attraper();
                    return true;
                }
            }
        }
        return false;
    }

    public Balle lacherBalle() {
        // Lâcher la première balle attrapée
        for (Balle balle : this.balles) {
            balle.relacher();
            capaciteActuelle--;
            this.balles.remove(balle);
            return balle;
        }
        return null;
    }

    public Balle lacherBalle(Balle balle) {
        // Lâcher une balle spécifique
        balle.relacher();
        capaciteActuelle--;
        this.balles.remove(balle);
        return balle;
    }

    public void dessinerRobot(GraphicsContext gc) {
        // Dessiner le robot sous forme de carré
        gc.setFill(COULEUR_ROBOT);
        gc.fillRect(position.getX() - TAILLE_ROBOT / 2, position.getY() - TAILLE_ROBOT / 2, TAILLE_ROBOT, TAILLE_ROBOT);
    }
}
