package mkx.imtminesales.robot2d.physics;

import java.util.List;

import mkx.imtminesales.robot2d.core.Balle;
import mkx.imtminesales.robot2d.core.Entite;
import mkx.imtminesales.robot2d.core.GestionnaireJeu;
import mkx.imtminesales.robot2d.core.Robot;
import mkx.imtminesales.robot2d_fx.Carte;
import mkx.imtminesales.robot2d_fx.Obstacle;
import mkx.imtminesales.robot2d_fx.Panier;

public class CollisionManager {

    private final GestionnaireJeu gestionnaireJeu;

    public CollisionManager(GestionnaireJeu gestionnaireJeu) {
        this.gestionnaireJeu = gestionnaireJeu;
    }

    public void mettreAJourCollisions(double dt) {
        Robot robot = gestionnaireJeu.getRobot();
        List<Balle> balles = gestionnaireJeu.getBalles();
        Carte carte = gestionnaireJeu.getCarte();
        List<Obstacle> obstacles = carte.getObstacles();
        Panier panier = gestionnaireJeu.getPanier();

        // Vérifier les collisions entre robots et obstacles
        if (collisionAvecObstacle(robot, obstacles)) {
            //robot.getPosition().retourEnArriere();
        } else {
            robot.deplacerBalles();
        }

        // Vérifier les collisions entre balles et obstacles
        for (Balle balle : balles) {
            if (collisionBalleAvecObstacle(balle, obstacles)) {
                //balle.getPosition().retourEnArriere();
                balle.setDelta();
            }
        }

        // Vérifier les collisions entre le robot et ses balles
        for (Balle balle : robot.getBalles()) {
            if (!collisionBalleAvecRobot(balle, robot)) {
                robot.lacherBalle(balle);
            }
        }

        // Vérifier les collisions entre balles
        if (collisionBalleAvecBalles(balles)) {

        }
        
        // Vérifier les collisions entre balles et le panier
        for (Balle balle : balles) {
            if (collisionBalleAvecPanier(balle, panier) && !balle.estAttrapee()) {
                gestionnaireJeu.supprimerBalle(balle);
                robot.incrementerScore();
            }
        }
    }

    public static boolean collisionAvecObstacle(Robot robot, List<Obstacle> obstacles) {
        for (Obstacle obstacle : obstacles) {
            if (collisionRectangles(
                    robot.getPosition().getX() - robot.getLargeur() / 2,
                    robot.getPosition().getY() - robot.getHauteur() / 2,
                    robot.getLargeur(),
                    robot.getHauteur(),
                    obstacle.getX(),
                    obstacle.getY(),
                    obstacle.getLargeur(),
                    obstacle.getHauteur())) {
                return true;
            }
        }
        return false;
    }

