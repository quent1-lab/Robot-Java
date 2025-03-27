package mkx.imtminesales.robot2d.physics;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import mkx.imtminesales.robot2d.core.Balle;
import mkx.imtminesales.robot2d.core.GestionnaireJeu;
import mkx.imtminesales.robot2d_fx.Obstacle;
import mkx.imtminesales.robot2d_fx.Panier;

public class RayTracing {

    private GestionnaireJeu gestionnaireJeu;
    private double origineX, origineY; // Origine des rayons
    private List<Rayon> rayons; // Liste des rayons
    private List<double[]> directions; // Directions des rayons
    private double maxDistance; // Distance maximale des rayons
    private List<Obstacle> obstacles; // Liste des obstacles à vérifier
    private List<Balle> balles; // Liste des balles à vérifier
    private Panier panier; // Liste des paniers à vérifier

    /**
     * Représente un rayon avec son origine, sa direction, sa distance calculée,
     * le type d'objet touché et la couleur à utiliser pour le dessin.
     */
    public static class Rayon {

        public double origineX, origineY;
        public double directionX, directionY;
        public double distance;
        public String typeObjet; // Type d'objet touché (Obstacle, Balle, Panier)
        public Color couleur; // Couleur du rayon

        public Rayon(double origineX, double origineY, double directionX, double directionY, double distance, String typeObjet, Color couleur) {
            this.origineX = origineX;
            this.origineY = origineY;
            this.directionX = directionX;
            this.directionY = directionY;
            this.distance = distance;
            this.typeObjet = typeObjet;
            this.couleur = couleur;
        }
    }

    /**
     * Constructeur de RayTracing.
     *
     * @param origineX Coordonnée X de l'origine des rayons.
     * @param origineY Coordonnée Y de l'origine des rayons.
     * @param maxDistance Distance maximale des rayons.
     * @param obstacles Liste des obstacles à vérifier.
     */
    public RayTracing(GestionnaireJeu gestionnaireJeu, double origineX, double origineY, double maxDistance) {
        this.gestionnaireJeu = gestionnaireJeu;
        this.obstacles = gestionnaireJeu.getCarte().getObstacles();
        this.balles = gestionnaireJeu.getBalles();
        this.panier = gestionnaireJeu.getPanier();
        this.origineX = origineX;
        this.origineY = origineY;
        this.maxDistance = maxDistance;
        this.rayons = new ArrayList<>();
        this.directions = new ArrayList<>();
    }

    /**
     * Retourne la liste des rayons.
     *
     * @return Liste des rayons.
     */
    public List<Rayon> getRayons() {
        return rayons;
    }

    /**
     * Ajoute une direction pour un rayon.
     *
     * @param directionX Direction X du rayon.
     * @param directionY Direction Y du rayon.
     */
    public void ajouterDirection(double directionX, double directionY) {
        this.directions.add(new double[]{directionX, directionY});
    }

    /**
     * Met à jour les rayons en recalculant leurs distances et les objets
     * touchés.
     */
    public void mettreAJour() {
        this.origineX = gestionnaireJeu.getRobot().getPosition().getX();
        this.origineY = gestionnaireJeu.getRobot().getPosition().getY();
        this.obstacles = gestionnaireJeu.getCarte().getObstacles();
        this.balles = gestionnaireJeu.getBalles();
        this.panier = gestionnaireJeu.getPanier();
        rayons.clear();

        for (double[] direction : directions) {
            double directionX = direction[0];
            double directionY = direction[1];

            // Lancer le rayon et récupérer les informations sur l'objet touché
            Rayon rayon = lancerRayon(this.origineX, this.origineY, directionX, directionY, maxDistance);
            rayons.add(rayon);
        }
    }

    /**
     * Dessine les rayons sur un canvas.
     *
     * @param gc Contexte graphique pour dessiner.
     */
    public void dessiner(GraphicsContext gc) {
        for (Rayon rayon : rayons) {
            gc.setStroke(rayon.couleur);
            gc.setLineWidth(1);

            // Normaliser la direction du rayon
            double norme = Math.sqrt(rayon.directionX * rayon.directionX + rayon.directionY * rayon.directionY);
            double dirX = rayon.directionX / norme;
            double dirY = rayon.directionY / norme;

            // Calculer la position finale du rayon
            double finX = rayon.origineX + dirX * rayon.distance;
            double finY = rayon.origineY + dirY * rayon.distance;

            // Dessiner le rayon
            gc.strokeLine(rayon.origineX, rayon.origineY, finX, finY);
        }
    }

