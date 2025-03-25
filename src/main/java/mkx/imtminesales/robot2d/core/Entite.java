package mkx.imtminesales.robot2d.core;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class Entite {

    protected Position2D position;
    protected double vitesseX; // Vitesse sur l'axe X
    protected double vitesseY; // Vitesse sur l'axe Y
    protected double masse; // Masse de l'entité
    protected double coefficientRebond; // Coefficient de rebond (entre 0 et 1)
    protected double frottement; // Coefficient de frottement (entre 0 et 1)
    protected int largeur; // Largeur de l'entité
    protected int hauteur; // Hauteur de l'entité
    protected Color couleur; // Couleur de l'entité

    public Entite(int x, int y, int largeur, int hauteur, double masse, Color couleur) {
        this.position = new Position2D(x, y);
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.masse = masse;
        this.couleur = couleur;
        this.vitesseX = 0;
        this.vitesseY = 0;
        this.coefficientRebond = 0.8; // Par défaut, un rebond modéré
        this.frottement = 0.98; // Par défaut, un léger frottement
    }

    // Getter et setter pour la position
    public Position2D getPosition() {
        return position;
    }

    public void setPosition(Position2D position) {
        this.position = position;
    }

    public int getLargeur() {
        return largeur;
    }

    public int getHauteur() {
        return hauteur;
    }

    // Getter et setter pour la vitesse
    public double getVitesseX() {
        return vitesseX;
    }

    public void setVitesseX(double vitesseX) {
        this.vitesseX = vitesseX;
    }

    public double getVitesseY() {
        return vitesseY;
    }

    public void setVitesseY(double vitesseY) {
        this.vitesseY = vitesseY;
    }

    // Méthode pour appliquer une force à l'entité
    public void appliquerForce(double forceX, double forceY) {
        this.vitesseX += forceX / masse;
        this.vitesseY += forceY / masse;
    }

    // Méthode pour mettre à jour la position en fonction de la vitesse
    public void mettreAJourPosition() {
        // Mettre à jour la position en fonction de la vitesse
        position.deplacer(vitesseX, vitesseY);

        // Appliquer le frottement pour réduire la vitesse progressivement
        vitesseX *= frottement;
        vitesseY *= frottement;
    }

    // Méthode pour gérer les collisions avec les bords de la carte
    public void gererCollisionBords(int largeurCarte, int hauteurCarte) {
        // Collision avec le bord gauche ou droit
        if (position.getX() < 0 || position.getX() + largeur > largeurCarte) {
            vitesseX = -vitesseX * coefficientRebond; // Inverser la vitesse avec un rebond
            position.setX(Math.max(0, Math.min(position.getX(), largeurCarte - largeur)));
        }

        // Collision avec le bord haut ou bas
        if (position.getY() < 0 || position.getY() + hauteur > hauteurCarte) {
            vitesseY = -vitesseY * coefficientRebond; // Inverser la vitesse avec un rebond
            position.setY(Math.max(0, Math.min(position.getY(), hauteurCarte - hauteur)));
        }
    }

    // Méthode abstraite pour dessiner l'entité (à implémenter dans les sous-classes)
    public abstract void dessiner(GraphicsContext gc);
}