    public boolean collisionEntiteAvecObstacle(Entite entite, List<Obstacle> obstacles) {
        // Vérifier la forme de l'entité pour les collisions
        if (entite.getForme().equals("rectangle")) {
            for (Obstacle obstacle : obstacles) {
                if (collisionRectangles(
                        entite.getPosition().getX() - entite.getLargeur() / 2,
                        entite.getPosition().getY() - entite.getHauteur() / 2,
                        entite.getLargeur(),
                        entite.getHauteur(),
                        obstacle.getX(),
                        obstacle.getY(),
                        obstacle.getLargeur(),
                        obstacle.getHauteur())) {
                    return true;
                }
            }
        } else if (entite.getForme().equals("cercle")) {
            for (Obstacle obstacle : obstacles) {
                if (collisionCercles(
                        entite.getPosition().getX(),
                        entite.getPosition().getY(),
                        entite.getLargeur() / 2 * 0.9,
                        obstacle.getX() + obstacle.getLargeur() / 2,
                        obstacle.getY() + obstacle.getHauteur() / 2,
                        Math.sqrt(obstacle.getLargeur() * obstacle.getLargeur() + obstacle.getHauteur() * obstacle.getHauteur()) / 2)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Modifier la méthode collisionBalleAvecObstacle pour inclure le rebond
    public static boolean collisionBalleAvecObstacle(Balle balle, List<Obstacle> obstacles) {
        for (Obstacle obstacle : obstacles) {
            if (collisionRectangles(
                    balle.getPosition().getX() - balle.getLargeur() / 2,
                    balle.getPosition().getY() - balle.getLargeur() / 2,
                    balle.getLargeur(),
                    balle.getLargeur(),
                    obstacle.getX(),
                    obstacle.getY(),
                    obstacle.getLargeur(),
                    obstacle.getHauteur())) {

                // // Calculer la normale de collision
                // double normaleX = 0;
                // double normaleY = 0;

                // if (balle.getPosition().getX() < obstacle.getX()) {
                //     normaleX = -1; // Collision à gauche
                // } else if (balle.getPosition().getX() > obstacle.getX() + obstacle.getLargeur()) {
                //     normaleX = 1; // Collision à droite
                // }

                // if (balle.getPosition().getY() < obstacle.getY()) {
                //     normaleY = -1; // Collision en haut
                // } else if (balle.getPosition().getY() > obstacle.getY() + obstacle.getHauteur()) {
                //     normaleY = 1; // Collision en bas
                // }

                // recalculerVitesseApresCollision(balle, normaleX, normaleY);
                return true;
            }
        }
        return false;
    }

    public static boolean collisionBalleAvecRobot(Balle balle, Robot robot) {
        return collisionCercles(
                balle.getPosition().getX(),
                balle.getPosition().getY(),
                balle.getLargeur() / 2,
                robot.getPosition().getX(),
                robot.getPosition().getY(),
                robot.getLargeur() / 2);
    }

    // Modifier la méthode collisionBalleAvecBalles pour inclure un transfert d'énergie réaliste
    public static boolean collisionBalleAvecBalles(List<Balle> balles) {
        boolean collision = false;
        for (int i = 0; i < balles.size(); i++) {
            for (int j = i + 1; j < balles.size(); j++) {
                Balle balle1 = balles.get(i);
                Balle balle2 = balles.get(j);

                if (collisionCercles(
                        balle1.getPosition().getX(),
                        balle1.getPosition().getY(),
                        balle1.getLargeur() / 2,
                        balle2.getPosition().getX(),
                        balle2.getPosition().getY(),
                        balle2.getLargeur() / 2)) {

                    // Calculer la normale de collision
                    double normaleX = balle2.getPosition().getX() - balle1.getPosition().getX();
                    double normaleY = balle2.getPosition().getY() - balle1.getPosition().getY();
                    double magnitude = Math.sqrt(normaleX * normaleX + normaleY * normaleY);
                    normaleX /= magnitude;
                    normaleY /= magnitude;

                    // Calculer le vecteur tangent
                    double tangentX = -normaleY;
                    double tangentY = normaleX;

                    // Projeter les vitesses sur la normale et la tangente
                    double v1n = balle1.getVitesseX() * normaleX + balle1.getVitesseY() * normaleY;
                    double v1t = balle1.getVitesseX() * tangentX + balle1.getVitesseY() * tangentY;
                    double v2n = balle2.getVitesseX() * normaleX + balle2.getVitesseY() * normaleY;
                    double v2t = balle2.getVitesseX() * tangentX + balle2.getVitesseY() * tangentY;

                    // Les vitesses tangentielles restent inchangées
                    double v1tFinal = v1t;
                    double v2tFinal = v2t;

                    // Les vitesses normales sont échangées (collision élastique)
                    double v1nFinal = v2n;
                    double v2nFinal = v1n;

                    // Reconvertir les vitesses normales et tangentielles en coordonnées cartésiennes
                    balle1.setVitesseX(v1nFinal * normaleX + v1tFinal * tangentX);
                    balle1.setVitesseY(v1nFinal * normaleY + v1tFinal * tangentY);
                    balle2.setVitesseX(v2nFinal * normaleX + v2tFinal * tangentX);
                    balle2.setVitesseY(v2nFinal * normaleY + v2tFinal * tangentY);

                    collision = true;
                }
            }
        }
        return collision;
    }

    public static boolean collisionObstacleAvecObstacles(Obstacle obstacle, List<Obstacle> obstacles) {
        for (Obstacle autre : obstacles) {
            if (obstacle != autre && collisionRectangles(
                    obstacle.getX(),
                    obstacle.getY(),
                    obstacle.getLargeur(),
                    obstacle.getHauteur(),
                    autre.getX(),
                    autre.getY(),
                    autre.getLargeur(),
                    autre.getHauteur())) {
                return true;
            }
        }
        return false;
    }

    public static boolean collisionPanierAvecObstacles(Panier panier, List<Obstacle> obstacles) {
        for (Obstacle obstacle : obstacles) {
            if (collisionRectangles(
                    panier.getPosition().getX(),
                    panier.getPosition().getY(),
                    panier.getLargeur(),
                    panier.getHauteur(),
                    obstacle.getX(),
                    obstacle.getY(),
                    obstacle.getLargeur(),
                    obstacle.getHauteur())) {
                return true;
            }
        }
        return false;
    }

    public static boolean collisionBalleAvecPanier(Balle balle, Panier panier) {
        // Vérifier si 50% de la balle est dans le panier
        return balle.getPosition().getX() - balle.getLargeur() / 2 >= panier.getPosition().getX()
                && balle.getPosition().getX() + balle.getLargeur() / 2 <= panier.getPosition().getX() + panier.getLargeur()
                && balle.getPosition().getY() - balle.getLargeur() / 2 >= panier.getPosition().getY()
                && balle.getPosition().getY() + balle.getLargeur() / 2 <= panier.getPosition().getY() + panier.getHauteur();
    }

    private static boolean collisionRectangles(double x1, double y1, double largeur1, double hauteur1,
            double x2, double y2, double largeur2, double hauteur2) {
        return x1 < x2 + largeur2
                && x1 + largeur1 > x2
                && y1 < y2 + hauteur2
                && y1 + hauteur1 > y2;
    }

    private static boolean collisionCercles(double x1, double y1, double rayon1, double x2, double y2, double rayon2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double distanceCarree = dx * dx + dy * dy;
        double rayonTotal = rayon1 + rayon2;
        return distanceCarree <= rayonTotal * rayonTotal;
    }

    // Ajouter une méthode pour recalculer la vélocité après une collision
    private static void recalculerVitesseApresCollision(Balle balle, double normaleX, double normaleY) {
        double dotProduct = balle.getVitesseX() * normaleX + balle.getVitesseY() * normaleY;
        double newVitesseX = balle.getVitesseX() - 2 * dotProduct * normaleX;
        double newVitesseY = balle.getVitesseY() - 2 * dotProduct * normaleY;
        // Saturer la vélocité pour éviter les problèmes de stabilité
        if (Math.abs(newVitesseX) < 0.1) {
            newVitesseX = 0;
        }
        if (Math.abs(newVitesseY) < 0.1) {
            newVitesseY = 0;
        }
        if (Math.abs(newVitesseX) > 3) {
            newVitesseX = newVitesseX / Math.abs(newVitesseX) * 3;
        }
        if (Math.abs(newVitesseY) > 3) {
            newVitesseY = newVitesseY / Math.abs(newVitesseY) * 3;
        }
        balle.setVitesseX(newVitesseX);
        balle.setVitesseY(newVitesseY);
    }
}
