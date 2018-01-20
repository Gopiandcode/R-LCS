package com.gopiandcode.lcs.rcs;

import com.gopiandcode.lcs.problem.Action;
import com.gopiandcode.lcs.problem.BinaryAlphabet;
import com.gopiandcode.lcs.problem.Situation;
import com.gopiandcode.lcs.problem.TernaryAlphabet;
import com.gopiandcode.lcs.util.Either;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class RCSClassifier {
    private RCSCondition condition;
    private Either<Action, Situation> output;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(condition);
        if(output.isRight()) {
            sb.append(" -> ");
            sb.append(output.right().toString());
        } else {
            sb.append(": (");
            sb.append(output.left().toString());
            sb.append(")");

        }
        return sb.toString();
    }

    /**
     * acerage payoff expected if classifier matches and action used
     */
    private double p;
    /**
     * estimates errors in prediction value
     */
    private double e;
    private double f;
    /**
     * number of times since creationg classifier has belonged to an action set
     */
    private double exp;
    /**
     * the time step the last of a GA in which the classifier belonged
     */
    private long ts;
    /**
     * estimate of the average size of the action set the classifier belongs to
     */
    private double as;
    /**
     * the number of microclassifiers this classifier represents
     */
    private double n;
    /**
     * value used to prevent infinite loops
     */
    private int life = 0;

    private boolean initialized = false;

    public RCSClassifier(RCSCondition condition, Situation situation) {
        this.condition = condition;
        this.output = Either.right(situation);
    }

    public RCSClassifier(RCSCondition condition, Action action) {
        this.condition = condition;
        this.output = Either.left(action);
    }



    private void checkInitialization() {
        if (!initialized)
            throw new RuntimeException("RCSClassifier used before initialization");
    }


    public void initialize(double p_I, double e_I, double F_I, long t) {
        this.p = p_I;
        this.e = e_I;
        this.f = F_I;
        this.exp = 0;
        this.ts = t;
        this.n = 1;
        this.as = 1;
        this.life = 0;
        this.initialized = true;
    }



    public static Situation GenerateMatching(RCSClassifier cl, int size) {
        cl.checkInitialization();
        TernaryAlphabet[] values = cl.condition.getValues();

        if(values.length < size)
            throw new IllegalArgumentException("Attempted to create matching situation larger than classifer pattern");

        BinaryAlphabet[] result = new BinaryAlphabet[values.length];
        for (int i = 0; i < size; i++) {
           switch(values[i]) {
               case ONE:
                   result[i] =  BinaryAlphabet.ONE;
                   break;
               case ZERO:
                   result[i] =  BinaryAlphabet.ZERO;
                   break;
               case HASH:
                   if(ThreadLocalRandom.current().nextDouble() > 0.5) {
                       result[i] =  BinaryAlphabet.ZERO;
                   } else {
                       result[i] =  BinaryAlphabet.ONE;
                   }
                   break;
           }
        }

        return new Situation(result);

    }

    public static Situation GenerateMatching(RCSClassifier cl) {
       return GenerateMatching(cl, cl.condition.getValues().length);
    }


    public static RCSClassifier copy(RCSClassifier original) {
        Either<Action, Situation> output = original.output;
        RCSClassifier copy;
       if(output.isLeft() ) {
          copy = new RCSClassifier(RCSCondition.copy(original.getCondition()), Action.copy(output.left()));
       } else {
           copy = new RCSClassifier(RCSCondition.copy(original.getCondition()), output.right().copy());
       }

        copy.initialize(
                original.p,
                original.e,
                original.f,
                original.ts
        );
        copy.exp =  original.exp;
        copy.n = original.n;
        copy.as = original.as;
        return copy;
    }

    public static void applyCrossover(RCSClassifier child1, RCSClassifier child2) {
        if(child1.condition.getValues().length != child2.condition.getValues().length)
            throw new IllegalArgumentException("Classifiers don't have same length inputs");
        if(child1.output.isLeft() != child2.output.isLeft())
            throw new IllegalArgumentException("Classifiers are not of the same output type");

        double x = ThreadLocalRandom.current().nextDouble() * (child1.condition.getValues().length + 1);
        double y = ThreadLocalRandom.current().nextDouble() * (child1.condition.getValues().length + 1);
        if(x > y) {
            double temp = x;
            x = y;
            y = temp;
        }

        int i = 0;
        do {
            if(x <= i && i < y) {
                TernaryAlphabet temp = child1.getCondition().getValues()[i];
                child1.getCondition().getValues()[i] = child2.getCondition().getValues()[i];
                child2.getCondition().getValues()[i] = temp;
            }
            i++;
        } while(i < y && i < child1.getCondition().getValues().length);

        child1.initialized = false;
        child2.initialized = false;

    }

    public void mutate(Situation sigma, double mew) {
        int i = 0;
        do {
            if(ThreadLocalRandom.current().nextDouble() < mew) {
                if(i < sigma.getValues().length) {
                    if (condition.getValues()[i] == TernaryAlphabet.HASH) {
                        switch (sigma.getValues()[i]) {
                            case ONE:
                                condition.setValues(i, TernaryAlphabet.ONE);
                                break;
                            case ZERO:
                                condition.setValues(i, TernaryAlphabet.ZERO);
                                break;
                        }
                    } else {
                        condition.setValues(i, TernaryAlphabet.HASH);
                    }
                } else {
                     if (condition.getValues()[i] == TernaryAlphabet.HASH) {
                        switch (condition.getValues()[i]) {
                            case ONE:
                                condition.setValues(i, TernaryAlphabet.ZERO);
                                break;
                            case ZERO:
                                condition.setValues(i, TernaryAlphabet.ONE);
                                break;
                        }
                    } else {
                        condition.setValues(i, TernaryAlphabet.HASH);
                    }

                }
            }
            i++;
        } while(i < condition.getValues().length);
    }

    public static RCSClassifier coverSituation(@NotNull Situation sigma, Action action, int size, double P_hash) {
        return new RCSClassifier(RCSCondition.createCovering(sigma, size, P_hash), action);
    }

     public static RCSClassifier coverSituation(@NotNull Situation sigma, Situation situation, int size, double P_hash) {
        return new RCSClassifier(RCSCondition.createCovering(sigma, size, P_hash), situation);
    }


    public boolean isSame(@NotNull RCSClassifier cl) {
        return cl.condition.equals(condition) && cl.output.equals(output);
    }

    public boolean matches(@NotNull Situation sigma) {
        checkInitialization();
        return life > 0 && this.condition.matches(sigma);
    }


    public RCSCondition getCondition() {
        return condition;
    }

    public Either<Action, Situation> getOutput() {
        return output;
    }


    public double getP() {
        return p;
    }

    public void setP(double p) {
        this.p = p;
    }

    public double getE() {
        return e;
    }

    public void setE(double e) {
        this.e = e;
    }

    public double getF() {
        return f;
    }

    public void setF(double f) {
        this.f = f;
    }

    public double getExp() {
        return exp;
    }

    public void setExp(double exp) {
        this.exp = exp;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public double getAs() {
        return as;
    }

    public void setAs(double as) {
        this.as = as;
    }

    public double getN() {
        return n;
    }

    public void setN(double n) {
        this.n = n;
    }

    public int getLife() {
        return life;
    }

    public boolean isAlive() {
        return life > 0;
    }
    public void decrementLife() {
        life--;
    }

    public void resetLife(int life) {
        this.life = life;
    }
}

