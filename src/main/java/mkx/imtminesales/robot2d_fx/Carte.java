package mkx.imtminesales.robot2d_fx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mkx.imtminesales.robot2d.core.GestionnaireJeu;
import mkx.imtminesales.robot2d.physics.CollisionManager;

/**
 * Classe permettant de dessiner la carte.
 *
 * @author Quent
 */
public class Carte {

    private GestionnaireJeu gestionnaire;
    private final int LARGEUR;
    private final int HAUTEUR;
    private final List<Obstacle> obstacles; // Liste des obstacles
    private static final int TAILLE_OBSTACLE = 20; // Taille des obstacles (carrés)

    public Carte(GestionnaireJeu gestionnaire) {
        this.gestionnaire = gestionnaire;
        this.LARGEUR = gestionnaire.largeur;
        this.HAUTEUR = gestionnaire.hauteur;

        this.obstacles = new ArrayList<>();
        genererContour(); // Générer les obstacles du contour
        genererObstacles(); // Générer les obstacles au démarrage
    }

    private void genererObstacles() {
        Random random = new Random();

        // Ajouter des obstacles aléatoires
        int nombreObstacles = 10 + obstacles.size(); // Nombre d'obstacles aléatoires
        int nbEssais = 0;
        while (obstacles.size() < nombreObstacles) {
            int x = random.nextInt(LARGEUR - TAILLE_OBSTACLE);
            int y = random.nextInt(HAUTEUR - TAILLE_OBSTACLE);
            Obstacle obstacle = new Obstacle(x, y, TAILLE_OBSTACLE, TAILLE_OBSTACLE, Color.DARKBLUE);
            if (!CollisionManager.collisionObstacleAvecObstacles(obstacle, obstacles)) {
                obstacles.add(obstacle);
            }
            nbEssais++;
            if (nbEssais > 1000) {
                System.out.println("Impossible de générer un obstacle sans collision !");
                break;
            }
        }
    }

    private void genererContour() {
        // Ajouter des obstacles autour du contour de l'écran
        for (int x = 0; x < LARGEUR; x += TAILLE_OBSTACLE) {
            obstacles.add(new Obstacle(x, 0, TAILLE_OBSTACLE, TAILLE_OBSTACLE, Color.DARKRED)); // Bord haut
            obstacles.add(new Obstacle(x, HAUTEUR - TAILLE_OBSTACLE, TAILLE_OBSTACLE, TAILLE_OBSTACLE, Color.DARKRED)); // Bord bas
        }
        for (int y = 0; y < HAUTEUR; y += TAILLE_OBSTACLE) {
            obstacles.add(new Obstacle(0, y, TAILLE_OBSTACLE, TAILLE_OBSTACLE, Color.DARKRED)); // Bord gauche
            obstacles.add(new Obstacle(LARGEUR - TAILLE_OBSTACLE, y, TAILLE_OBSTACLE, TAILLE_OBSTACLE, Color.DARKRED)); // Bord droit
        }
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public void dessinerCarte(GraphicsContext gc) {
        // Effacer le canvas
        gc.clearRect(0, 0, LARGEUR, HAUTEUR);

        // Dessiner le fond gris clair
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, LARGEUR, HAUTEUR);

        // Dessiner le quadrillage
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(0.5); // Épaisseur des lignes du quadrillage
        int gridSize = 40; // Taille du quadrillage (40 pixels)

        // Dessiner les lignes verticales
        for (int x = 0; x <= LARGEUR; x += gridSize) {
            gc.strokeLine(x, 0, x, HAUTEUR);
        }

        // Dessiner les lignes horizontales
        for (int y = 0; y <= HAUTEUR; y += gridSize) {
            gc.strokeLine(0, y, LARGEUR, y);
        }

        // Dessiner les obstacles
        for (Obstacle obstacle : obstacles) {
            Color couleur = obstacle.getCouleur();
            gc.setFill(couleur);
            gc.fillRect(obstacle.getX(), obstacle.getY(), obstacle.getLargeur(), obstacle.getHauteur());
        }
    }
}
