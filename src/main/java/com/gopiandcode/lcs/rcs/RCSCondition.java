package com.gopiandcode.lcs.rcs;

import com.gopiandcode.lcs.problem.BinaryAlphabet;
import com.gopiandcode.lcs.problem.Situation;
import com.gopiandcode.lcs.problem.TernaryAlphabet;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class RCSCondition {
    private final TernaryAlphabet[] values;

    public RCSCondition(TernaryAlphabet[] values) {
        this.values = values;
    }

    public TernaryAlphabet[] getValues() {
        return values;
    }


    public RCSCondition(String representation) {
        values = new TernaryAlphabet[representation.length()];
        for (int i = 0; i < representation.length(); i++) {
            switch (representation.charAt(i)) {
                case '0':
                    values[i] = TernaryAlphabet.ZERO;
                    break;
                case '1':
                    values[i] = TernaryAlphabet.ONE;
                    break;
                case '#':
                    values[i] = TernaryAlphabet.HASH;
                    break;
                default:
                    throw new RuntimeException("Unknown character when constructing condition" + representation.charAt(i));
            }
        }
    }


    public static RCSCondition createRandom(int size) {
        TernaryAlphabet[] ternaryAlphabets = new TernaryAlphabet[size];
        for (int i = 0; i < size; i++) {

            TernaryAlphabet value;
            switch (ThreadLocalRandom.current().nextInt(0, 3)) {
                case 0:
                    value = TernaryAlphabet.ZERO;
                    break;
                case 1:
                    value = TernaryAlphabet.ONE;
                    break;
                case 2:
                    value = TernaryAlphabet.HASH;
                    break;
                default:
                    throw new RuntimeException("Unknown random value recieved in creating RCSCondition");
            }
            ternaryAlphabets[i] = value;
        }
        return new RCSCondition(ternaryAlphabets);
    }

    public static RCSCondition createRandom(int size, double P_hash) {
        TernaryAlphabet[] ternaryAlphabets = new TernaryAlphabet[size];
        for (int i = 0; i < size; i++) {

            TernaryAlphabet value = TernaryAlphabet.HASH;
            if (ThreadLocalRandom.current().nextDouble() > P_hash) {
                switch (ThreadLocalRandom.current().nextInt(0, 2)) {
                    case 0:
                        value = TernaryAlphabet.ZERO;
                        break;
                    case 1:
                        value = TernaryAlphabet.ONE;
                        break;
                    default:
                        throw new RuntimeException("Unknown random value recieved in creating RCSCondition");
                }
            }
            ternaryAlphabets[i] = value;
        }
        return new RCSCondition(ternaryAlphabets);
    }

    public static RCSCondition createCovering(Situation sigma, int size, double p_hash) {
        BinaryAlphabet[] sigmaValues = sigma.getValues();
        TernaryAlphabet[] values = new TernaryAlphabet[size];

        for (int i = 0; i < values.length; i++) {
            if (i < sigmaValues.length) {
                if (ThreadLocalRandom.current().nextDouble() < p_hash) {
                    // x <- #
                    values[i] = TernaryAlphabet.HASH;
                } else {
                    // x <- the corresponding value in sigmah
                    switch (sigmaValues[i]) {
                        case ONE:
                            values[i] = TernaryAlphabet.ONE;
                            break;
                        case ZERO:
                            values[i] = TernaryAlphabet.ZERO;
                            break;
                    }
                }
            } else {
                TernaryAlphabet value = TernaryAlphabet.HASH;
                if (ThreadLocalRandom.current().nextDouble() > p_hash) {
                    switch (ThreadLocalRandom.current().nextInt(0, 2)) {
                        case 0:
                            value = TernaryAlphabet.ZERO;
                            break;
                        case 1:
                            value = TernaryAlphabet.ONE;
                            break;
                        default:
                            throw new RuntimeException("Unknown random value recieved in creating RCSCondition");
                    }
                }
                values[i] = value;
            }
        }

        return new RCSCondition(values);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (TernaryAlphabet value : values) {
            sb.append(value.toString());
        }
        return "RCSCondition(" +
               sb.toString() +
               ')';
    }

     public boolean matches(Situation situation) {
        BinaryAlphabet[] situationValues = situation.getValues();
        if (situationValues.length > this.values.length) {
            throw new RuntimeException("Situation is longer than RCS condition");
        }
        for (int i = 0; i < situationValues.length; i++) {
            TernaryAlphabet value = this.values[i];
            if (value != TernaryAlphabet.HASH) {
                switch (situationValues[i]) {
                    case ONE:
                        if (value != TernaryAlphabet.ONE) return false;
                        break;
                    case ZERO:
                        if (value != TernaryAlphabet.ZERO) return false;
                        break;
                }
            }
        }

        return true;
    }




    public long hashCount() {
        return Arrays.stream(values).filter(ternaryAlphabet -> ternaryAlphabet == TernaryAlphabet.HASH).map(ternaryAlphabet -> 1).reduce(Integer::sum).orElse(0);
    }


    public static RCSCondition copy(RCSCondition condition) {
        return new RCSCondition(Arrays.copyOf(condition.getValues(), condition.getValues().length));
    }

    public void setValues(int i, TernaryAlphabet value) {
        values[i] = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RCSCondition that = (RCSCondition) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }
}

