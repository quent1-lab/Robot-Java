package mkx.imtminesales.robot2d.core;

import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import mkx.imtminesales.robot2d.physics.RayTracing;

public abstract class Entite {

    protected GestionnaireJeu gestionnaireJeu;
    protected Position2D position;
    protected RayTracing rayTracing;

    protected double vitesseX; // Vitesse sur l'axe X
    protected double vitesseY; // Vitesse sur l'axe Y
    protected double masse; // Masse de l'entité
    protected double coefficientRebond; // Coefficient de rebond (entre 0 et 1)
    protected double frottement; // Coefficient de frottement (entre 0 et 1)
    protected int largeur; // Largeur de l'entité
    protected int hauteur; // Hauteur de l'entité
    protected Color couleur; // Couleur de l'entité

    public Entite(GestionnaireJeu gestionnaireJeu, int x, int y, int largeur, int hauteur, double masse, Color couleur) {
        this.gestionnaireJeu = gestionnaireJeu;
        this.position = new Position2D(x, y);
        this.rayTracing = new RayTracing(gestionnaireJeu, x, y, 40);

        this.largeur = largeur;
        this.hauteur = hauteur;
        this.masse = masse;
        this.couleur = couleur;
        this.vitesseX = 0;
        this.vitesseY = 0;
        this.coefficientRebond = 0.8; // Par défaut, un rebond modéré
        this.frottement = 0.98; // Par défaut, un léger frottement

        initialisationRayTracing();
    }

    private void initialisationRayTracing() {
        rayTracing.ajouterDirection(1, 0); // Droite
        rayTracing.ajouterDirection(0, 1); // Bas
        rayTracing.ajouterDirection(-1, 0); // Gauche
        rayTracing.ajouterDirection(0, -1); // Haut
        rayTracing.ajouterDirection(1, 1); // Bas-droite
        rayTracing.ajouterDirection(-1, 1); // Bas-gauche
        rayTracing.ajouterDirection(-1, -1); // Haut-gauche
        rayTracing.ajouterDirection(1, -1); // Haut-droite
    }

    // Getter et setter pour la position
    public Position2D getPosition() {
        return this.position;
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

    // Méthode pour appliquer une force à l'entité
    public void appliquerForce(double forceX, double forceY) {
        this.vitesseX += forceX / masse;
        this.vitesseY += forceY / masse;
    }

    public void deplacerAvecRayTracing(double deltaX, double deltaY) {
        // Mettre à jour les rayons pour refléter la position actuelle
        rayTracing.mettreAJour();

        // Vérifier les collisions avec les obstacles pour chaque rayon
        List<RayTracing.Rayon> rayons = rayTracing.getRayons();

        // Variables pour ajuster le déplacement
        boolean bloquerX = false;
        boolean bloquerY = false;

        for (RayTracing.Rayon rayon : rayons) {
            // Vérifier si le rayon est dans la même direction que le déplacement
            boolean directionXValide = (deltaX > 0 && rayon.directionX > 0) || (deltaX < 0 && rayon.directionX < 0);
            boolean directionYValide = (deltaY > 0 && rayon.directionY > 0) || (deltaY < 0 && rayon.directionY < 0);

            // Si un rayon détecte un obstacle dans la direction du déplacement
            if (directionXValide && rayon.distance <= Math.abs(deltaX) + largeur / 2) {
                bloquerX = true; // Bloquer le déplacement sur X
            }
            if (directionYValide && rayon.distance <= Math.abs(deltaY) + hauteur / 2) {
                bloquerY = true; // Bloquer le déplacement sur Y
            }
        }

        // Ajuster le déplacement en fonction des collisions détectées
        if (bloquerX) {
            deltaX = 0;
        }
        if (bloquerY) {
            deltaY = 0;
        }

        // Si un déplacement est encore possible, déplacer l'entité
        position.deplacer(deltaX, deltaY);
    }

    // Méthode pour mettre à jour la position en fonction de la vitesse
    public void mettreAJourPosition(double dt) {
        // Mettre à jour la position en fonction de la vitesse
        //position.deplacer(vitesseX, vitesseY);
        deplacerAvecRayTracing(vitesseX, vitesseY);

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
