package mkx.imtminesales.robot2d_fx;

import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import mkx.imtminesales.robot2d.core.Balle;
import mkx.imtminesales.robot2d.core.Position2D;
import mkx.imtminesales.robot2d.physics.CollisionManager;

public class Panier {

    private int largeurCarte;
    private int hauteurCarte;

    private Position2D position;
    private static final int LARGEUR_PANIER = 50;
    private static final int HAUTEUR_PANIER = 50;
    private Color couleur;

    public Panier(int largeurCarte, int hauteurCarte) {
        this.largeurCarte = largeurCarte;
        this.hauteurCarte = hauteurCarte;
        this.couleur = Color.ORANGE; // Couleur par défaut du panier
    }

    public void apparitionPanier(List<Obstacle> obstacles) {
        // Générer une position aléatoire pour le panier
        int x = (int) (Math.random() * (largeurCarte - LARGEUR_PANIER));
        int y = (int) (Math.random() * (hauteurCarte - HAUTEUR_PANIER));
        this.position = new Position2D(x, y);
        // Vérifier les collisions avec les obstacles
        while (CollisionManager.collisionPanierAvecObstacles(this, obstacles)) {
            x = (int) (Math.random() * (largeurCarte - LARGEUR_PANIER));
            y = (int) (Math.random() * (hauteurCarte - HAUTEUR_PANIER));
            this.position = new Position2D(x, y);
        }
    }

    public Position2D getPosition() {
        return position;
    }

    public int getLargeur() {
        return LARGEUR_PANIER;
    }

    public int getHauteur() {
        return HAUTEUR_PANIER;
    }

    public void dessinerPanier(GraphicsContext gc) {
        // Dessiner le cercle principal du panier
        gc.setFill(couleur);
        gc.fillOval(position.getX(), position.getY(), LARGEUR_PANIER, HAUTEUR_PANIER);

        // Dessiner le contour du panier
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeOval(position.getX(), position.getY(), LARGEUR_PANIER, HAUTEUR_PANIER);

        // Ajouter des lignes pour simuler un filet
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);

        double centerX = position.getX() + LARGEUR_PANIER / 2.0;
        double centerY = position.getY() + HAUTEUR_PANIER / 2.0;
        double radiusX = LARGEUR_PANIER / 2.0 - 1;
        double radiusY = HAUTEUR_PANIER / 2.0 - 1;

        // Lignes verticales (limitées au cercle)
        for (int i = 1; i < 5; i++) {
            double x = position.getX() + i * (LARGEUR_PANIER / 5.0);
            double y1 = centerY - Math.sqrt(Math.pow(radiusY, 2) - Math.pow(x - centerX, 2)); // Haut du cercle
            double y2 = centerY + Math.sqrt(Math.pow(radiusY, 2) - Math.pow(x - centerX, 2)); // Bas du cercle
            gc.strokeLine(x, y1, x, y2);
        }

        // Lignes horizontales (limitées au cercle)
        for (int i = 1; i < 5; i++) {
            double y = position.getY() + i * (HAUTEUR_PANIER / 5.0);
            double x1 = centerX - Math.sqrt(Math.pow(radiusX, 2) - Math.pow(y - centerY, 2)); // Gauche du cercle
            double x2 = centerX + Math.sqrt(Math.pow(radiusX, 2) - Math.pow(y - centerY, 2)); // Droite du cercle
            gc.strokeLine(x1, y, x2, y);
        }
    }

    public boolean verifierDepotBalles(List<Balle> balles) {
        balles.removeIf(balle -> {
            return (CollisionManager.collisionBalleAvecPanier(balle, this));
        });
        return false;
    }

    public boolean verifierDepotBalle(Balle balle) {
        return CollisionManager.collisionBalleAvecPanier(balle, this);
    }
}
