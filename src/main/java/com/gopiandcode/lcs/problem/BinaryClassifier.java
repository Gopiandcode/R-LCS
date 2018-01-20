package com.gopiandcode.lcs.problem;

import com.gopiandcode.lcs.dataset.BinaryClassifierTestData;

public interface BinaryClassifier {
    boolean runSingleTrainIteration(BinaryClassifierTestData data);

    Action classify(Situation sigma);
}
