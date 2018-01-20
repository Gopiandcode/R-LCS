package com.gopiandcode.lcs.problem;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static java.util.concurrent.ThreadLocalRandom.current;

public class Situation {
    private final BinaryAlphabet[] values;

    public BinaryAlphabet[] getValues() {
        return values;
    }

    public Situation(BinaryAlphabet[] values) {
        this.values = values;
    }

    public Situation(@NotNull String representation) {
        values = new BinaryAlphabet[representation.length()];
        for (int i = 0; i < representation.length(); i++) {
            switch (representation.charAt(i)) {
                case '0':
                    values[i] = BinaryAlphabet.ZERO;
                    break;
                case '1':
                    values[i] = BinaryAlphabet.ONE;
                    break;
                default:
                    throw new RuntimeException("Unknown character when constructing situation" + representation.charAt(i));
            }
        }
    }

    public static Situation createRandom(int size) {
        BinaryAlphabet[] binaryAlphabets = new BinaryAlphabet[size];
        for (int i = 0; i < size; i++) {
            BinaryAlphabet value;
            switch (current().nextInt(0, 2)) {
                case 0:
                    value = BinaryAlphabet.ZERO;
                    break;
                case 1:
                    value = BinaryAlphabet.ONE;
                    break;
                default:
                    throw new RuntimeException("Unknown random value recieved in creating XCSCondition");
            }
            binaryAlphabets[i] = value;
        }
        return new Situation(binaryAlphabets);
    }

    @NotNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (BinaryAlphabet situationValue : values) {
            sb.append(situationValue.toString());
        }

        return "Situation(" +
                  sb.toString() +
                ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Situation situation = (Situation) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(values, situation.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    public Situation copy() {
       return copy(this);
    }

    public static Situation copy(Situation situation) {
        BinaryAlphabet[] values = situation.getValues();
        return new Situation(Arrays.copyOf(values, values.length));
    }
}
