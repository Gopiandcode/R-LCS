package com.gopiandcode.lcs.problem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Problem {
    private final List<Situation> steps;

    public Problem(List<Situation> environment) {
        this.steps = environment;
    }

    public Problem(Situation... steps) {
        this.steps = new ArrayList<>();
        Collections.addAll(this.steps, steps);
    }

    public Situation getSituationAt(int index) {
        return this.steps.get(index);
    }

    public int getSteps() {
        return this.steps.size();
    }

    @NotNull
    @Override
    public String toString() {
        return "Problem(" +
                steps +
                ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Problem problem = (Problem) o;

        return steps != null ? steps.equals(problem.steps) : problem.steps == null;
    }

    @Override
    public int hashCode() {
        return steps != null ? steps.hashCode() : 0;
    }
}
