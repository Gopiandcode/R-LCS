package com.gopiandcode.lcs.dataset;

import com.gopiandcode.lcs.problem.Action;
import com.gopiandcode.lcs.problem.BinaryAlphabet;
import com.gopiandcode.lcs.problem.Situation;

public class GeneratedMultiplexerBinaryClassifierDataset implements BinaryClassifierDataset{
    private final int controlBits;
    private final int totalSize;

    public GeneratedMultiplexerBinaryClassifierDataset(int controlBits) {
        this.controlBits = controlBits;
        this.totalSize = (int) (controlBits + Math.pow(2, controlBits));
    }

    @Override
    public boolean hasMoreData() {
        return true;
    }

    @Override
    public BinaryClassifierTestData nextDataPoint() {
        Situation situation = Situation.createRandom(totalSize);
        BinaryAlphabet[] values = situation.getValues();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < controlBits; i++) {
            switch(values[i]) {
                case ONE:
                    sb.append("1");
                    break;
                case ZERO:
                    sb.append("0");
                    break;
            }
        }

        int position = Integer.parseInt(sb.toString(), 2);
        final Action action;
        switch (values[controlBits + position]) {
            case ONE:
                action = Action.getOneClassification();
                break;
            case ZERO:
                action = Action.getZeroClassification();
                break;
            default:
                throw new RuntimeException("Could not happen");
        }

        return new BinaryClassifierTestData(situation, action);
    }

    @Override
    public void reset() {
        throw new RuntimeException("Can not reset a generator");
    }
}
