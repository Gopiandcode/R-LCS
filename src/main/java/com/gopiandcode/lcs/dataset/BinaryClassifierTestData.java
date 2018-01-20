package com.gopiandcode.lcs.dataset;

import com.gopiandcode.lcs.problem.Action;
import com.gopiandcode.lcs.problem.Situation;

public class BinaryClassifierTestData {
    private final Situation situation;
    private final Action classification;

    public BinaryClassifierTestData(Situation situation, Action classification) {
        this.situation = situation;
        this.classification = classification;
    }

    public Situation getSituation() {
        return situation;
    }
    public Action getAction() {
        return classification;
    }

    @Override
    public String toString() {
        return "BinaryClassifierTestData{" +
               "situation=" + situation +
               ", classification=" + classification +
               '}';
    }
}
