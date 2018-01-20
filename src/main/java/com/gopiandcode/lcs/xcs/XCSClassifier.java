package com.gopiandcode.lcs.xcs;

import com.gopiandcode.lcs.problem.Action;
import com.gopiandcode.lcs.problem.BinaryAlphabet;
import com.gopiandcode.lcs.problem.TernaryAlphabet;
import com.gopiandcode.lcs.problem.Situation;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class XCSClassifier {

    public static Situation GenerateMatching(XCSClassifier cl) {
        TernaryAlphabet[] values = cl.condition.getValues();
        BinaryAlphabet[] result = new BinaryAlphabet[values.length];
        for (int i = 0; i < values.length; i++) {
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
    @NotNull
    @Override
    public String toString() {
        return "XCSClassifier(" +
                 condition +
                " -> " + action +
                ")(" + n + ")";
    }

    private XCSCondition condition;
    private Action action;
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
    private boolean initialized;

    public XCSClassifier(XCSCondition condition, Action action) {
        this.condition = condition;
        this.action = action;
        this.p = 0;
        this.e = 0;
        this.f = 0;
        this.exp = 0;
        this.ts = 0;
        this.as = 0;
        this.n = 0;
        initialized = false;
    }

    private void checkInitialization() {
        if (!initialized)
            throw new RuntimeException("XCSClassifier used before initialization");
    }

    public XCSCondition getCondition() {
        checkInitialization();
        return condition;
    }

    public void setCondition(XCSCondition condition) {
        checkInitialization();
        this.condition = condition;
    }

    public Action getAction() {
        checkInitialization();
        return action;
    }

    public void setAction(Action action) {
        checkInitialization();
        this.action = action;
    }

    public double getP() {
        checkInitialization();
        return p;
    }

    public void setP(double p) {
        checkInitialization();
        this.p = p;
    }

    public double getE() {
        checkInitialization();
        return e;
    }

    public void setE(double e) {
        checkInitialization();
        this.e = e;
    }

    public double getExp() {
        checkInitialization();
        return exp;
    }

    public void setExp(double exp) {
        checkInitialization();
        this.exp = exp;
    }

    public long getTs() {
        checkInitialization();
        return ts;
    }

    public void setTs(long ts) {
        checkInitialization();
        this.ts = ts;
    }

    public double getAs() {
        checkInitialization();
        return as;
    }

    public void setAs(double as) {
        checkInitialization();
        this.as = as;
    }

    public double getN() {
        checkInitialization();
        return n;
    }

    public void setN(double n) {
        checkInitialization();
        this.n = n;
    }

    public double getF() {
        return f;
    }

    public void setF(double f) {
        this.f = f;
    }

    public boolean matches(@NotNull Situation sigma) {
        checkInitialization();
        return this.condition.matches(sigma);
    }

    public static XCSClassifier coverSituation(@NotNull Situation sigma, Action action, double P_hash) {
        return new XCSClassifier(XCSCondition.createCovering(sigma, P_hash), action);
    }

    public void initialize(double p_I, double e_I, double F_I, long t) {
        this.p = p_I;
        this.e = e_I;
        this.f = F_I;
        this.exp = 0;
        this.ts = t;
        this.n = 1;
        this.as = 1;
        this.initialized = true;
    }

    @NotNull
    public static XCSClassifier copy(@NotNull XCSClassifier original) {
       XCSClassifier copy = new XCSClassifier(XCSCondition.copy(original.getCondition()), Action.copy(original.getAction()));
       copy.initialize(
               original.getP(),
               original.getE(),
               original.getF(),
               original.getTs()
       );
       copy.setExp(original.getExp());
       copy.setN(original.getN());
       copy.setAs(original.getAs());
       return copy;
    }

    public static void applyCrossover(@NotNull XCSClassifier child1, @NotNull XCSClassifier child2) {
        double x = ThreadLocalRandom.current().nextDouble() * (child1.getCondition().getValues().length + 1);
        double y = ThreadLocalRandom.current().nextDouble() * (child1.getCondition().getValues().length + 1);
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

    public void mutate(@NotNull Situation sigma, double mew) {
        int i = 0;
        do {
            if(ThreadLocalRandom.current().nextDouble() < mew) {
               if(condition.getValues()[i] == TernaryAlphabet.HASH) {
                   switch(sigma.getValues()[i]) {
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
            }
            i++;
        } while(i < condition.getValues().length);
    }

    public boolean isSame(@NotNull XCSClassifier cl) {
        return cl.condition.equals(condition) && cl.action.equals(action);
    }
    public static boolean couldSubsume(@NotNull XCSClassifier cl, double theta_sub, double epsilon_nought) {
        if(cl.getExp() > theta_sub) {
            if(cl.getE() < epsilon_nought) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMoreGeneral(@NotNull XCSClassifier general, @NotNull XCSClassifier specific) {
        if(general.getCondition().hashCount() <= specific.getCondition().hashCount()) return false;
        int i = 0;
        TernaryAlphabet[] generalValues = general.getCondition().getValues();
        TernaryAlphabet[] specificValues = specific.getCondition().getValues();
        do {
            TernaryAlphabet genI = generalValues[i];
            TernaryAlphabet specI = specificValues[i];
            if(genI != TernaryAlphabet.HASH && genI != specI)
                return false;
            i++;
        } while(i < generalValues.length);
        return true;
    }

    public static boolean doesSubsume(@NotNull XCSClassifier sub, @NotNull XCSClassifier tos, double theta_sub, double epsilon_nought) {
        if(sub.getAction().equals(tos.getAction()))
            if(XCSClassifier.couldSubsume(sub, theta_sub, epsilon_nought))
                if(XCSClassifier.isMoreGeneral(sub, tos))
                    return true;
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XCSClassifier that = (XCSClassifier) o;

        if (Double.compare(that.p, p) != 0) return false;
        if (Double.compare(that.e, e) != 0) return false;
        if (Double.compare(that.f, f) != 0) return false;
        if (Double.compare(that.exp, exp) != 0) return false;
        if (ts != that.ts) return false;
        if (Double.compare(that.as, as) != 0) return false;
        if (Double.compare(that.n, n) != 0) return false;
        if (initialized != that.initialized) return false;
        if (condition != null ? !condition.equals(that.condition) : that.condition != null) return false;
        return action != null ? action.equals(that.action) : that.action == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = condition != null ? condition.hashCode() : 0;
        result = 31 * result + (action != null ? action.hashCode() : 0);
        temp = Double.doubleToLongBits(p);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(e);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(f);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(exp);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (ts ^ (ts >>> 32));
        temp = Double.doubleToLongBits(as);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(n);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (initialized ? 1 : 0);
        return result;
    }
}