    /**
     * Lance un rayon depuis une origine dans une direction donnée et retourne
     * un objet Rayon contenant les informations sur l'objet touché.
     */
    private Rayon lancerRayon(double origineX, double origineY, double directionX, double directionY, double maxDistance) {
        double distance = maxDistance;
        String typeObjet = "Aucun";
        Color couleur = Color.GRAY;

        double rayonNorme = Math.sqrt(directionX * directionX + directionY * directionY);
        double dirX = directionX / rayonNorme;
        double dirY = directionY / rayonNorme;

        // Vérifier les intersections avec les obstacles
        for (Obstacle obstacle : obstacles) {
            double intersection = calculerIntersectionRayonObstacle(origineX, origineY, dirX, dirY, obstacle);
            if (intersection >= 0 && intersection < distance) {
                distance = intersection;
                typeObjet = "Obstacle";
                couleur = Color.RED;
            }
        }

        // Vérifier les intersections avec les balles
        for (Balle balle : balles) {
            double intersection = calculerIntersectionRayonBalle(origineX, origineY, dirX, dirY, balle);
            if (intersection >= 0 && intersection < distance) {
                distance = intersection;
                typeObjet = "Balle";
                couleur = Color.BLUE;
            }
        }

        // Vérifier les intersections avec le panier
        double intersectionPanier = calculerIntersectionRayonPanier(origineX, origineY, dirX, dirY, panier);
        if (intersectionPanier >= 0 && intersectionPanier < distance) {
            distance = intersectionPanier;
            typeObjet = "Panier";
            couleur = Color.ORANGE;
        }

        return new Rayon(origineX, origineY, directionX, directionY, distance, typeObjet, couleur);
    }

    /**
     * Calcule l'intersection entre un rayon et un obstacle rectangulaire.
     */
    private static double calculerIntersectionRayonObstacle(double origineX, double origineY, double dirX, double dirY, Obstacle obstacle) {
        double xMin = obstacle.getX();
        double xMax = obstacle.getX() + obstacle.getLargeur();
        double yMin = obstacle.getY();
        double yMax = obstacle.getY() + obstacle.getHauteur();

        double tMin = (xMin - origineX) / dirX;
        double tMax = (xMax - origineX) / dirX;

        if (tMin > tMax) {
            double temp = tMin;
            tMin = tMax;
            tMax = temp;
        }

        double tMinY = (yMin - origineY) / dirY;
        double tMaxY = (yMax - origineY) / dirY;

        if (tMinY > tMaxY) {
            double temp = tMinY;
            tMinY = tMaxY;
            tMaxY = temp;
        }

        if (tMin > tMaxY || tMinY > tMax) {
            return -1; // Pas d'intersection
        }

        tMin = Math.max(tMin, tMinY);
        tMax = Math.min(tMax, tMaxY);

        if (tMin < 0) {
            return tMax; // Intersection derrière l'origine
        }

        return tMin; // Intersection devant l'origine
    }

    /**
     * Calcule l'intersection entre un rayon et une balle.
     */
    private static double calculerIntersectionRayonBalle(double origineX, double origineY, double dirX, double dirY, Balle balle) {
        // Ajuster la direction pour corriger l'angle
        double adjustedDirX = -dirX; // Rotation de π
        double adjustedDirY = -dirY;

        double dx = balle.getPosition().getX() - origineX;
        double dy = balle.getPosition().getY() - origineY;
        double rayon = balle.getLargeur() / 2.0;

        double a = adjustedDirX * adjustedDirX + adjustedDirY * adjustedDirY;
        double b = 2 * (adjustedDirX * dx + adjustedDirY * dy);
        double c = dx * dx + dy * dy - rayon * rayon;

        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) {
            return -1; // Pas d'intersection
        }

        double t1 = (-b - Math.sqrt(discriminant)) / (2 * a);
        double t2 = (-b + Math.sqrt(discriminant)) / (2 * a);

        if (t1 >= 0) {
            return t1;
        }
        if (t2 >= 0) {
            return t2;
        }
        return -1; // Pas d'intersection
    }

    /**
     * Calcule l'intersection entre un rayon et le panier.
     */
    private static double calculerIntersectionRayonPanier(double origineX, double origineY, double dirX, double dirY, Panier panier) {
        // Ajuster la direction pour corriger l'angle
        double adjustedDirX = -dirX;
        double adjustedDirY = -dirY;

        double dx = panier.getPosition().getX() + panier.getLargeur() / 2.0 - origineX;
        double dy = panier.getPosition().getY() + panier.getHauteur() / 2.0 - origineY;
        double rayon = panier.getLargeur() / 2.0;

        double a = adjustedDirX * adjustedDirX + adjustedDirY * adjustedDirY;
        double b = 2 * (adjustedDirX * dx + adjustedDirY * dy);
        double c = dx * dx + dy * dy - rayon * rayon;

        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) {
            return -1; // Pas d'intersection
        }

        double t1 = (-b - Math.sqrt(discriminant)) / (2 * a);
        double t2 = (-b + Math.sqrt(discriminant)) / (2 * a);

        if (t1 >= 0) {
            return t1;
        }
        if (t2 >= 0) {
            return t2;
        }
        return -1; // Pas d'intersection
    }
}
