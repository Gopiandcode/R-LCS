/**
 * XCSBinaryClassifier Implementation - Based on An Algorithmic Description of XCSBinaryClassifier - Martin V. Butz and Stewart W.Wilson
 */
package com.gopiandcode.lcs.xcs;

import com.gopiandcode.lcs.problem.BinaryAlphabet;
import com.gopiandcode.lcs.problem.BinaryClassifier;
import com.gopiandcode.lcs.problem.Situation;
import com.gopiandcode.lcs.dataset.BinaryClassifierTestData;
import com.gopiandcode.lcs.problem.Action;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static java.util.concurrent.ThreadLocalRandom.current;

public class XCSBinaryClassifier implements BinaryClassifier {
    /**
     * Maximum size of population
     * <p>
     * should be large enough that covering only occurs at the beginning of a run
     */
    private long N = 10000;
    /**
     * Learning rate for p, e, f, and as
     * <p>
     * should be in the range 0.1 - 0.2
     */
    private double beta = 0.15;
    /**
     * Parameter used in calculating the fitness of a classifier
     * <p>
     * should be in the range 0.1
     */
    private double alpha = 0.1;
    /**
     * Parameter used in calculating the fitness of a classifier
     * should be 1% of the maximum value of p
     */
    private double epsilon_nought = 1; // assuming maximum is 100
    /**
     * Parameter used in calculating the fitness of a classifier
     * <p>
     * typically 5
     */
    private double v = 5;

    /**
     * discount factor used in multistep problems to update classifier predictions
     * <p>
     * typically 0.71
     */
    private double gamma = 0.71;
    /**
     * GA threshold - GA is applied when average time since last GA is greater than this value
     * <p>
     * often in the range 25-50
     */
    private double theta_GA = 37.5;
    /**
     * probability of applying crossover
     * <p>
     * often in the range 0.5-1.0
     */
    private double x = 0.725;
    /**
     * probability of mutating an allele in the offspring
     * <p>
     * often in the range 0.01-0.05
     */
    private double mew = 0.025;
    /**
     * deletion threshold below which classifier considered for deletion
     * <p>
     * could be about 20
     */
    private double theta_del = 20;
    /**
     * fraction of mean fitness below which a classifier's fitness is then used for considering it's deletion
     * <p>
     * often 0.1
     */
    private double delta = 0.1;
    /**
     * subsumption threshold above which classifiers are elligeable for subsuming others
     * <p>
     * around 20, although in some problems larger values work better
     */
    private double theta_sub = 20;
    /**
     * probability of using a # in an attribute in C
     * <p>
     * could be around 0.33
     */
    private double P_sharp = 0.33;
    /**
     * initial value for prediction
     * <p>
     * very small - essentially 0
     */
    private double p_I = 0.0001;
    /**
     * initial value of prediction error
     * <p>
     * very small - essentially 0
     */
    private double e_I = 0.0001;
    /**
     * initial value of fitness
     * <p>
     * very small - essentially 0
     */
    private double F_I = 0.0001;
    /**
     * specifies probability during action selection of choosing the value uniform randomly
     * <p>
     * could be 0.5 - depends on type of experiment
     */
    private double p_explr = 0.01;
    /**
     * minimal number of actions required for no covering to happen
     * <p>
     * to achieve maximum covering set this to the number of actions
     */
    private double theta_mna = 2;
    /**
     * whether offspring are tested for subsumption by parents
     * <p>
     * usefull in problems with well defined target functions - i.e multiplexer
     */
    private boolean doGASubsumption = false;
    /**
     * whether action sets are to be tested for subsuming
     * <p>
     * stronger than GA subsumption and condenses population more
     */
    private boolean doActionSetSubsumption = false;

    /**
     * timestep
     */
    private long t = 0;
    /**
     * Last reward
     */
    private double p_1;
    /**
     * Last situation
     */
    private Situation sigma_1;
    /**
     * population - all classifiers that exist at time t
     */
    @NotNull
    private List<XCSClassifier> P = new ArrayList<>();
    /**
     * match set formed from P - includes all classifiers that match the current situation
     */
    @NotNull
    private List<XCSClassifier> M = new ArrayList<>();
    /**
     * action set formed from M - includes all classifiers of M which propose executed action
     */
    @NotNull
    private List<XCSClassifier> A = new ArrayList<>();
    /**
     * previous action set
     */
    @NotNull
    private List<XCSClassifier> A_1 = new ArrayList<>();

