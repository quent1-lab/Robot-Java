package mkx.imtminesales.robot2d.core;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import mkx.imtminesales.robot2d.physics.CollisionManager;
import mkx.imtminesales.robot2d_fx.Carte;
import mkx.imtminesales.robot2d_fx.Panier;

public class GestionnaireJeu {

    private final Robot robot;
    private final List<Balle> balles;
    private final Panier panier;
    private final Carte carte;

    private final CollisionManager collisionMng;

    public final int largeur;
    public final int hauteur;

    public GestionnaireJeu(int largeurCarte, int hauteurCarte) {
        this.largeur = largeurCarte;
        this.hauteur = hauteurCarte;

        // Initialiser la carte
        this.carte = new Carte(this);

        // Initialiser le robot au centre de la carte
        this.robot = new Robot(this, largeurCarte / 2, hauteurCarte / 2);

        // Initialiser les balles à des positions aléatoires
        this.balles = new ArrayList<>();
        this.apparitionBalle(5);

        // Initialiser le panier
        this.panier = new Panier(largeurCarte, hauteurCarte);
        this.panier.apparitionPanier(carte.getObstacles());

        // Initialiser le gestionnaire de collisions
        this.collisionMng = new CollisionManager(this);
    }

    private void apparitionBalle(int nombre) {
        // Ajouter une balle à une position aléatoire et vérifier les collisions avec les obstacles et les autres balles
        int nbEssais = 0;
        while (balles.size() < nombre) {
            Balle balle = new Balle(this,
                    (int) (Math.random() * largeur),
                    (int) (Math.random() * hauteur)
            );
            if (!CollisionManager.collisionBalleAvecObstacle(balle, carte.getObstacles())
                    && !CollisionManager.collisionBalleAvecBalles(balles)) {
                balles.add(balle);
            }
            nbEssais++;
            if (nbEssais > 1000) {
                System.out.println("Impossible de générer une balle sans collision !");
                break;
            }
        }
    }

    public Robot getRobot() {
        return robot;
    }

    public List<Balle> getBalles() {
        return balles;
    }

    public Carte getCarte() {
        return carte;
    }

    public Panier getPanier() {
        return panier;
    }

    public CollisionManager getCollisionManager() {
        return collisionMng;
    }

    public void gererDeplacementRobot(int dx, int dy) {
        robot.appliquerForce(dx, dy);
        //collisionRobotObstacles();
    }

    public void actionBalle() {
        if (!robot.attraperBalle(balles)) {
            Balle balleLacher = robot.lacherBalle();
        }
    }

    public void supprimerBalle(Balle balle) {
        balles.remove(balle);
    }

    public void mettreAJour(double dt) {
        // Mettre à jour les déplcement du robot et des balles
        robot.mettreAJourPosition(dt);

        for (Balle balle : balles) {
            balle.mettreAJourPosition(dt);
        }
        
        collisionMng.mettreAJourCollisions(dt);
    }
    
    public void dessiner(GraphicsContext gc) {
        // Dessiner la carte
        carte.dessinerCarte(gc);

        // Dessiner le panier
        panier.dessinerPanier(gc);

        // Dessiner le robot
        robot.dessiner(gc);

        // Dessiner les balles
        for (Balle balle : balles) {
            balle.dessiner(gc);
        }

        // Afficher le score
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Arial", 16));
        gc.fillText("Score : " + robot.getScore(), 10, 17);
    }
}
