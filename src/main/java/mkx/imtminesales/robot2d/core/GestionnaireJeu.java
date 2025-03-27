package mkx.imtminesales.robot2d.core;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import mkx.imtminesales.robot2d.ai.IA;
import mkx.imtminesales.robot2d.physics.CollisionManager;
import mkx.imtminesales.robot2d_fx.Carte;
import mkx.imtminesales.robot2d_fx.Panier;

public class GestionnaireJeu {

    private final Robot robot;
    private final List<Balle> balles;
    private final Panier panier;
    private final Carte carte;
    private final IA ia; // Instance de l'IA

    private final CollisionManager collisionMng;

    public final int largeur;
    public final int hauteur;

    private double tempsEcoule; // Temps écoulé en secondes

    private Thread threadAjoutBalles;
    private boolean jeuEnCours = true; // Indique si le jeu est en cours
    private final int limiteBalles = 100; // Limite maximale de balles sur la carte

    public GestionnaireJeu(int largeurCarte, int hauteurCarte) {
        this.largeur = largeurCarte;
        this.hauteur = hauteurCarte;

        // Initialiser la carte
        this.carte = new Carte(this);

        // Initialiser le robot au centre de la carte
        this.robot = new Robot(this, largeurCarte / 2, hauteurCarte / 2);

        // Initialiser les balles à des positions aléatoires
        this.balles = new ArrayList<>();
        this.apparitionBalle(50);

        // Initialiser le panier
        this.panier = new Panier(largeurCarte, hauteurCarte);
        this.panier.apparitionPanier(carte.getObstacles());

        // Initialiser le gestionnaire de collisions
        this.collisionMng = new CollisionManager(this);

        // Initialiser l'IA
        ia = new IA(this);

        // Démarrer le thread pour ajouter des balles
        demarrerAjoutBalles();
    }

    private void ajouterBalle() {
        int nbEssais = 0;
        while (nbEssais < 10000) { // Limiter le nombre d'essais pour éviter une boucle infinie
            Balle balle = new Balle(this,
                    (int) (Math.random() * (largeur - 10 - 20)),
                    (int) (Math.random() * (hauteur - 10 - 20)));
            if (!CollisionManager.collisionBalleAvecObstacle(balle, carte.getObstacles())
                    && !CollisionManager.collisionBalleAvecBalles(balles)) {
                balles.add(balle);
                System.out.println("Balle ajoutée !");
                break;
            }
            nbEssais++;
        }
    }

    private void apparitionBalle(int nombre) {
        // Ajouter une balle à une position aléatoire et vérifier les collisions avec
        // les obstacles et les autres balles
        int nbEssais = 0;
        while (balles.size() < nombre) {
            ajouterBalle();
            nbEssais++;
            if (nbEssais > 10) {
                System.out.println("Impossible de générer une balle sans collision !");
                break;
            }
        }
    }

    public void demarrerAjoutBalles() {
        threadAjoutBalles = new Thread(() -> {
            while (jeuEnCours) {
                try {
                    Thread.sleep(5000); // Attendre 5 secondes avant d'ajouter une balle
                    synchronized (balles) {
                        if (balles.size() < limiteBalles) {
                            ajouterBalle();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Réagir à l'interruption
                }
            }
        });
        threadAjoutBalles.setDaemon(true); // Le thread s'arrête automatiquement à la fin du programme
        threadAjoutBalles.start();
    }

    public void arreterAjoutBalles() {
        jeuEnCours = false;
        if (threadAjoutBalles != null && threadAjoutBalles.isAlive()) {
            threadAjoutBalles.interrupt(); // Interrompre le thread
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
        // collisionRobotObstacles();
    }

    public void actionBalle() {
        if (!robot.attraperBalle(balles)) {
            robot.lacherBalle();
        }
    }

    public void supprimerBalle(Balle balle) {
        balles.removeIf(b -> b == balle); // Supprime la balle sans provoquer de ConcurrentModificationException
    }

    public void mettreAJour(double dt) {
        // Appel de l'IA pour déterminer les actions du robot
        ia.update();
        
        // Mettre à jour le temps écoulé
        tempsEcoule += dt;

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
        gc.fillText("Balles restantes : " + balles.size(), 80, 17);

        // Afficher le temps écoulé
        int minutes = (int) (tempsEcoule / 60);
        int secondes = (int) (tempsEcoule % 60);
        gc.fillText(String.format("Temps : %02d:%02d", minutes, secondes), 650, 17);

        ia.dessiner(gc); // Dessiner les informations de l'IA
    }

    public void arreterJeu() {
        arreterAjoutBalles();
    }
}
