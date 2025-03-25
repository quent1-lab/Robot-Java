package mkx.imtminesales.robot2d.core;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Classe permettant de gérer une balle. Hérite de la classe Entite pour
 * bénéficier des fonctionnalités de base.
 *
 */
public class Balle extends Entite {

    private boolean estAttrapee; // Indique si la balle est attrapée par le robot

    public Balle(int x, int y) {
        super(x, y, 20, 20, 5, Color.GREEN); // Taille 20x20, masse 5, couleur verte par défaut
        this.estAttrapee = false;

        this.masse = 15;
        this.coefficientRebond = 0.8;
        this.frottement = 0.99;
    }

    public boolean estAttrapee() {
        return estAttrapee;
    }

    public void attraper() {
        estAttrapee = true;
        vitesseX = 0; // Réinitialiser la vitesse lorsqu'elle est attrapée
        vitesseY = 0;
    }

    public void attraper(double vitesseX, double vitesseY) {
        estAttrapee = true;
        this.vitesseX = vitesseX; // Appliquer la vitesse du robot
        this.vitesseY = vitesseY;
    }

    public void relacher() {
        estAttrapee = false;
    }

    @Override
    public void mettreAJourPosition() {
        // Appliquer les règles de physique uniquement si la balle n'est pas attrapée
        super.mettreAJourPosition();

    }

    @Override
    public void dessiner(GraphicsContext gc) {
        // Dessiner la balle rouge si elle est attrapée, sinon verte
        if (estAttrapee) {
            gc.setFill(Color.RED);
        } else {
            gc.setFill(couleur);
        }
        gc.fillOval(position.getX() - largeur / 2, position.getY() - hauteur / 2, largeur, hauteur);
    }
}
