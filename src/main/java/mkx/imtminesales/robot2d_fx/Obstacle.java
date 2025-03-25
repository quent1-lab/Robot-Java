package mkx.imtminesales.robot2d_fx;

import javafx.scene.paint.Color;

public class Obstacle {

    private int x;
    private int y;
    private int largeur;
    private int hauteur;
    private Color couleur;

    public Obstacle(int x, int y, int largeur, int hauteur, Color couleur) {
        this.x = x;
        this.y = y;
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.couleur = couleur;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getLargeur() {
        return largeur;
    }

    public int getHauteur() {
        return hauteur;
    }

    public Color getCouleur() {
        return couleur;
    }
}