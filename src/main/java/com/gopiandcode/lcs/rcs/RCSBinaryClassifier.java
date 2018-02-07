package com.gopiandcode.lcs.rcs;

import com.google.common.collect.*;
import com.gopiandcode.lcs.dataset.BinaryClassifierTestData;
import com.gopiandcode.lcs.problem.Action;
import com.gopiandcode.lcs.problem.BinaryAlphabet;
import com.gopiandcode.lcs.problem.BinaryClassifier;
import com.gopiandcode.lcs.problem.Situation;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class RCSClassifierOutput {
    Optional<RCSClassifier> outputClassifier = Optional.empty();
    Stack<RCSClassifier> intermediateClassifiers = new Stack<>();

    public RCSClassifierOutput(RCSClassifier classifier) {
        SetNextClassifier(classifier);
    }

    public void SetNextClassifier(RCSClassifier classifier) {
        classifier.decrementLife();
        if (classifier.getOutput().isLeft()) {
            outputClassifier = Optional.of(classifier);
        } else {
            intermediateClassifiers.push(classifier);
        }
    }

    public RCSClassifier GetLastIntermediateClassifier() {
        return intermediateClassifiers.peek();
    }

    public Situation GetCurrentMessage() {
        return intermediateClassifiers.peek().getOutput().right();
    }

    public RCSClassifier GetFinalClassifier() {
        return outputClassifier.get();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(outputClassifier.toString());
        for (RCSClassifier classifier : intermediateClassifiers) {
            sb.append(" <- ");
            sb.append(classifier.toString());
        }
        return sb.toString();
    }

    public Action getAction() {
        return outputClassifier.get().getOutput().left();
    }

    public boolean isComplete() {
        return outputClassifier.isPresent();
    }

    public Stack<RCSClassifier> getIntermediateClassifiers() {
        return intermediateClassifiers;
    }

    public void incrementExp() {
        RCSClassifier outputClassifier = this.outputClassifier.get();
        outputClassifier.setExp(outputClassifier.getExp() + 1);

        this.intermediateClassifiers.stream().forEach(classifier ->
                                                              classifier.setExp(classifier.getExp() + 1));
    }

    public void forEach(Consumer<? super RCSClassifier> function) {
        this.outputClassifier.ifPresent(function);
        this.intermediateClassifiers.stream().forEach(function);
    }

    public <T> Stream<T> map(Function<RCSClassifier, T> function) {
        Stream<T> tStream = this.intermediateClassifiers.stream().map(function);
        if (this.outputClassifier.isPresent()) {
            return Streams.concat(
                    Arrays.asList(this.outputClassifier.map(function).get()).stream(),
                    tStream
            );
        }
        return tStream;
    }

    public int getN() {
        double N = this.outputClassifier.get().getN();

        N += this.intermediateClassifiers.stream().map(RCSClassifier::getN).reduce(Double::sum).orElse(0.0);
        return (int) N;
    }
}

public class RCSBinaryClassifier implements BinaryClassifier {
    public static Ordering<RCSClassifier> RCS_CLASSIFIER_ORDERING = Ordering.from(Comparator.comparingDouble((RCSClassifier o) -> o.getF()));

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
    /*
        Represents the maximum length of a path through the classifiers
     */
    private int life = 3;
    /**
     * Represents the size of classifiers used in the system
     */
    private int intermediateSituationSize = 25;

    public RCSBinaryClassifier(int life, int intermediateSituationSize) {
        this.life = life;
        this.intermediateSituationSize = intermediateSituationSize;
    }

    private List<RCSClassifierOutput> M = new ArrayList<>();
    private List<RCSClassifierOutput> A = new ArrayList<>();
    private List<RCSClassifier> P = new ArrayList<>();
    private long t = 0;
    private double rewardForCorrectClassification = 1000;
    private double rewardForIncorrectClassification = 0;

