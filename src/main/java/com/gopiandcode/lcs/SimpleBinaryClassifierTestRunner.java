package com.gopiandcode.lcs;

import com.gopiandcode.lcs.dataset.BinaryClassifierDataset;
import com.gopiandcode.lcs.dataset.BinaryClassifierTestData;
import com.gopiandcode.lcs.logging.ClassifierTrainingLogger;
import com.gopiandcode.lcs.logging.graphing.GraphingLogger;
import com.gopiandcode.lcs.logging.graphing.LoggableDataType;
import com.gopiandcode.lcs.problem.BinaryClassifier;

import java.util.Optional;

public class SimpleBinaryClassifierTestRunner implements BinaryClassifierTestRunner {
    private final BinaryClassifierDataset testDataset;
    private final BinaryClassifierDataset trainDataset;
    private final BinaryClassifier classifier;

    private Optional<ClassifierTrainingLogger> logger = Optional.empty();
    private Optional<Integer> loggingFrequency = Optional.empty();
    private Optional<Integer> testLoggingFrequency = Optional.empty();
    private Optional<Integer> testLoggingSampleSize = Optional.empty();
    private Optional<ClassifierTrainingLogger> testLogger = Optional.empty();
    private int trainIterationCount = 0;
    private int testIterationCount = 0;
    private boolean shouldReset = false;

    public SimpleBinaryClassifierTestRunner(BinaryClassifierDataset train_dataset, BinaryClassifierDataset test_dataset, BinaryClassifier classifier) {
        this.trainDataset = train_dataset;
        this.testDataset = test_dataset;
        this.classifier = classifier;
    }

    public void setLogger(ClassifierTrainingLogger logger, int loggingFrequency) {
        this.logger = Optional.of(logger);
        this.loggingFrequency = Optional.of(loggingFrequency);
    }

    public void setTestLogger(ClassifierTrainingLogger logger, int loggingFrequency, int logSampleSize) {
        this.setTestLogger(logger);
        this.setTestLoggingFrequency(loggingFrequency);
        this.setTestLoggingSampleSize(logSampleSize);
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
                  logger.get().logAccuracyAtIteration( i, ratioCorrect * 100);
                  logger.get().logPopulationSizeAtIteration(i, classifier.getPopulationSize());
                    System.out.println("[" + i + "]: Train Accuracy over " + loggingFrequency.get() + " is " + ratioCorrect * 100 + "%" );
                  correctlyPredicted = 0;
                  correctStart = i;
                }
            }

              if(testLoggingFrequency.isPresent() && testLogger.isPresent() && testLoggingSampleSize.isPresent()) {
                if(i != 0 && (i % testLoggingFrequency.get()) == 0) {
                  double ratioCorrect = runTestIterations(testLoggingSampleSize.get());
                  testLogger.get().logAccuracyAtIteration( i, ratioCorrect * 100);
                  testLogger.get().logPopulationSizeAtIteration(i, classifier.getPopulationSize());
                    System.out.println("[" + i + "]: Test Accuracy over " + testLoggingFrequency.get() + " is " + ratioCorrect * 100 + "%" );
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


    private void setTestLogger(ClassifierTrainingLogger testLogger) {
        this.testLogger = Optional.of(testLogger);
    }

    private void setTestLoggingSampleSize(int testLoggingSampleSize) {
        this.testLoggingSampleSize = Optional.of(testLoggingSampleSize);
    }

    private void setTestLoggingFrequency(int testLoggingFrequency) {
        this.testLoggingFrequency = Optional.of(testLoggingFrequency);
    }
}
