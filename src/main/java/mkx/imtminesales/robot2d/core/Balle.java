package mkx.imtminesales.robot2d.core;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Classe permettant de gérer une balle. Hérite de la classe Entite pour
 * bénéficier des fonctionnalités de base.
 *
 */
public class Balle extends Entite {

    private double deltaX_robot; // Décalage en x par rapport au robot
    private double deltaY_robot; // Décalage en y par rapport au robot

    private boolean estAttrapee; // Indique si la balle est attrapée par le robot
    private static final double FROTTEMENT_BALLE = 0.99; // Coefficient de frottement

    public Balle(GestionnaireJeu gestionnaireJeu, int x, int y) {
        super(gestionnaireJeu, x, y, 20, 20, 15, Color.GREEN,"cercle"); // Taille 20x20, masse 5, couleur verte par défaut
        this.estAttrapee = false;

        deltaX_robot = 0;
        deltaY_robot = 0;

        this.coefficientRebond = 0.8;
        this.frottement = FROTTEMENT_BALLE;
    }

    public boolean estAttrapee() {
        return estAttrapee;
    }

    public void setDelta(){
        if(estAttrapee) {
            deltaX_robot = position.getX() - gestionnaireJeu.getRobot().getPosition().getX();
            deltaY_robot = position.getY() - gestionnaireJeu.getRobot().getPosition().getY();
        }
    }

    public void attraper() {
        estAttrapee = true;
        vitesseX = 0; // Réinitialiser la vitesse lorsqu'elle est attrapée
        vitesseY = 0;
        setDelta();
    }

    public void attraper(double vitesseX, double vitesseY) {
        estAttrapee = true;
        this.vitesseX = vitesseX; // Appliquer la vitesse du robot
        this.vitesseY = vitesseY;
        setDelta();
    }

    public void relacher() {
        estAttrapee = false;
    }

    @Override
    public void mettreAJourPosition(double dt) {
        // Appliquer les règles de physique uniquement si la balle n'est pas attrapée
        if (estAttrapee) {
            // Calcule la vitesse par rapport au robot et au décalage pour suivre le robot
            double newX = gestionnaireJeu.getRobot().getPosition().getX() + deltaX_robot;
            double newY = gestionnaireJeu.getRobot().getPosition().getY() + deltaY_robot;

            vitesseX = (newX - position.getX())  / frottement;
            vitesseY = (newY - position.getY()) / frottement;
        }
        super.mettreAJourPosition(dt);
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