    private double rewardForCorrectClassification = 1000;
    private double rewardForInCorrectClassification = 0;



    @Override
    public boolean runSingleTrainIteration(BinaryClassifierTestData data) {
        Situation sigma = data.getSituation();
        this.generateMatchSet(sigma);
        XCSPredictionArray PA = XCSPredictionArray.generatePredictionArray(this.M);
        Action act = this.selectActionUsing(PA);
        this.generateActionSet(act);
        double p;
        boolean isCorrect;
        if (data.getAction().equals(act)) {
            p = rewardForCorrectClassification;
            isCorrect = true;
        } else {
           p = rewardForInCorrectClassification;
           isCorrect = false;
        }

//         for multistep problems
//        if (this.A_1.size() > 0) {
//            double Ptemp = this.p_1 + this.gamma * PA.findMax();
//            this.updateActionSet(this.A_1, Ptemp);
//            this.runGAOnActionSet(this.A_1, this.sigma_1);
//        }
//        if (endOfProblem) {
        this.updateActionSet(this.A, p);
        this.runGAOnActionSet(this.A, sigma);
        this.A_1.clear();
//        } else {
//            assert false;
//            this.A_1.addAll(this.A);
//            this.p_1 = p;
//            this.sigma_1 = sigma;
//        }
        t++;
        return isCorrect;
    }

    @Override
    public Action classify(Situation sigma) {
        this.generateMatchSet(sigma);
        XCSPredictionArray PA = XCSPredictionArray.generatePredictionArray(this.M);
        return this.selectActionUsing(PA);
    }

    private void generateActionSet(Action act) {
        //  GENERATE ACTION SET out of [M] according to act
        this.A.clear();
        for (XCSClassifier cl : this.M) {
            if (cl.getAction().equals(act)) {
                this.A.add(cl);
            }
        }
    }

    private Action selectActionUsing(@NotNull XCSPredictionArray PA) {
        if (current().nextDouble() < this.p_explr) {
            return PA.selectRandomAction();
        } else {
            return PA.selectBestAction();
        }
    }

    private void generateMatchSet(@NotNull Situation sigma) {
        // [M] <- Generate Match set out of [P] using Sigma
        this.M.clear();
        while (this.M.isEmpty()) {
            for (XCSClassifier cl : this.P) {
                if (cl.matches(sigma)) {
                    this.M.add(cl);
                }
            }

            Set<BinaryAlphabet> actions = new HashSet<>();
            for (XCSClassifier classifier : this.M) {
                actions.add(classifier.getAction().getClassification());
            }
            if (actions.size() < this.theta_mna) {
                XCSClassifier cl_c = this.generateCoveringClassifier(sigma);
                this.insertIntoPopulation(cl_c);
                this.deleteFromPopulation();
                this.M.clear();
            }
        }
    }

    @NotNull
    private XCSClassifier generateCoveringClassifier(@NotNull Situation sigma) {
        XCSClassifier classifer = XCSClassifier.coverSituation(sigma, this.selectActionFromMatchSet(), this.P_sharp);
        assert classifer.matches(sigma);
        classifer.initialize(this.p_I, this.e_I, this.F_I, this.t);
        return classifer;
    }

    @NotNull
    private Action selectActionFromMatchSet() {
        Set<Action> actionsInM = new HashSet<>();
        for (XCSClassifier cl : this.M) {
            actionsInM.add(cl.getAction());
        }

        Action oneClassification = Action.getOneClassification();
        Action zeroClassification = Action.getZeroClassification();
        if (actionsInM.contains(oneClassification) && !actionsInM.contains(zeroClassification)) {
            return zeroClassification;
        } else if (actionsInM.contains(zeroClassification) && !actionsInM.contains(oneClassification)) {
            return oneClassification;
        } else {
            return Action.createRandom();
        }
    }

    private void deleteFromPopulation() {
        long populationSize = this.getPopulationSize();
        if (populationSize <= this.N) {
            return;
        }
        double averagePopulationFitness = getAveragePopulationFitness(populationSize);
        double votesum = 0.0;
        for (XCSClassifier cl : this.P) {
            double deletionVoteFor = this.getDeletionVoteFor(cl, averagePopulationFitness);
            votesum += deletionVoteFor;
        }
        double choicePoint = ThreadLocalRandom.current().nextDouble() * votesum;
        votesum = 0;
        for (XCSClassifier cl : this.P) {
            votesum += this.getDeletionVoteFor(cl, averagePopulationFitness);
            if (votesum > choicePoint) {
                if (cl.getN() > 1) {
                    cl.setN(cl.getN() - 1);
                } else {
                    this.P.remove(cl);
                }
                return;
            }
        }
    }

