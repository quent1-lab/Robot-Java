package mkx.imtminesales.robot2d.ai;

import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import mkx.imtminesales.robot2d.core.GestionnaireJeu;
import mkx.imtminesales.robot2d.core.Robot;
import mkx.imtminesales.robot2d.physics.RayTracing;

public class IA {

    private GestionnaireJeu gestionnaire;
    private Robot robot;

    // Paramètres de Q-learning
    private Map<String, double[]> qTable; // clé: état discrétisé, valeur: Q-values pour chacune des 11 actions
    private double epsilon; // taux d'exploration
    private double alpha; // taux d'apprentissage
    private double gamma; // facteur de discount

    // Pour le suivi de l'état précédent
    private String previousState = null;
    private int previousAction = -1;
    private int previousScore = 0;
    private double previousRewards = 0.0;
    private int nbActions = 11; // Nombre total d'actions (8 directions + 3 actions spéciales)

    private Random random;

    // Actions spéciales
    private final int ACTION_ATTRAPER = 8;
    private final int ACTION_RELACHER = 9;
    private final int ACTION_LANCER = 10;

    // Définition des 11 actions possibles (8 directions + 3 actions spéciales)
    // Pour les déplacements, on utilise des vecteurs normalisés
    private final double[][] actions = {
            { 0, -1 }, // 0: Nord
            { 1 / sqrt(2), -1 / sqrt(2) }, // 1: Nord-Est
            { 1, 0 }, // 2: Est
            { 1 / sqrt(2), 1 / sqrt(2) }, // 3: Sud-Est
            { 0, 1 }, // 4: Sud
            { -1 / sqrt(2), 1 / sqrt(2) }, // 5: Sud-Ouest
            { -1, 0 }, // 6: Ouest
            { -1 / sqrt(2), -1 / sqrt(2) }, // 7: Nord-Ouest
            null, // 8: Attraper
            null, // 9: Relâcher
            null // 10: Lancer
    };

    private int iteration = 0;

    public IA(GestionnaireJeu gestionnaire) {
        this.gestionnaire = gestionnaire;
        this.robot = gestionnaire.getRobot();
        this.qTable = new HashMap<>();
        this.epsilon = 0.3;
        this.alpha = 0.12;
        this.gamma = 0.85;
        this.previousScore = robot.getScore();
        this.random = new Random();
    }

