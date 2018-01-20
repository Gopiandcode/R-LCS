package com.gopiandcode.lcs.dataset;

public interface BinaryClassifierDataset {
    boolean hasMoreData();
    BinaryClassifierTestData nextDataPoint();
    void reset();
}