    @Override
    public boolean runSingleTrainIteration(BinaryClassifierTestData data) {
        Situation sigma = data.getSituation();
        this.generateMatchingOutSet(sigma);
        RCSPredictionArray PA = RCSPredictionArray.generatePredictionArray(this.M);
        Action act = this.selectActionUsing(PA);
        this.generateActionSet(act);
        double p;
        boolean isCorrect;
        if (data.getAction().equals(act)) {
            p = rewardForCorrectClassification;
            isCorrect = true;
        } else {
            p = rewardForIncorrectClassification;
            isCorrect = false;
        }

        this.updateActionSetWith(p);
        this.runGAOnActionSet(sigma);
        t++;
        return isCorrect;
    }

    private void runGAOnActionSet(Situation sigma) {
        long actionSetSize = this.A.stream().map(RCSClassifierOutput::getN).reduce(Integer::sum).orElse(0);
        double averageActionSetTime = this.A.stream().map(
                rcsClassifierOutput -> rcsClassifierOutput.map(classifier -> classifier.getTs() * classifier.getN()
                ).reduce(Double::sum).orElse(0.0)).reduce(Double::sum).orElse(0.0) / actionSetSize;
        if (this.t - averageActionSetTime > this.theta_GA) {
            for (RCSClassifierOutput output : A) {
                output.forEach(cl -> cl.setTs(t));
            }
            List<RCSClassifier> rcsClassifiers = this.selectOffspring();
            RCSClassifier parent1 = rcsClassifiers.get(0);
            RCSClassifier parent2 = rcsClassifiers.get(1);

            RCSClassifier child1 = RCSClassifier.copy(parent1);
            RCSClassifier child2 = RCSClassifier.copy(parent2);

            child1.setN(1);
            child2.setN(1);


            if (ThreadLocalRandom.current().nextDouble() < this.x) {
                RCSClassifier.applyCrossover(child1, child2);
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

            for (RCSClassifier child : Arrays.asList(child1, child2)) {
                child.mutate(sigma, mew);
                this.insertIntoPopulation(child);
                this.deleteFromPopulation();
            }

        }
    }

    private List<RCSClassifier> selectOffspring() {
        // TODO : SELECT OFFSPRING in [A]

        final double[] fitnessSum = {0.0};
        for (RCSClassifierOutput output : A) {
            output.forEach(cl -> fitnessSum[0] += cl.getF());
        }
        double choicePoint = ThreadLocalRandom.current().nextDouble() * fitnessSum[0];

        final RCSClassifier[] child1 = {null};
        fitnessSum[0] = 0.0;
        for (RCSClassifierOutput output : A) {
            output.forEach(cl -> {
                fitnessSum[0] += cl.getF();
                if (child1[0] == null && fitnessSum[0] > choicePoint) {
                    child1[0] = cl;
                }
            });
            if (child1[0] != null) break;
        }


        if (child1[0] == null)
            throw new RuntimeException("Reached end of select offspring method without selecting offspring");

        if (child1[0].getOutput().isLeft()) {
            double outputFitnessSum = 0.0;

            for (RCSClassifierOutput output : A) {
                outputFitnessSum += output.GetFinalClassifier().getF();
            }
            double outputChoicePoint = ThreadLocalRandom.current().nextDouble() * outputFitnessSum;
            outputFitnessSum = 0.0;
            for (RCSClassifierOutput output : A) {
                outputFitnessSum += output.GetFinalClassifier().getF();
                if (outputFitnessSum > outputChoicePoint) {
                    RCSClassifier child2 = output.GetFinalClassifier();
                    return ImmutableList.of(child1[0], child2);
                }
            }

            throw new RuntimeException("Reached end of select offspring method without selecting offspring");
        } else {
            final double[] intermediateFitnessSum = {0.0};
            for (RCSClassifierOutput output : A) {
                output.forEach(cl -> {
                    if (!cl.getOutput().isLeft()
                        && cl.getCondition().getValues().length == child1[0].getCondition().getValues().length
                        && cl.getOutput().right().getValues().length == child1[0].getOutput().right().getValues().length)
                        intermediateFitnessSum[0] += cl.getF();
                });
            }

            if (intermediateFitnessSum[0] == 0.0) return selectOffspring();

            double intermediateChoicePoint = ThreadLocalRandom.current().nextDouble() * intermediateFitnessSum[0];

            intermediateFitnessSum[0] = 0.0;
            final RCSClassifier[] child2 = {null};
            for (RCSClassifierOutput output : A) {
                output.forEach(cl -> {
                    if (!cl.getOutput().isLeft()
                        && cl.getCondition().getValues().length == child1[0].getCondition().getValues().length
                        && cl.getOutput().right().getValues().length == child1[0].getOutput().right().getValues().length) {
                        intermediateFitnessSum[0] += cl.getF();
                        if (child2[0] == null && intermediateFitnessSum[0] > intermediateChoicePoint) {
                            child2[0] = cl;
                        }
                    }
                });
                if (child2[0] != null) break;
            }
            if (child2[0] != null) {
                return ImmutableList.of(child1[0], child2[0]);
            }

            throw new RuntimeException("Reached end of select offspring method without selecting offspring");

        }
    }

    private void generateActionSet(Action act) {
        this.A.clear();

        for (RCSClassifierOutput cl : this.M) {
            if (cl.getAction().equals(act))
                this.A.add(cl);
        }
    }

    private void updateActionSetWith(double p) {
        long actionSetSize = this.A.stream().map(RCSClassifierOutput::getN).reduce(Integer::sum).orElse(0);
        for (RCSClassifierOutput clSeq : this.A) {
            clSeq.incrementExp();

            final double[] beta = {this.beta};
            final double[] p1 = {p};
            final long[] actionSetSize1 = {actionSetSize};
            clSeq.forEach(cl -> {
                if (cl.getExp() < 1 / beta[0]) {
                    // if cl.exp < 1/beta
                    cl.setP(cl.getP() + (p1[0] - cl.getP()) / cl.getExp());
                    // cp.p <- cp.p + (P - cl.p) / cl.exp
                } else {
                    cl.setP(cl.getP() + beta[0] * (p1[0] - cl.getP()));
                    //cl.p <- cl.p + beta * (P - cl.p)
                }

                if (cl.getExp() < 1 / beta[0]) {
                    // if cl.exp < 1/beta
                    cl.setE(cl.getE() + (Math.abs(p1[0] - cl.getP()) - cl.getE()) / cl.getExp());
                    // cl.e <- cl.e + (|P - cl.p| - cl.e)/cl.exp
                } else {
                    cl.setE(cl.getE() + beta[0] * (Math.abs(p1[0] - cl.getP()) - cl.getE()));
                    // cl.e <- cl.e + this.beta * (|P - cl.p| - cl.e)
                }

                if (cl.getExp() < 1 / beta[0]) {
                    cl.setAs(cl.getAs() + (actionSetSize1[0] - cl.getAs()) / cl.getExp());
                    // cl.as <- cl.as + (sum c.n for c in [A] - cl.as)/cl.exp
                } else {
                    cl.setAs(cl.getAs() + beta[0] * (actionSetSize1[0] - cl.getAs()));
                    // cl.as <- cl.as + beta * (sum c.n for c in [a] - cl.as)
                }

//                beta[0] *= 1.1;
//                actionSetSize1[0] *= 0.9;
//                p1[0] *= 0.9;
            });
        }

        this.updateActionSetFitness();
    }

    private void updateActionSetFitness() {
        final double[] accuracySum = {0};
        List<Double> k = new ArrayList<>();
        for (RCSClassifierOutput output : A) {
            output.forEach(cl -> {
                               double value;
                               if (cl.getE() < this.epsilon_nought) {
                                   value = 1.0;
                               } else {
                                   value = this.alpha * Math.pow(cl.getE() / this.epsilon_nought, -1 * this.v);
                               }
                               k.add(value);
                               accuracySum[0] += value * cl.getN();
                           }
            );

        }

        final int[] i = {0};
        for (RCSClassifierOutput output : A) {
            output.forEach(cl -> {
                cl.setF(cl.getF() + this.beta * (k.get(i[0]) * cl.getN() / accuracySum[0] - cl.getF()));
                i[0]++;
            });

        }

    }

    private Action selectActionUsing(RCSPredictionArray pa) {
        if (ThreadLocalRandom.current().nextDouble() < this.p_explr) {
            return pa.selectRandomAction();
        } else {
            return pa.selectBestAction();
        }
    }

    private void generateMatchingOutSet(Situation sigma) {
        this.M.clear();
        while (M.isEmpty()) {
            this.resetPopulation();
            Map<Boolean, List<RCSClassifierOutput>> result = this.P.stream().filter(rcsClassifier -> rcsClassifier.matches(sigma)).map(RCSClassifierOutput::new).collect(Collectors.partitioningBy(RCSClassifierOutput::isComplete));

            List<RCSClassifierOutput> completed = result.get(true);
            List<RCSClassifierOutput> partial = result.get(false);

            this.M.addAll(completed);
            fillMatchingSet(partial);

            Set<BinaryAlphabet> actions = new HashSet<>();
            for (RCSClassifierOutput output : this.M) {
                actions.add(output.getAction().getClassification());
            }
            if (actions.size() < this.theta_mna) {
                RCSClassifier cl_c = this.generateCoveringClassifier(actions, sigma);
                this.insertIntoPopulation(cl_c);
                this.deleteFromPopulation();
                this.M.clear();
            }
        }
    }

    private void deleteFromPopulation() {
        Double populationSize = this.P.stream().map(RCSClassifier::getN).reduce(Double::sum).orElse(0.0);
        if (populationSize <= this.N) return;
        Double averagePopulationFitness = this.P.stream().map(RCSClassifier::getF).reduce(Double::sum).orElse(0.0) / populationSize;
        double votesum = 0.0;
        for (RCSClassifier cl : this.P) {
            double deletionVoteFor = this.getDeletionVoteFor(cl, averagePopulationFitness);
            votesum += deletionVoteFor;
        }
        double choicePoint = ThreadLocalRandom.current().nextDouble() * votesum;
        votesum = 0;
        for (RCSClassifier cl : this.P) {
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

    private double getDeletionVoteFor(RCSClassifier cl, Double averagePopulationFitness) {
        double vote = cl.getAs() * cl.getN();
        if (cl.getExp() > this.theta_del && cl.getF() / cl.getN() < this.delta * averagePopulationFitness) {
            vote *= averagePopulationFitness / (cl.getF() / cl.getN());
        }
        return vote;
    }

    private void insertIntoPopulation(RCSClassifier cl_c) {
        for (RCSClassifier cl : this.P) {
            if (cl.isSame(cl_c)) {
                cl.setN(cl.getN() + 1);
                return;
            }
        }
        this.P.add(cl_c);
    }

    private RCSClassifier generateCoveringClassifier(Set<BinaryAlphabet> actions, Situation sigma) {
        Situation situation = this.selectIntermediateSituationFromMatchSet();
        RCSClassifier classifier;
        if (ThreadLocalRandom.current().nextDouble() < 0.3) {
            classifier = RCSClassifier.coverSituation(situation, this.selectUnseenAction(actions), this.intermediateSituationSize, this.P_sharp);
        } else if (ThreadLocalRandom.current().nextDouble() < 0.6) {
            classifier = RCSClassifier.coverSituation(sigma, situation, sigma.getValues().length, this.P_sharp);
        } else {
            classifier = RCSClassifier.coverSituation(sigma, situation, this.intermediateSituationSize, this.P_sharp);
        }
        classifier.initialize(this.p_I, this.e_I, this.F_I, this.t);
        return classifier;
    }

    private Action selectUnseenAction(Set<BinaryAlphabet> actions) {
        if (actions.contains(BinaryAlphabet.ONE) && !actions.contains(BinaryAlphabet.ZERO)) {
            return Action.getZeroClassification();
        } else if (actions.contains(BinaryAlphabet.ZERO) && !actions.contains(BinaryAlphabet.ONE)) {
            return Action.getOneClassification();
        }
        return Action.createRandom();
    }

    private Situation selectIntermediateSituationFromMatchSet() {
        if (!this.M.isEmpty()) {
            int i = ThreadLocalRandom.current().nextInt(0, this.M.size());
            if (!this.M.get(i).getIntermediateClassifiers().isEmpty()) {
                int j = ThreadLocalRandom.current().nextInt(0, this.M.get(i).getIntermediateClassifiers().size());
                return RCSClassifier.GenerateMatching(this.M.get(i).getIntermediateClassifiers().get(j));
            } else {
                return RCSClassifier.GenerateMatching(this.M.get(i).GetFinalClassifier());
            }
        }
        if (!this.P.isEmpty()) {
            int i = ThreadLocalRandom.current().nextInt(0, this.P.size());
            return RCSClassifier.GenerateMatching(this.P.get(i));
        }
        return Situation.createRandom(this.intermediateSituationSize);
    }

    private void fillMatchingSet(List<RCSClassifierOutput> partial) {
        while (!partial.isEmpty()) {
            Map<Boolean, List<RCSClassifierOutput>> splitOutputs = partial.stream().collect(Collectors.partitioningBy(RCSClassifierOutput::isComplete));

            partial = splitOutputs.get(false);
            this.M.addAll(splitOutputs.get(true));

            Iterator<RCSClassifierOutput> iterator = partial.iterator();
            while (iterator.hasNext()) {
                RCSClassifierOutput partialSeq = iterator.next();
                // run through each output - repeatedly insert matching classifiers until either output or no classifiers match
                boolean noneMatch = false;

                ImmutableList<RCSClassifier> rcsClassifiers = RCS_CLASSIFIER_ORDERING.immutableSortedCopy(this.P);
                while (!partialSeq.isComplete() && !noneMatch) {
                    noneMatch = true;
                    for (RCSClassifier classifier : rcsClassifiers) {
                        if (classifier.matches(partialSeq.GetCurrentMessage())) {
                            partialSeq.SetNextClassifier(classifier);
                            noneMatch = false;
                            break;
                        }
                    }
                }
                // remove non matching classifiers from list
                if (noneMatch) iterator.remove();
            }
        }
    }

    private void resetPopulation() {
        for (RCSClassifier classifier : P) classifier.resetLife(life);
    }

    @Override
    public Action classify(Situation sigma) {
        this.generateMatchingOutSet(sigma);
        RCSPredictionArray PA = RCSPredictionArray.generatePredictionArray(this.M);
        return this.selectActionUsing(PA);
    }

    public long getPopulationSize() {
        return this.P.stream().map(RCSClassifier::getN).reduce(Double::sum).orElse(0.0).longValue();
    }

    public double getOutputClassifierCount() {
        return this.P.stream().filter(classifier -> classifier.getOutput().isLeft()).map(RCSClassifier::getN).reduce(Double::sum).orElse(0.0);
    }

    public double getIntermediateClassifierCount() {
        return this.P.stream().filter(classifier -> !classifier.getOutput().isLeft()).map(RCSClassifier::getN).reduce(Double::sum).orElse(0.0);
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

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = life;
    }

    public int getIntermediateSituationSize() {
        return intermediateSituationSize;
    }

    public void setIntermediateSituationSize(int intermediateSituationSize) {
        this.intermediateSituationSize = intermediateSituationSize;
    }

    public long getT() {
        return t;
    }

    public void setT(long t) {
        this.t = t;
    }

    public double getRewardForCorrectClassification() {
        return rewardForCorrectClassification;
    }

    public void setRewardForCorrectClassification(double rewardForCorrectClassification) {
        this.rewardForCorrectClassification = rewardForCorrectClassification;
    }

    public double getRewardForIncorrectClassification() {
        return rewardForIncorrectClassification;
    }

    public void setRewardForIncorrectClassification(double rewardForIncorrectClassification) {
        this.rewardForIncorrectClassification = rewardForIncorrectClassification;
    }

    public List<RCSClassifier> getPopulation() {
        return this.P;
    }
}
