package mkx.imtminesales.robot2d.core;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import mkx.imtminesales.robot2d.physics.CollisionManager;

/**
 * Classe permettant de gérer un robot. Hérite de la classe Entite pour
 * bénéficier des fonctionnalités de base.
 *
 */
public class Robot extends Entite {

    private List<Balle> balles; // Liste des balles transportées par le robot
    private int capaciteMax; // Capacité maximale de transport
    private int capaciteActuelle; // Nombre actuel de balles transportées
    private int score; // Score du robot

    public Robot(int x, int y) {
        super(x, y, 40, 40, 10, Color.BLUE); // Taille 40x40, masse 10, couleur bleue
        this.balles = new ArrayList<>();
        this.capaciteMax = 3;
        this.capaciteActuelle = 0;
        this.score = 0;

        this.masse = 10;
        this.coefficientRebond = 0.8;
        this.frottement = 0.95;
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

    public void deplacerBalles() {
        // Déplacer les balles transportées par le robot
        for (Balle balle : balles) {
            balle.setVitesseX(this.getVitesseX());
            balle.setVitesseY(this.getVitesseY());
        }
    }

    public boolean attraperBalle(List<Balle> balles) {
        // Vérifier si le robot est en collision avec une balle
        for (Balle balle : balles) {
            if (CollisionManager.collisionBalleAvecRobot(balle, this)) {
                if (this.capaciteActuelle < this.capaciteMax && !balle.estAttrapee()) {
                    this.capaciteActuelle++;
                    this.balles.add(balle);
                    balle.attraper(this.getVitesseX(), this.getVitesseY());
                    return true;
                }
            }
        }
        return false;
    }

    public Balle lacherBalle() {
        // Lâcher la première balle attrapée
        if (!this.balles.isEmpty()) {
            Balle balle = this.balles.remove(0);
            balle.relacher();
            capaciteActuelle--;
            return balle;
        }
        return null;
    }

    public Balle lacherBalle(Balle balle) {
        // Lâcher une balle spécifique
        if (!this.balles.isEmpty()) {
            balle.relacher();
            capaciteActuelle--;
            this.balles.remove(balle);
            return balle;
        }
        return null;
    }

    public void dessiner(GraphicsContext gc) {
        // Dessiner le robot sous forme de carré
        gc.setFill(couleur);
        gc.fillRect(position.getX() - largeur / 2, position.getY() - hauteur / 2, largeur, hauteur);

        // Ecrire la vitesse du robot au dessus de lui
        gc.setFill(Color.BLACK);
        gc.fillText(String.format("Vx: %.2f Vy: %.2f", vitesseX, vitesseY), position.getX() - largeur / 2, position.getY() - hauteur / 2);
    }
}
