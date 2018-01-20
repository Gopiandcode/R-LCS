package com.gopiandcode.lcs;

import com.gopiandcode.lcs.graphing.GraphingLogger;

/**
 * Created by Gopiandcode on 10/01/2018.
 */
public interface BinaryClassifierTestRunner {
    void setLogger(GraphingLogger logger, int loggingFrequency);

    void runTrainIterations(int count);

    double runTestIterations(int count);

    void setShouldReset(boolean shouldReset);
}
