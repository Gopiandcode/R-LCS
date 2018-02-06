package com.gopiandcode.lcs;

import com.gopiandcode.lcs.logging.ClassifierTrainingLogger;
import com.gopiandcode.lcs.logging.graphing.GraphingLogger;

public interface BinaryClassifierTestRunner {

    void runTrainIterations(int count);

    double runTestIterations(int count);

    void setShouldReset(boolean shouldReset);
}