    private double getDeletionVoteFor(@NotNull XCSClassifier cl, double averagePopulationFitness) {
        double vote = cl.getAs() * cl.getN();
        if (cl.getExp() > this.theta_del && cl.getF() / cl.getN() < this.delta * averagePopulationFitness) {
            vote *= averagePopulationFitness / (cl.getF() / cl.getN());
        }
        return vote;
    }

    private double getAveragePopulationFitness() {
        return getAveragePopulationFitness(getPopulationSize());
    }

    private double getAveragePopulationFitness(long populationSize) {
        double totalFitness = 0;
        for (XCSClassifier cl : this.P) {
            totalFitness += cl.getF();
        }
        return totalFitness / populationSize;
    }

    public long getPopulationSize() {
        long size = 0;
        for (XCSClassifier cl : this.P) {
            size += cl.getN();
        }
        return size;
    }


    private void updateActionSet(@NotNull List<XCSClassifier> a, double P) {
        // TODO: UPDATE SET A using p possibly deleting in [P]
        Double actionSetSize = a.stream().map(XCSClassifier::getN).reduce(Double::sum).orElse(0.0);
        for (XCSClassifier cl : a) {
            // for each classifer cl in [a]

            cl.setExp(cl.getExp() + 1);
            // cl.exp++

            if (cl.getExp() < 1 / this.beta) {
                // if cl.exp < 1/beta
                cl.setP(cl.getP() + (P - cl.getP()) / cl.getExp());
                // cp.p <- cp.p + (P - cl.p) / cl.exp
            } else {
                cl.setP(cl.getP() + this.beta * (P - cl.getP()));
                //cl.p <- cl.p + beta * (P - cl.p)
            }

            if (cl.getExp() < 1 / this.beta) {
                // if cl.exp < 1/beta
                cl.setE(cl.getE() + (Math.abs(P - cl.getP()) - cl.getE()) / cl.getExp());
                // cl.e <- cl.e + (|P - cl.p| - cl.e)/cl.exp
            } else {
                cl.setE(cl.getE() + this.beta * (Math.abs(P - cl.getP()) - cl.getE()));
                // cl.e <- cl.e + this.beta * (|P - cl.p| - cl.e)
            }

            if (cl.getExp() < 1 / this.beta) {
                cl.setAs(cl.getAs() + (actionSetSize - cl.getAs()) / cl.getExp());
                // cl.as <- cl.as + (sum c.n for c in [A] - cl.as)/cl.exp
            } else {
                cl.setAs(cl.getAs() + this.beta * (actionSetSize - cl.getAs()));
                // cl.as <- cl.as + beta * (sum c.n for c in [a] - cl.as)
            }
        }

        this.updateFitnessActionSet(a);
        if (this.doActionSetSubsumption) {
            this.performActionSetSubsumption(a);
        }
    }

    private void updateFitnessActionSet(@NotNull List<XCSClassifier> a) {
        // UPDATE fitness in set [A]
        double accuracySum = 0;
        List<Double> k = new ArrayList<>();
        for (XCSClassifier cl : a) {
            double value;
            if (cl.getE() < this.epsilon_nought) {
                value = 1.0;
            } else {
                value = this.alpha * Math.pow(cl.getE() / this.epsilon_nought, -1 * this.v);
            }
            k.add(value);
            accuracySum += value * cl.getN();
        }

        for (int i = 0; i < a.size(); i++) {
            XCSClassifier cl = a.get(i);
            cl.setF(cl.getF() + this.beta * (k.get(i) * cl.getN() / accuracySum - cl.getF()));
        }

    }

