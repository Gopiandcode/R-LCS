package com.gopiandcode.lcs.logging;

public interface RCSClassifierTrainingLogger extends ClassifierTrainingLogger {
    void logIntermediateClassifierCountAtIteration(int iteration, double intermediateClassifierCount);
    void logOutputClassifierCountAtIteration(int iteration, double intermediateClassifierCount);
}
