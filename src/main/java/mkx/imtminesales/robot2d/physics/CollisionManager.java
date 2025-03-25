package mkx.imtminesales.robot2d.physics;

import java.util.List;

import mkx.imtminesales.robot2d.core.Balle;
import mkx.imtminesales.robot2d.core.Robot;
import mkx.imtminesales.robot2d_fx.Obstacle;
import mkx.imtminesales.robot2d_fx.Panier;

public class CollisionManager {

    public static boolean collisionAvecObstacle(Robot robot, List<Obstacle> obstacles) {
        for (Obstacle obstacle : obstacles) {
            if (collisionRectangles(
                    robot.getPosition().getX() - robot.getTaille() / 2,
                    robot.getPosition().getY() - robot.getTaille() / 2,
                    robot.getTaille(),
                    robot.getTaille(),
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
                    balle.getPosition().getX() - balle.getTaille() / 2,
                    balle.getPosition().getY() - balle.getTaille() / 2,
                    balle.getTaille(),
                    balle.getTaille(),
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
                balle.getTaille() / 2,
                robot.getPosition().getX(),
                robot.getPosition().getY(),
                robot.getTaille() / 2);
    }

    public static boolean collisionBalleAvecBalles(List<Balle> balles) {
        for (int i = 0; i < balles.size(); i++) {
            for (int j = i + 1; j < balles.size(); j++) {
                if (collisionCercles(
                        balles.get(i).getPosition().getX(),
                        balles.get(i).getPosition().getY(),
                        balles.get(i).getTaille() / 2,
                        balles.get(j).getPosition().getX(),
                        balles.get(j).getPosition().getY(),
                        balles.get(j).getTaille() / 2)) {
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
        return balle.getPosition().getX() - balle.getTaille() / 2 >= panier.getPosition().getX() &&
               balle.getPosition().getX() + balle.getTaille() / 2 <= panier.getPosition().getX() + panier.getLargeur() &&
               balle.getPosition().getY() - balle.getTaille() / 2 >= panier.getPosition().getY() &&
               balle.getPosition().getY() + balle.getTaille() / 2 <= panier.getPosition().getY() + panier.getHauteur();
    }

    private static boolean collisionRectangles(int x1, int y1, int largeur1, int hauteur1,
                                               int x2, int y2, int largeur2, int hauteur2) {
        return x1 < x2 + largeur2 &&
               x1 + largeur1 > x2 &&
               y1 < y2 + hauteur2 &&
               y1 + hauteur1 > y2;
    }

    private static boolean collisionCercles(int x1, int y1, int rayon1, int x2, int y2, int rayon2) {
        int dx = x1 - x2;
        int dy = y1 - y2;
        int distanceCarree = dx * dx + dy * dy;
        int rayonTotal = rayon1 + rayon2;
        return distanceCarree <= rayonTotal * rayonTotal;
    }
}