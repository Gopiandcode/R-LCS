package com.gopiandcode.lcs.rcs;

import com.gopiandcode.lcs.problem.Action;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class RCSPredictionArray {
    @Override
    public String toString() {
        return "RCSPredictionArray{" +
                predictionArray +
               '}';
    }

    private final Map<Action, Double> predictionArray;

     public double findMax() {
        return predictionArray.values().stream().max(Double::compare).orElse(0.0);
    }


    public RCSPredictionArray(Map<Action, Double> pa) {

        this.predictionArray = pa;
    }

    public static RCSPredictionArray generatePredictionArray(List<RCSClassifierOutput> m) {
         Map<Action, Double> pa = new LinkedHashMap<>();
        Map<Action, Double> fsa = new HashMap<>();
        for (RCSClassifierOutput output : m) {
            RCSClassifier cl = output.GetFinalClassifier();
            //  pa[cl.a] <- pa[cl.a] + cl.p * cl.f
            Action outputAction = cl.getOutput().left();
            pa.put(
                    outputAction,
                    // pa[cl.a]
                    pa.getOrDefault(outputAction, 0.0) +
                    // cl.p * cl.f
                            cl.getP() * cl.getF());
            // fsa[cl.a] = fsa[cl.a] + cl.f
            fsa.put(outputAction,
                    // fsa[cl.a]
                    fsa.getOrDefault(outputAction, 0.0) +
                    // cl.f
                    cl.getF());
        }

        for (Action a : fsa.keySet()) {
            // if fsa[a] != 0.0, pa[a] <- pa[a] / fsa[a]
            if(fsa.get(a) != 0)
                pa.put(a, pa.get(a) / fsa.get(a));
        }
        return new RCSPredictionArray(pa);

    }

     public Action selectRandomAction() {
        List<Action> actions = new ArrayList<>(predictionArray.keySet());
        int size = predictionArray.keySet().size();
        return actions.get(ThreadLocalRandom.current().nextInt(0, size));
    }

    public Action selectBestAction() {
        Optional<Action> bestSeen = Optional.empty();
        Optional<Double> bestScore = Optional.empty();
        for (Map.Entry<Action, Double> actionDoubleEntry : predictionArray.entrySet()) {
            if (!bestScore.isPresent() || bestScore.get() < actionDoubleEntry.getValue()) {
                bestScore = Optional.of(actionDoubleEntry.getValue());
                bestSeen = Optional.of(actionDoubleEntry.getKey());
            }
        }
        return bestSeen.orElse(Action.createRandom());
    }

}
