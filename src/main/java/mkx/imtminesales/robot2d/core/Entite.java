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
    protected double amplificationVitesse; // Amplification de la vitesse
    protected double masse; // Masse de l'entité
    protected double coefficientRebond; // Coefficient de rebond (entre 0 et 1)
    protected double frottement; // Coefficient de frottement (entre 0 et 1)
    protected int largeur; // Largeur de l'entité
    protected int hauteur; // Hauteur de l'entité
    protected Color couleur; // Couleur de l'entité
    protected String forme; // Forme de l'entité

    public Entite(GestionnaireJeu gestionnaireJeu, int x, int y, int largeur, int hauteur, double masse, Color couleur, String forme) {
        this.gestionnaireJeu = gestionnaireJeu;
        this.position = new Position2D(x, y);
        this.rayTracing = new RayTracing(gestionnaireJeu, x, y, 200);

        this.largeur = largeur;
        this.hauteur = hauteur;
        this.masse = masse;
        this.couleur = couleur;
        this.forme = forme;
        this.vitesseX = 0;
        this.vitesseY = 0;
        this.coefficientRebond = 0.8; // Par défaut, un rebond modéré
        this.frottement = 0.98; // Par défaut, un léger frottement
        this.amplificationVitesse = 1;

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
        rayTracing.ajouterDirection(Math.cos(Math.PI / 8), Math.sin(Math.PI / 8)); // Bas-droite (pi/8)
        rayTracing.ajouterDirection(-Math.cos(Math.PI / 8), Math.sin(Math.PI / 8)); // Bas-gauche (7pi/8)
        rayTracing.ajouterDirection(-Math.cos(Math.PI / 8), -Math.sin(Math.PI / 8)); // Haut-gauche (9pi/8)
        rayTracing.ajouterDirection(Math.cos(Math.PI / 8), -Math.sin(Math.PI / 8)); // Haut-droite (15pi/8)
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

    public double getVitesseX() {
        return vitesseX;
    }

    public double getVitesseY() {
        return vitesseY;
    }

    public void setVitesseX(double vitesseX) {
        this.vitesseX = vitesseX;
    }

    public void setVitesseY(double vitesseY) {
        this.vitesseY = vitesseY;
    }

    public String getForme() {
        return forme;
    }

    // Méthode pour appliquer une force à l'entité
    public void appliquerForce(double forceX, double forceY) {
        this.vitesseX += forceX * amplificationVitesse / masse;
        this.vitesseY += forceY * amplificationVitesse / masse;
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

            // Calculer la distance projetée sur la direction du déplacement
            double distanceProjeteX = Math.abs(deltaX) / Math.sqrt(rayon.directionX * rayon.directionX + rayon.directionY * rayon.directionY);
            double distanceProjeteY = Math.abs(deltaY) / Math.sqrt(rayon.directionX * rayon.directionX + rayon.directionY * rayon.directionY);

            // Calculer la taille du rayon pour détecter les collisions en fonction de la largeur et de la hauteur et de la direction du rayon
            double tailleRayonX = Math.abs(rayon.directionX) * largeur / 2;
            double tailleRayonY = Math.abs(rayon.directionY) * hauteur / 2;
            double tailleRayon = Math.sqrt(tailleRayonX * tailleRayonX + tailleRayonY * tailleRayonY);

            // Si un rayon détecte un obstacle dans la direction du déplacement
            if (directionXValide && rayon.distance <= distanceProjeteX + tailleRayon) {
                bloquerX = true; // Bloquer le déplacement sur X
            }
            if (directionYValide && rayon.distance <= distanceProjeteY + tailleRayon) {
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

    public void deplacerCollision(double dt) {
        // Vérifier pour chaque axe individuellement si un déplacement est possible

        // Déplacement sur l'axe X
        position.deplacer(vitesseX, 0);
        if (gestionnaireJeu.getCollisionManager().collisionEntiteAvecObstacle(this, gestionnaireJeu.getCarte().getObstacles())) {
            if (Math.abs(vitesseX) > 0.2) { // Vérifier si la vitesse dépasse une certaine valeur
                vitesseX = -vitesseX * coefficientRebond; // Inverser la vitesse avec un rebond
                position.deplacer(vitesseX, 0);
            } else {
                position.deplacer(-vitesseX, 0);
                vitesseX = 0; // Sinon, arrêter complètement le mouvement sur X
            }
        }

        // Déplacement sur l'axe Y
        position.deplacer(0, vitesseY);
        if (gestionnaireJeu.getCollisionManager().collisionEntiteAvecObstacle(this, gestionnaireJeu.getCarte().getObstacles())) {
            if (Math.abs(vitesseY) > 0.2) { // Vérifier si la vitesse dépasse une certaine valeur
                vitesseY = -vitesseY * coefficientRebond; // Inverser la vitesse avec un rebond
                position.deplacer(0, vitesseY);
            } else {
                position.deplacer(0, -vitesseY);
                vitesseY = 0; // Sinon, arrêter complètement le mouvement sur Y
            }
        }
    }

    // Méthode pour mettre à jour la position en fonction de la vitesse
    public void mettreAJourPosition(double dt) {
        rayTracing.mettreAJour();

        // Mettre à jour la position en fonction de la vitesse
        //position.deplacer(vitesseX, vitesseY);
        //deplacerAvecRayTracing(vitesseX, vitesseY);
        deplacerCollision(dt);

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
