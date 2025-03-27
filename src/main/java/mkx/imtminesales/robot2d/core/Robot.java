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

    public Robot(GestionnaireJeu gestionnaireJeu, int x, int y) {
        super(gestionnaireJeu, x, y, 40, 40, 10, Color.BLUE, "rectangle"); // Taille 40x40, masse 10, couleur bleue
        this.balles = new ArrayList<>();
        this.capaciteMax = 3;
        this.capaciteActuelle = 0;
        this.score = 0;

        this.coefficientRebond = 0.6;
        this.frottement = 0.9;
        this.amplificationVitesse = 4;
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
            balle.vitesseX = this.vitesseX;
            balle.vitesseY = this.vitesseY;
        }
    }

    public boolean attraperBalle(List<Balle> balles) {
        // Vérifier si le robot est en collision avec une balle
        for (Balle balle : balles) {
            if (CollisionManager.collisionBalleAvecRobot(balle, this)) {
                if (this.capaciteActuelle < this.capaciteMax && !balle.estAttrapee()) {
                    this.capaciteActuelle++;
                    this.balles.add(balle);
                    balle.attraper(this.vitesseX, this.vitesseY);
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

    public void lancerBalle() {
        // Lancer la première balle attrapée
        if (!this.balles.isEmpty()) {
            Balle balle = this.balles.remove(0);
            balle.lancer();
            capaciteActuelle--;
        }
    }

    public void dessiner(GraphicsContext gc) {
        // Dessiner rayon de détection
        rayTracing.dessiner(gc);

        // Dessiner une ombre pour le robot
        gc.setFill(Color.DARKGRAY);
        //gc.fillRect(position.getX() - largeur / 2 + 3, position.getY() - hauteur / 2 + 3, largeur, hauteur);

        // Dessiner le corps principal du robot (carré)
        gc.setFill(couleur);
        gc.fillRect(position.getX() - largeur / 2, position.getY() - hauteur / 2, largeur, hauteur);

        // Dessiner une bordure autour du robot
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(position.getX() - largeur / 2, position.getY() - hauteur / 2, largeur, hauteur);

        // Ajouter un motif interne ressemblant à une cible
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(1);

        // Dessiner un cercle au centre du robot
        gc.strokeOval(position.getX() - largeur / 4, position.getY() - hauteur / 4, largeur / 2, hauteur / 2);

        // Dessiner deux traits croisés (horizontal et vertical)
        gc.strokeLine(position.getX() - largeur / 2 + 2, position.getY(),
                position.getX() + largeur / 2 - 2, position.getY());
        gc.strokeLine(position.getX(), position.getY() - hauteur / 2 + 2,
                position.getX(), position.getY() + hauteur / 2 - 2);

        // Ajouter un effet visuel pour indiquer la vitesse
        if (Math.abs(vitesseX) > 0.1 || Math.abs(vitesseY) > 0.1) {
            gc.setStroke(Color.BROWN);
            gc.setLineWidth(2);
            gc.strokeLine(position.getX(), position.getY(),
                    position.getX() + vitesseX * 5, position.getY() + vitesseY * 5);
        }

        // Écrire la vitesse du robot au-dessus de lui
        gc.setFill(Color.BLACK);
        String vitesse = String.format("Vx: %.2f Vy: %.2f", vitesseX, vitesseY);
        double tailleVitesse = gc.getFont().getSize() / 2.5 * vitesse.length() / 2;
        gc.fillText(vitesse, position.getX() - tailleVitesse, position.getY() - hauteur / 2 - 5);
    }
}
