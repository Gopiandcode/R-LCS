package com.gopiandcode.lcs.xcs;

import com.gopiandcode.lcs.problem.BinaryAlphabet;
import com.gopiandcode.lcs.problem.Situation;
import com.gopiandcode.lcs.problem.TernaryAlphabet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static java.util.concurrent.ThreadLocalRandom.current;

public class XCSCondition {
    private final TernaryAlphabet[] values;

    public TernaryAlphabet[] getValues() {
        return values;
    }

    @NotNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (TernaryAlphabet value : values) {
           sb.append(value.toString());
        }
        return "XCSCondition(" +
                sb.toString() +
                ')';
    }

    XCSCondition(TernaryAlphabet[] values) {
        this.values = values;
    }

    XCSCondition(@NotNull String representation) {
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

    public boolean matches(@NotNull Situation situation) {
        BinaryAlphabet[] situationValues = situation.getValues();
        if (situationValues.length != this.values.length) {
            throw new RuntimeException("Situation does not match the size of the XCSCondition");
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

    public static XCSCondition createRandom(int size) {
        TernaryAlphabet[] ternaryAlphabets = new TernaryAlphabet[size];
        for (int i = 0; i < size; i++) {

            TernaryAlphabet value;
            switch (current().nextInt(0, 3)) {
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
                    throw new RuntimeException("Unknown random value recieved in creating XCSCondition");
            }
            ternaryAlphabets[i] = value;
        }
        return new XCSCondition(ternaryAlphabets);
    }

    public static XCSCondition createRandom(int size, double P_hash) {
        TernaryAlphabet[] ternaryAlphabets = new TernaryAlphabet[size];
        for (int i = 0; i < size; i++) {

            TernaryAlphabet value = TernaryAlphabet.HASH;
            if (current().nextDouble() > P_hash) {
                switch (current().nextInt(0, 2)) {
                    case 0:
                        value = TernaryAlphabet.ZERO;
                        break;
                    case 1:
                        value = TernaryAlphabet.ONE;
                        break;
                    default:
                        throw new RuntimeException("Unknown random value recieved in creating XCSCondition");
                }
            }
            ternaryAlphabets[i] = value;
        }
        return new XCSCondition(ternaryAlphabets);
    }

    public static XCSCondition createCovering(@NotNull Situation sigma, double p_hash) {
        BinaryAlphabet[] sigmaValues = sigma.getValues();
        TernaryAlphabet[] values = new TernaryAlphabet[sigmaValues.length];

        for (int i = 0; i < values.length; i++) {
            if (current().nextDouble() < p_hash) {
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
        }
        return new XCSCondition(values);
    }

    public static XCSCondition copy(@NotNull XCSCondition condition) {
       return new XCSCondition(Arrays.copyOf(condition.getValues(), condition.getValues().length));
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XCSCondition condition = (XCSCondition) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if(values.length != condition.values.length)
            return false;
        for(int i = 0; i < values.length; i++) {
            if(values[i] != condition.values[i])
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    public void setValues(int i, TernaryAlphabet value) {
       values[i] = value;
    }
}
