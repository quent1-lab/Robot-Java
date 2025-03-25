package mkx.imtminesales.robot2d.physics;

import java.util.List;

import mkx.imtminesales.robot2d.core.Balle;
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
            robot.getPosition().retourEnArriere();
        }else{
            robot.deplacerBalles();
        }

        // Vérifier les collisions entre balles et obstacles
        for (Balle balle : balles) {
            if (collisionBalleAvecObstacle(balle, obstacles)) {
                balle.getPosition().retourEnArriere();
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

    public static boolean collisionBalleAvecBalles(List<Balle> balles) {
        for (int i = 0; i < balles.size(); i++) {
            for (int j = i + 1; j < balles.size(); j++) {
                if (collisionCercles(
                        balles.get(i).getPosition().getX(),
                        balles.get(i).getPosition().getY(),
                        balles.get(i).getLargeur() / 2,
                        balles.get(j).getPosition().getX(),
                        balles.get(j).getPosition().getY(),
                        balles.get(j).getLargeur() / 2)) {
                    return true;
                }
            }
        }
        return false;
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
}