    private void runGAOnActionSet(@NotNull List<XCSClassifier> a, @NotNull Situation sigma) {
        // TODO: RUN GA in A considering sigma_1 inserting and possibly deleting in [P]

        Double actionSetSize = a.stream().map(XCSClassifier::getN).reduce(Double::sum).orElse(0.0);
        Double averageActionSetTime = a.stream()
                                       .map(classifier -> classifier.getTs() * classifier.getN())
                                       .reduce(Double::sum)
                                       .orElse(0.0) / actionSetSize;
        if (this.t - averageActionSetTime > this.theta_GA) {
            for (XCSClassifier cl : a) {
                cl.setTs(t);
            }
            XCSClassifier parent1 = this.selectOffspring(a);
            XCSClassifier parent2 = this.selectOffspring(a);

            XCSClassifier child1 = XCSClassifier.copy(parent1);
            XCSClassifier child2 = XCSClassifier.copy(parent2);

            child1.setN(1);
            child2.setN(1);

            if (ThreadLocalRandom.current().nextDouble() < this.x) {
                XCSClassifier.applyCrossover(child1, child2);
                child1.initialize(
                        (parent1.getP() + parent2.getP()) / 2,
                        (parent1.getE() + parent2.getE()) / 2,
                        (parent1.getF() + parent2.getF()) / 2, t
                );
                child2.initialize(
                        (parent1.getP() + parent2.getP()) / 2,
                        (parent1.getE() + parent2.getE()) / 2,
                        (parent1.getF() + parent2.getF()) / 2, t
                );
            }
            child1.setF(child1.getF() * 0.1);
            child2.setF(child2.getF() * 0.1);

            for (XCSClassifier child : Arrays.asList(child1, child2)) {
                child.mutate(sigma, mew);
                if (this.doGASubsumption) {
                    assert false;
                    if (XCSClassifier.doesSubsume(parent1, child, this.epsilon_nought, this.theta_sub)) {
                        parent1.setN(parent1.getN() + 1);
                    } else if (XCSClassifier.doesSubsume(parent2, child, this.epsilon_nought, this.theta_sub)) {
                        parent2.setN(parent2.getN() + 1);
                    } else {
                        this.insertIntoPopulation(child);
                    }
                } else {
                    this.insertIntoPopulation(child);
                }
                this.deleteFromPopulation();
            }
        }
    }

    private void insertIntoPopulation(@NotNull XCSClassifier child) {
        // INSERT child into [P]
        for (XCSClassifier cl : this.P) {
            if (cl.isSame(child)) {
                cl.setN(cl.getN() + 1);
                return;
            }
        }
        this.P.add(child);
    }

    @NotNull
    private XCSClassifier selectOffspring(@NotNull List<XCSClassifier> a) {
        // TODO : SELECT OFFSPRING in [A]
        double fitnessSum = 0.0;
        for (XCSClassifier cl : a) {
            fitnessSum += cl.getF();
        }
        double choicePoint = ThreadLocalRandom.current().nextDouble() * fitnessSum;
        fitnessSum = 0.0;
        for (XCSClassifier cl : a) {
            fitnessSum += cl.getF();
            if (fitnessSum > choicePoint) {
                return cl;
            }
        }
        throw new RuntimeException("Reached end of select offspring method without selecting offspring");
    }

    private void performActionSetSubsumption(@NotNull List<XCSClassifier> a) {
        // DO ACTION SET SUBSUMPTION in [A] updating [P]
        Optional<XCSClassifier> cl = Optional.empty();

        for (XCSClassifier c : a) {
            if (XCSClassifier.couldSubsume(c, this.theta_sub, this.epsilon_nought)) {
                if (!cl.isPresent() || c.getCondition().hashCount() > cl.get().getCondition().hashCount() ||
                    (c.getCondition().hashCount() == cl.get().getCondition().hashCount() && ThreadLocalRandom.current().nextDouble() < 0.5)
                        ) {

                    cl = Optional.of(c);
                }
            }
        }

        cl.ifPresent(classifier -> {
            Iterator<XCSClassifier> iterator = a.iterator();
            while (iterator.hasNext()) {
                XCSClassifier c = iterator.next();

                if (XCSClassifier.isMoreGeneral(classifier, c)) {
                    classifier.setN(classifier.getN() + c.getN());
                    iterator.remove();
                    this.P.remove(c);
                }
            }
        });
    }

    public List<XCSClassifier> getTopN(long n) {

        return this.P.stream().sorted((o1, o2) -> -1 * Double.compare(o1.getF(), o2.getF())).limit(n).collect(Collectors.toList());
    }

    public long getN() {
        return N;
    }

