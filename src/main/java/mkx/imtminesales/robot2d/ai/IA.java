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
    private Map<String, double[]> qTable; // clé: état discretisé, valeur: Q-values pour chacune des 8 actions
    private double epsilon; // taux d'exploration
    private double alpha; // taux d'apprentissage
    private double gamma; // facteur de discount

    // Pour le suivi de l'état précédent
    private String previousState = null;
    private int previousAction = -1;
    private int previousScore = 0;

    private Random random;

    // Définition des 8 actions possibles (vecteurs normalisés)
    // 0: Nord, 1: Nord-Est, 2: Est, 3: Sud-Est, 4: Sud, 5: Sud-Ouest, 6: Ouest, 7:
    // Nord-Ouest
    private final double[][] actions = {
            { 0, -1 },
            { 1 / sqrt(2), -1 / sqrt(2) },
            { 1, 0 },
            { 1 / sqrt(2), 1 / sqrt(2) },
            { 0, 1 },
            { -1 / sqrt(2), 1 / sqrt(2) },
            { -1, 0 },
            { -1 / sqrt(2), -1 / sqrt(2) }
    };

    public IA(GestionnaireJeu gestionnaire) {
        this.gestionnaire = gestionnaire;
        this.robot = gestionnaire.getRobot();

        this.qTable = new HashMap<>();
        this.epsilon = 0.2;
        this.alpha = 0.1;
        this.gamma = 0.9;

        this.previousScore = robot.getScore();
        this.random = new Random();
    }

    /**
     * Méthode d'actualisation de l'IA appelée à chaque frame de mise à jour.
     * Elle utilise uniquement les données issues du raytracing pour constituer
     * l'état.
     */
    public void update() {
        // Mettre à jour les rayons du robot (les informations "visuelles")
        robot.getRayTracing().mettreAJour();

        // Discrétisation de l'état à partir de 4 directions clés : avant (0°), gauche
        // (90°), arrière (180°) et droite (270°)
        String state = discretiserEtat();

        // Calculer le reward basé sur l'évolution du score et la proximité d'obstacles
        int currentScore = robot.getScore();
        double reward = currentScore - previousScore;

        // Vérifier tous les rayons pour détecter les obstacles proches
        double penaliteObstacle = 0;
        for (RayTracing.Rayon rayon : robot.getRayTracing().getRayons()) {
            if ("Obstacle".equals(rayon.typeObjet) && rayon.distance < 30) {
                penaliteObstacle -= 10; // Pénalité accrue pour chaque obstacle proche
            }
        }

        // Ajouter la pénalité totale au reward
        reward += penaliteObstacle;

        // Si un état précédent existe, mettre à jour la Q-value de la transition
        // précédente
        if (previousState != null && previousAction != -1) {
            double[] qValuesPrev = qTable.getOrDefault(previousState, initQValues());
            double qPrev = qValuesPrev[previousAction];
            double maxQ = maxQValue(state);
            // Mise à jour de la Q-value (formule Q-learning)
            qValuesPrev[previousAction] = qPrev + alpha * (reward + gamma * maxQ - qPrev);
            qTable.put(previousState, qValuesPrev);
        }

        // Choisir la prochaine action via la stratégie ε-greedy
        int action = choisirAction(state);
        // Appliquer la force correspondant à l'action choisie
        double[] force = actions[action];
        robot.appliquerForce(force[0], force[1]);

        // Sauvegarder l'état et l'action pour la prochaine mise à jour
        previousState = state;
        previousAction = action;
        previousScore = currentScore;
    }

    /**
     * Discrétise l'état à partir des distances des rayons dans 4 directions clés.
     * Chaque distance est convertie en bin : 0 (proche), 1 (moyen), 2 (éloigné).
     */
    private String discretiserEtat() {
        int binAvant = discretiserDistance(getRayPourAngle(0, robot.getRayTracing().getRayons()));
        int binGauche = discretiserDistance(getRayPourAngle(90, robot.getRayTracing().getRayons()));
        int binArriere = discretiserDistance(getRayPourAngle(180, robot.getRayTracing().getRayons()));
        int binDroite = discretiserDistance(getRayPourAngle(270, robot.getRayTracing().getRayons()));
        // Format de l'état : "A-<binAvant>|G-<binGauche>|AR-<binArriere>|D-<binDroite>"
        return "A-" + binAvant + "|G-" + binGauche + "|AR-" + binArriere + "|D-" + binDroite;
    }

    /**
     * Discrétise la distance d'un rayon en 3 bins.
     * Si le rayon est null, on considère la distance maximale.
     * 
     * @param rayon Le rayon à discrétiser.
     * @return 0 pour "proche" (< 100), 1 pour "moyen" (< 300), 2 pour "éloigné" (>=
     *         300)
     */
    private int discretiserDistance(RayTracing.Rayon rayon) {
        double distance = (rayon != null) ? rayon.distance : 1000;
        if (distance < 100)
            return 0;
        else if (distance < 300)
            return 1;
        else
            return 2;
    }

    /**
     * Retourne le rayon dont l'angle (en degrés) est le plus proche de targetAngle.
     * 
     * @param targetAngle L'angle cible en degrés.
     * @param rayons      La liste des rayons du robot.
     * @return Le rayon correspondant ou null s'il n'existe pas.
     */
    private RayTracing.Rayon getRayPourAngle(double targetAngle, java.util.List<RayTracing.Rayon> rayons) {
        RayTracing.Rayon meilleurRayon = null;
        double minDiff = Double.MAX_VALUE;
        for (RayTracing.Rayon rayon : rayons) {
            double angle = toDegrees(atan2(rayon.directionY, rayon.directionX));
            // Normaliser l'angle entre 0 et 360
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
     * Initialise un tableau de Q-values à 0 pour les 8 actions.
     */
    private double[] initQValues() {
        return new double[8];
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
     * Sélectionne une action selon une politique ε-greedy.
     */
    private int choisirAction(String state) {
        double[] qValues = qTable.getOrDefault(state, initQValues());
        qTable.putIfAbsent(state, qValues);
        if (random.nextDouble() < epsilon) {
            // Exploration : action aléatoire
            return random.nextInt(8);
        } else {
            // Exploitation : choisir l'action ayant la Q-value maximale
            int bestAction = 0;
            double bestValue = qValues[0];
            for (int i = 1; i < qValues.length; i++) {
                if (qValues[i] > bestValue) {
                    bestValue = qValues[i];
                    bestAction = i;
                }
            }
            return bestAction;
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
        // Dessiner les variables de l'IA sur le canvas et son choix
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Arial", 16));
        gc.fillText("Epsilon: " + epsilon, 250, 17);
        gc.fillText("Alpha: " + alpha, 350, 17);
        gc.fillText("Gamma: " + gamma, 450, 17);
        gc.fillText("Action: " + previousAction, 550, 17);
    }
}
