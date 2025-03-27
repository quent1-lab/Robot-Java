package mkx.imtminesales.robot2d_fx;

import mkx.imtminesales.robot2d.core.GestionnaireJeu;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;

import java.util.HashSet;
import java.util.Set;

/**
 * Classe principale de l'application Java FX.
 *
 * @author Quent
 */
public class App extends Application {

    private Canvas toile;
    private GestionnaireJeu gestionnaireJeu;
    private static final int LARGEUR = 800;
    private static final int HAUTEUR = 600;

    private final Set<KeyCode> touchesAppuyees = new HashSet<>(); // Suivi des touches pressées
    private boolean espaceAppuye = false; // Variable pour suivre l'état de la touche ESPACE
    private boolean eAppuye = false; // Variable pour suivre l'état de la touche E

    @Override
    public void start(Stage fenetre) {
        toile = new Canvas(LARGEUR, HAUTEUR);

        // Initialiser le gestionnaire de jeu
        gestionnaireJeu = new GestionnaireJeu(LARGEUR, HAUTEUR);

        VBox racine = new VBox(toile);
        Scene scene = new Scene(racine);

        // Gestion des touches pressées
        scene.setOnKeyPressed(this::gererToucheAppuyee);
        scene.setOnKeyReleased(this::gererToucheRelachee);

        fenetre.setTitle("Simulation Robot v0.2");
        fenetre.setScene(scene);
        fenetre.show();

        // Utiliser AnimationTimer pour la boucle d'animation
        AnimationTimer animationTimer = new AnimationTimer() {
            private long dernierTemps = 0; // Temps de la dernière itération en nanosecondes

            @Override
            public void handle(long maintenant) {
                if (dernierTemps == 0) {
                    dernierTemps = maintenant; // Initialiser le temps précédent
                    return;
                }

                // Calculer le delta time en secondes
                double deltaTime = (maintenant - dernierTemps) / 1_000_000_000.0;
                dernierTemps = maintenant;

                // Mettre à jour l'état du jeu avec le delta time
                gererDeplacement();
                gestionnaireJeu.mettreAJour(deltaTime);

                // Dessiner tous les éléments
                GraphicsContext gc = toile.getGraphicsContext2D();
                gestionnaireJeu.dessiner(gc);
            }
        };

        // Démarrer l'animation
        animationTimer.start();
    }

    private void gererToucheAppuyee(KeyEvent evenement) {
        touchesAppuyees.add(evenement.getCode()); // Ajouter la touche au Set
    }

    private void gererToucheRelachee(KeyEvent evenement) {
        touchesAppuyees.remove(evenement.getCode()); // Retirer la touche du Set
    }

    private void gererDeplacement() {
        if (touchesAppuyees.contains(KeyCode.UP)) {
            gestionnaireJeu.gererDeplacementRobot(0, -1);
        }
        if (touchesAppuyees.contains(KeyCode.DOWN)) {
            gestionnaireJeu.gererDeplacementRobot(0, 1);
        }
        if (touchesAppuyees.contains(KeyCode.LEFT)) {
            gestionnaireJeu.gererDeplacementRobot(-1, 0);
        }
        if (touchesAppuyees.contains(KeyCode.RIGHT)) {
            gestionnaireJeu.gererDeplacementRobot(1, 0);
        }
        if (touchesAppuyees.contains(KeyCode.SPACE)) {
            if (!espaceAppuye) { // Si la touche ESPACE vient d'être pressée
                espaceAppuye = true;
                gestionnaireJeu.actionBalle();
            }
        } else {
            espaceAppuye = false; // Réinitialiser l'état lorsque la touche est relâchée
        }
        if (touchesAppuyees.contains(KeyCode.E)) {
            if (!eAppuye) { // Si la touche E vient d'être pressée
                eAppuye = true;
                gestionnaireJeu.getRobot().lancerBalle();
            }
        } else {
            eAppuye = false; // Réinitialiser l'état lorsque la touche est relâchée
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