    /**
     * Méthode d'actualisation de l'IA appelée à chaque frame.
     * Utilise uniquement les données issues du raytracing pour constituer l'état.
     */
    public void update() {
        iteration++;
        // Optionnel : ajuster périodiquement epsilon/alpha (ici on peut les réduire
        // doucement)
        if (iteration % 5000 == 0) {
            epsilon = Math.max(0.05, epsilon * 0.95);
            alpha = Math.max(0.01, alpha * 0.995);
        }

        // Mise à jour des rayons pour avoir les dernières observations
        robot.getRayTracing().mettreAJour();
 
        // Discrétiser l'état à partir de 4 directions clés ET du nombre de balles
        // détenues
        String state = discretiserEtat();

        // Calcul du reward
        int currentScore = robot.getScore();
        double reward = 0.0;

        // Base : différence de score
        reward += (currentScore - previousScore);

        // Récompense/pénalité basée sur les rayons
        double rewardRayons = 0;
        for (RayTracing.Rayon rayon : robot.getRayTracing().getRayons()) {
            if ("Obstacle".equals(rayon.typeObjet) && rayon.distance < 40) {
                rewardRayons -= 10; // forte pénalité si obstacle très proche
            }
            if ("Balle".equals(rayon.typeObjet)) {
                if (rayon.distance < 18) {
                    rewardRayons += 10; // très proche d'une balle
                } else if (rayon.distance < 40) {
                    rewardRayons += 5; // proche d'une balle
                }
            }
        }
        reward += rewardRayons;

        // Récompenses spécifiques pour les actions spéciales
        // Lors de l'action ATTRAPER, récompenser si le robot capte une balle (mais
        // uniquement si proche)
        if (previousAction == ACTION_ATTRAPER) {
            if (robot.aUneBalle()) {
                // On peut utiliser robot.balleAttrapee() pour savoir combien de balles ont été
                // prises dans cette action
                int balleAttrapee = robot.balleAttrapee();
                reward += 20 * balleAttrapee;
            } else {
                // Pénaliser si l'action d'attraper n'a rien capté
                reward -= 5;
            }
        }
        // Pour l'action LANCER, récompenser fortement si le lancer permet de déposer
        // une balle (augmentation de score)
        if (previousAction == ACTION_LANCER) {
            if (robot.aLanceBalleAvecSucces()) {
                reward += 50;
            } else {
                reward -= 5;
            }
        }
        // Pour l'action RELACHER, pénaliser si le robot relâche alors qu'il n'a pas de
        // balle
        if (previousAction == ACTION_RELACHER && !robot.aUneBalle()) {
            reward -= 10;
        }
        // Bonus pour être proche du panier quand le robot transporte des balles
        double distanceAuPanier = distance(robot.getPosition().getX(), robot.getPosition().getY(),
                gestionnaire.getPanier().getPosition().getX() + gestionnaire.getPanier().getLargeur() / 2.0,
                gestionnaire.getPanier().getPosition().getY() + gestionnaire.getPanier().getHauteur() / 2.0);
        if (robot.aUneBalle() && distanceAuPanier < 100) {
            reward += 15;
        }
        if (robot.aUneBalle() && distanceAuPanier < 50) {
            reward += 30;
        }
        if (robot.aUneBalle() && distanceAuPanier < 20) {
            reward += 50;
        }

        // Mise à jour de la Q-table pour la transition précédente
        if (previousState != null && previousAction != -1) {
            double[] qValuesPrev = qTable.getOrDefault(previousState, initQValues());
            double qPrev = qValuesPrev[previousAction];
            double maxQ = maxQValue(state);
            qValuesPrev[previousAction] = qPrev + alpha * (reward + gamma * maxQ - qPrev);
            qTable.put(previousState, qValuesPrev);
        }

        // Choisir la prochaine action selon ε-greedy
        int action = choisirAction(state);
        // Exécuter l'action choisie
        if (action < 8) {
            // Déplacement
            double[] force = actions[action];
            robot.appliquerForce(force[0], force[1]);
        } else if (action == ACTION_ATTRAPER) {
            attraperBalle();
        } else if (action == ACTION_RELACHER) {
            relacherBalle();
        } else if (action == ACTION_LANCER) {
            lancerBalle();
        }

        // Sauvegarde de l'état et de l'action pour la prochaine mise à jour
        previousState = state;
        previousAction = action;
        previousScore = currentScore;
        previousRewards = reward;
    }

    /**
     * Discrétise l'état en fonction des distances des rayons dans 4 directions clés
     * et du nombre de balles détenues par le robot.
     * Format :
     * "A-<binAvant>|G-<binGauche>|AR-<binArriere>|D-<binDroite>|B-<nbBalles>"
     */
    private String discretiserEtat() {
        int binAvant = discretiserDistance(getRayPourAngle(0, robot.getRayTracing().getRayons()));
        int binGauche = discretiserDistance(getRayPourAngle(90, robot.getRayTracing().getRayons()));
        int binArriere = discretiserDistance(getRayPourAngle(180, robot.getRayTracing().getRayons()));
        int binDroite = discretiserDistance(getRayPourAngle(270, robot.getRayTracing().getRayons()));
        int nbBalles = robot.aUneBalle() ? robot.balleAttrapee() : 0;
        return "A-" + binAvant + "|G-" + binGauche + "|AR-" + binArriere + "|D-" + binDroite + "|B-" + nbBalles;
    }

    /**
     * Discrétise la distance d'un rayon en bins.
     * La granularité peut être ajustée.
     */
    private int discretiserDistance(RayTracing.Rayon rayon) {
        double distance = (rayon != null) ? rayon.distance : 1000;
        if (distance < robot.getHauteur() / 2 + 5)
            return 0;
        else if (distance < 100)
            return 1;
        else if (distance < 300)
            return 2;
        else
            return 3;
    }

