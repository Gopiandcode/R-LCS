package com.gopiandcode.lcs.logging;

public interface ClassifierTrainingLogger {
    void logAccuracyAtIteration(int iteration, double accuracy);
    void logPopulationSizeAtIteration(int iteration, double populationSize);
}
