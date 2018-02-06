package com.gopiandcode.lcs.logging.graphing;

import com.gopiandcode.lcs.logging.RCSClassifierTrainingLogger;

public class ClassifierTrainingLoggerAdapter implements RCSClassifierTrainingLogger {

    private final GraphingLogger delegate;

    public ClassifierTrainingLoggerAdapter(GraphingLogger delegate) {
        this.delegate = delegate;
    }

    @Override
    public void logAccuracyAtIteration(int iteration, double accuracy) {
       delegate.recordValueAtIteration(LoggableDataType.ACCURACY,accuracy, iteration);
    }

    @Override
    public void logPopulationSizeAtIteration(int iteration, double populationSize) {
        delegate.recordValueAtIteration(LoggableDataType.POPULATION_SIZE, populationSize, iteration);
    }

    @Override
    public void logIntermediateClassifierCountAtIteration(int iteration, double intermediateClassifierCount) {
        delegate.recordValueAtIteration(LoggableDataType.INTERMEDIATE_CLASSIFIER_COUNT, intermediateClassifierCount, iteration);
    }

    @Override
    public void logOutputClassifierCountAtIteration(int iteration, double outputClassifierCount) {
        delegate.recordValueAtIteration(LoggableDataType.OUTPUT_CLASSIFIER_COUNT, outputClassifierCount, iteration);
    }
}
