package mkx.imtminesales.robot2d.physics;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import mkx.imtminesales.robot2d.core.GestionnaireJeu;
import mkx.imtminesales.robot2d_fx.Obstacle;

public class RayTracing {

    private GestionnaireJeu gestionnaireJeu;
    private double origineX, origineY; // Origine des rayons
    private List<Rayon> rayons; // Liste des rayons
    private List<double[]> directions; // Directions des rayons
    private double maxDistance; // Distance maximale des rayons
    private List<Obstacle> obstacles; // Liste des obstacles à vérifier

    /**
     * Représente un rayon avec son origine, sa direction et sa distance calculée.
     */
    public static class Rayon {
        public double origineX, origineY;
        public double directionX, directionY;
        public double distance;

        public Rayon(double origineX, double origineY, double directionX, double directionY, double distance) {
            this.origineX = origineX;
            this.origineY = origineY;
            this.directionX = directionX;
            this.directionY = directionY;
            this.distance = distance;
        }
    }

    /**
     * Constructeur de RayTracing.
     *
     * @param origineX     Coordonnée X de l'origine des rayons.
     * @param origineY     Coordonnée Y de l'origine des rayons.
     * @param maxDistance  Distance maximale des rayons.
     * @param obstacles    Liste des obstacles à vérifier.
     */
    public RayTracing(GestionnaireJeu gestionnaireJeu, double origineX, double origineY, double maxDistance) {
        this.gestionnaireJeu = gestionnaireJeu;
        this.obstacles = gestionnaireJeu.getCarte().getObstacles();
        this.origineX = origineX;
        this.origineY = origineY;
        this.maxDistance = maxDistance;
        this.obstacles = obstacles;
        this.rayons = new ArrayList<>();
        this.directions = new ArrayList<>();
    }

    /**
     * Retourne la liste des rayons.
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
     * Met à jour les rayons en recalculant leurs distances.
     */
    public void mettreAJour() {
        this.origineX = gestionnaireJeu.getRobot().getPosition().getX();
        this.origineY = gestionnaireJeu.getRobot().getPosition().getY();
        this.obstacles = gestionnaireJeu.getCarte().getObstacles();
        rayons.clear();
        for (double[] direction : directions) {
            double directionX = direction[0];
            double directionY = direction[1];
            double distance = lancerRayon(this.origineX, this.origineY, directionX, directionY, this.obstacles, maxDistance);
            rayons.add(new Rayon(origineX, origineY, directionX, directionY, distance));
        }
    }

    /**
     * Dessine les rayons sur un canvas.
     *
     * @param gc Contexte graphique pour dessiner.
     */
    public void dessiner(GraphicsContext gc) {
        gc.setStroke(Color.RED);
        gc.setLineWidth(1);

        for (Rayon rayon : rayons) {
            double finX = rayon.origineX + rayon.directionX * rayon.distance;
            double finY = rayon.origineY + rayon.directionY * rayon.distance;

            // Dessiner le rayon
            gc.strokeLine(rayon.origineX, rayon.origineY, finX, finY);
        }
    }

    /**
     * Lance un rayon depuis une origine dans une direction donnée et retourne la distance
     * jusqu'au premier obstacle rencontré.
     */
    private static double lancerRayon(double origineX, double origineY, double directionX, double directionY, List<Obstacle> obstacles, double maxDistance) {
        double distance = maxDistance; // Distance maximale du rayon
        double rayonNorme = Math.sqrt(directionX * directionX + directionY * directionY);

        // Normaliser la direction
        double dirX = directionX / rayonNorme;
        double dirY = directionY / rayonNorme;

        // Parcourir tous les obstacles pour trouver la première intersection
        for (Obstacle obstacle : obstacles) {
            double intersection = calculerIntersectionRayonObstacle(origineX, origineY, dirX, dirY, obstacle);
            if (intersection >= 0 && intersection < distance) {
                distance = intersection; // Mettre à jour la distance si une intersection plus proche est trouvée
            }
        }

        return distance; // Retourner la distance jusqu'au premier obstacle
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
}