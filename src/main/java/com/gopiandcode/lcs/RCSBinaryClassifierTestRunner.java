package com.gopiandcode.lcs;

import com.gopiandcode.lcs.dataset.BinaryClassifierDataset;
import com.gopiandcode.lcs.dataset.BinaryClassifierTestData;
import com.gopiandcode.lcs.graphing.GraphingLogger;
import com.gopiandcode.lcs.graphing.LoggableDataType;
import com.gopiandcode.lcs.problem.BinaryClassifier;
import com.gopiandcode.lcs.rcs.RCSBinaryClassifier;
import com.gopiandcode.lcs.rcs.RCSClassifier;

import java.util.Optional;

/**
 * Created by Gopiandcode on 10/01/2018.
 */
public class RCSBinaryClassifierTestRunner implements BinaryClassifierTestRunner{
    private final BinaryClassifierDataset testDataset;
    private final BinaryClassifierDataset trainDataset;
    private final RCSBinaryClassifier classifier;

    private Optional<GraphingLogger> logger = Optional.empty();
    private Optional<Integer> loggingFrequency = Optional.empty();
    private int trainIterationCount = 0;
    private int testIterationCount = 0;
    private boolean shouldReset = false;

    public RCSBinaryClassifierTestRunner(BinaryClassifierDataset train_dataset, BinaryClassifierDataset test_dataset, RCSBinaryClassifier classifier) {
        this.trainDataset = train_dataset;
        this.testDataset = test_dataset;
        this.classifier = classifier;
    }

    @Override
    public void setLogger(GraphingLogger logger, int loggingFrequency) {
        this.logger = Optional.of(logger);
        this.loggingFrequency = Optional.of(loggingFrequency);
    }

    @Override
    public void runTrainIterations(int count) {

        int i = 0;
        int correctlyPredicted = 0;
        int correctStart = 0;
        while(i < count) {
            if(!trainDataset.hasMoreData()) {
                if(shouldReset)
                    trainDataset.reset();
                else
                    break;
            }
            BinaryClassifierTestData binaryClassifierTestData = trainDataset.nextDataPoint();
            boolean predictedCorrectly = classifier.runSingleTrainIteration(binaryClassifierTestData);
            if(predictedCorrectly) {
                correctlyPredicted++;
            }
            if(loggingFrequency.isPresent() && logger.isPresent()) {
                if(i != 0 && (i % loggingFrequency.get()) == 0) {
                    double ratioCorrect = (double)correctlyPredicted/(i - correctStart);
                    logger.get().recordValueAtIteration(LoggableDataType.ACCURACY, ratioCorrect * 100, i);
                    logger.get().recordValueAtIteration(LoggableDataType.INTERMEDIATE_CLASSIFIER_COUNT, classifier.getIntermediateClassifierCount(), i);
                    logger.get().recordValueAtIteration(LoggableDataType.OUTPUT_CLASSIFIER_COUNT, classifier.getOutputClassifierCount(), i);
                    logger.get().recordValueAtIteration(LoggableDataType.POPULATION_SIZE, classifier.getPopulationSize(), i);
                    System.out.println("[" + i + "]: Accuracy over " + loggingFrequency.get() + " is " + ratioCorrect * 100 + "%" );
                    correctlyPredicted = 0;
                    correctStart = i;
                }
            }
            i++;
        }
    }

    @Override
    public double runTestIterations(int count) {
        int i = 0;
        int correctlyPredicted = 0;
        while(i < count) {
            if(!testDataset.hasMoreData()) {
                if(shouldReset)
                    testDataset.reset();
                else
                    break;
            }

            BinaryClassifierTestData binaryClassifierTestData = testDataset.nextDataPoint();
            boolean predictedCorreclty = classifier.classify(binaryClassifierTestData.getSituation()).equals(binaryClassifierTestData.getAction());

            if(predictedCorreclty) {
                correctlyPredicted++;
            }

            i++;
        }
        return (double)correctlyPredicted/(double)count;
    }

    @Override
    public void setShouldReset(boolean shouldReset) {
        this.shouldReset = shouldReset;
    }
}