    public void setN(long n) {
        N = n;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getEpsilon_nought() {
        return epsilon_nought;
    }

    public void setEpsilon_nought(double epsilon_nought) {
        this.epsilon_nought = epsilon_nought;
    }

    public double getV() {
        return v;
    }

    public void setV(double v) {
        this.v = v;
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public double getTheta_GA() {
        return theta_GA;
    }

    public void setTheta_GA(double theta_GA) {
        this.theta_GA = theta_GA;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getMew() {
        return mew;
    }

    public void setMew(double mew) {
        this.mew = mew;
    }

    public double getTheta_del() {
        return theta_del;
    }

    public void setTheta_del(double theta_del) {
        this.theta_del = theta_del;
    }

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    public double getTheta_sub() {
        return theta_sub;
    }

    public void setTheta_sub(double theta_sub) {
        this.theta_sub = theta_sub;
    }

    public double getP_sharp() {
        return P_sharp;
    }

    public void setP_sharp(double p_sharp) {
        P_sharp = p_sharp;
    }

    public double getP_I() {
        return p_I;
    }

    public void setP_I(double p_I) {
        this.p_I = p_I;
    }

    public double getE_I() {
        return e_I;
    }

    public void setE_I(double e_I) {
        this.e_I = e_I;
    }

    public double getF_I() {
        return F_I;
    }

    public void setF_I(double f_I) {
        F_I = f_I;
    }

    public double getP_explr() {
        return p_explr;
    }

    public void setP_explr(double p_explr) {
        this.p_explr = p_explr;
    }

    public double getTheta_mna() {
        return theta_mna;
    }

    public void setTheta_mna(double theta_mna) {
        this.theta_mna = theta_mna;
    }

    public boolean isDoGASubsumption() {
        return doGASubsumption;
    }

    public void setDoGASubsumption(boolean doGASubsumption) {
        this.doGASubsumption = doGASubsumption;
    }

    public boolean isDoActionSetSubsumption() {
        return doActionSetSubsumption;
    }

    public void setDoActionSetSubsumption(boolean doActionSetSubsumption) {
        this.doActionSetSubsumption = doActionSetSubsumption;
    }

    public static void main(String[] args) {
//        GraphingLogger logger = new GraphingLogger("XCSBinaryClassifier Accuracy over Training Iterations");
//        List<Double> lastNScores = new ArrayList<>();
//        int n = 10;
//        int problemCount = 10;
//        ReinforcementProgram rp = new MultiplexerReinforcementProgram(1000.0, 0.0);
//        Environment env = new Environment(problemCount);
//        XCSBinaryClassifier lcs = new XCSBinaryClassifier();
//        lcs.setMew(0.001);
////        lcs.setTheta_GA(10000);
////        lcs.setN(300);
//        lcs.setDoGASubsumption(false);
////        lcs.setTheta_mna(20);
//        lcs.setDoActionSetSubsumption(false);
//
//        int iterationCount = 0;
//
//        for (int i = 0; i < 1000; i++) {
//            Optional<Situation> value = env.getSituation(iterationCount);
//            while (value.isPresent()) {
//                lcs.runSingleTrainIteration(value.get(), env.isEndOfProblem());
//                value = env.getSituation(iterationCount);
//                iterationCount++;
//            }
//
//            lastNScores.add(rp.getSystemScorer().getAccuracy() * 100);
//            if (lastNScores.size() > n) {
//                lastNScores.remove(0);
//            }
//
//            env = (new Environment(problemCount));
//            rp.getSystemScorer().reset();
//
//            if (i % Math.max(1, n) == 0 && lastNScores.size() > 0) {
//                double sx = lastNScores.stream().reduce(Double::sum).orElse(0.0);
//                double sx2 = lastNScores.stream().map(x -> x * x).reduce(Double::sum).orElse(0.0);
//
//                double xbar = sx / lastNScores.size();
//                double x2bar = sx2 / lastNScores.size();
//
//                double var = x2bar - xbar * xbar;
//                double sd = Math.sqrt(var);
//
//                System.out.println("[" + iterationCount + "]: Average of last " + n + ": " + xbar + ", sd: " + sd);
//
//                logger.recordValueAtIteration(xbar, iterationCount);
//
//                lastNScores.clear();
//            }
//        }
//
//        GraphRenderer renderer = new GraphRenderer(logger, 1280, 720);
//        renderer.save("result.png");

    }

    public double getRewardForInCorrectClassification() {
        return rewardForInCorrectClassification;
    }

    public void setRewardForInCorrectClassification(double rewardForInCorrectClassification) {
        this.rewardForInCorrectClassification = rewardForInCorrectClassification;
    }

    public double getRewardForCorrectClassification() {
        return rewardForCorrectClassification;
    }

    public void setRewardForCorrectClassification(double rewardForCorrectClassification) {
        this.rewardForCorrectClassification = rewardForCorrectClassification;
    }
}