    /**
     * Retourne le rayon dont l'angle (en degrés) est le plus proche de targetAngle.
     */
    private RayTracing.Rayon getRayPourAngle(double targetAngle, java.util.List<RayTracing.Rayon> rayons) {
        RayTracing.Rayon meilleurRayon = null;
        double minDiff = Double.MAX_VALUE;
        for (RayTracing.Rayon rayon : rayons) {
            double angle = toDegrees(atan2(rayon.directionY, rayon.directionX));
            if (angle < 0)
                angle += 360;
            double diff = abs(angle - targetAngle);
            if (diff > 180)
                diff = 360 - diff;
            if (diff < minDiff) {
                minDiff = diff;
                meilleurRayon = rayon;
            }
        }
        return meilleurRayon;
    }

    /**
     * Calcule la distance euclidienne entre deux points.
     */
    private double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1, dy = y2 - y1;
        return sqrt(dx * dx + dy * dy);
    }

    /**
     * Action pour attraper une balle.
     */
    private void attraperBalle() {
        if (robot.peutAttraperBalle()) {
            robot.attraperBalle(gestionnaire.getBalles());
        }
    }

    /**
     * Action pour relâcher une balle.
     */
    private void relacherBalle() {
        if (robot.aUneBalle()) {
            robot.lacherBalle();
        }
    }

    /**
     * Action pour lancer une balle.
     */
    private void lancerBalle() {
        if (robot.aUneBalle()) {
            robot.lancerBalle();
        }
    }

    /**
     * Initialise un tableau de Q-values à 0 pour toutes les actions.
     */
    private double[] initQValues() {
        return new double[nbActions];
    }

    /**
     * Retourne la valeur maximale Q pour un état donné.
     */
    private double maxQValue(String state) {
        double[] qValues = qTable.getOrDefault(state, initQValues());
        double max = -Double.MAX_VALUE;
        for (double q : qValues) {
            if (q > max)
                max = q;
        }
        return max;
    }

    /**
     * Sélectionne une action selon la politique ε-greedy.
     */
    private int choisirAction(String state) {
        double[] qValues = qTable.getOrDefault(state, initQValues());
        qTable.putIfAbsent(state, qValues);

        // Exploration : une probabilité epsilon d'explorer
        if (random.nextDouble() < epsilon) {
            // Pour une exploration plus dirigée, on peut choisir aléatoirement parmi un
            // ensemble de meilleures actions
            return random.nextBoolean() ? argMax(qValues) : random.nextInt(nbActions);
        } else {
            // Exploitation : choisir l'action avec la Q-value maximale
            return argMax(qValues);
        }
    }

    /**
     * Retourne l'indice de la valeur maximale dans un tableau.
     */
    private int argMax(double[] values) {
        int bestAction = 0;
        double bestValue = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] > bestValue) {
                bestValue = values[i];
                bestAction = i;
            }
        }
        return bestAction;
    }

    // Méthodes pour ajuster epsilon, alpha et gamma (facultatives)
    public void reduireEpsilon(double facteur) {
        if (epsilon > 0.01) {
            epsilon *= facteur;
        }
    }

    public void ajusterAlpha(double facteur) {
        if (alpha > 0.01) {
            alpha *= facteur;
        }
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getGamma() {
        return gamma;
    }

    public void dessiner(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Arial", 16));
        gc.fillText("Epsilon: " + String.format("%.2f", epsilon), 20, gestionnaire.hauteur - 2);
        gc.fillText("Alpha: " + String.format("%.2f", alpha), 120, gestionnaire.hauteur - 2);
        gc.fillText("Gamma: " + String.format("%.2f", gamma), 220, gestionnaire.hauteur - 2);
        gc.fillText("Reward: " + String.format("%.2f", previousRewards), 320, gestionnaire.hauteur - 2);
        gc.fillText("State: " + previousState, 420, gestionnaire.hauteur - 2);
    }
}
