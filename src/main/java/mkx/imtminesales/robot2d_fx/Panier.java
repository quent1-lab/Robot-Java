package mkx.imtminesales.robot2d_fx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import mkx.imtminesales.robot2d.core.Position2D;
import mkx.imtminesales.robot2d.core.Balle;
import mkx.imtminesales.robot2d.core.Robot;
import mkx.imtminesales.robot2d.physics.CollisionManager;
import mkx.imtminesales.robot2d_fx.Obstacle;

import java.util.List;
import java.util.ArrayList;

import java.util.List;

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
        gc.setFill(couleur);
        gc.fillRect(position.getX(), position.getY(), LARGEUR_PANIER, HAUTEUR_PANIER);
    }

    public boolean verifierDepotBalles(List<Balle> balles) {
        balles.removeIf(balle -> {
            return(CollisionManager.collisionBalleAvecPanier(balle, this));
        });
        return false;
    }

    public boolean verifierDepotBalle(Balle balle) {
        return CollisionManager.collisionBalleAvecPanier(balle, this);
    }
}