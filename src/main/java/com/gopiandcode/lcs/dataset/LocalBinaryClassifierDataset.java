package com.gopiandcode.lcs.dataset;

import com.gopiandcode.lcs.problem.Action;
import com.gopiandcode.lcs.problem.Situation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LocalBinaryClassifierDataset implements BinaryClassifierDataset {

    private List<BinaryClassifierTestData> testData;
    private int currentIndex = 0;

    public static LocalBinaryClassifierDataset loadFromFile(String filename) throws FileNotFoundException {
        return new LocalBinaryClassifierDataset(
                deserialize(new Scanner(new BufferedReader(new FileReader(new File(filename)))))
        );
    }

    private static List<BinaryClassifierTestData> deserialize(Scanner sc) {
        List<BinaryClassifierTestData> result = new ArrayList<>();
        int controlBits = Integer.parseInt(sc.nextLine().split(":")[1].replace(" ", ""));
        int totalSize = Integer.parseInt(sc.nextLine().split(":")[1].replace(" ", ""));
        if(totalSize !=  controlBits  + Math.pow(2, controlBits)) {
            throw new IllegalArgumentException("Control bits and total size does not match up");
        }
        int count = Integer.parseInt(sc.nextLine().split(":")[1].replace(" ", ""));

        for(int i = 0; i < count; i++) {
            String[] split = sc.nextLine().split(" ");
            Situation situation = new Situation(split[0].replace("\\w", ""));
            Action action = new Action(split[1]);
            if(situation.getValues().length != totalSize) {
                throw new IllegalArgumentException("Input file format not correct");
            }
            result.add(new BinaryClassifierTestData(situation, action));
        }
        return result;
    }


    public static void main(String[] args) {
        List<BinaryClassifierTestData> deserialize = LocalBinaryClassifierDataset.deserialize(new Scanner("controlBits: 3\n"
                                                                                                          + "totalSize: 11\n"
                                                                                                          + "count: 1\n"
                                                                                                          + "00111101000 1"));
        System.out.println(deserialize);
    }

    private LocalBinaryClassifierDataset(List<BinaryClassifierTestData> testData) {
        this.testData = testData;
    }

    @Override
    public boolean hasMoreData() {
        return currentIndex < testData.size();
    }

    @Override
    public BinaryClassifierTestData nextDataPoint() {
        return testData.get(currentIndex++);
    }

    public void reset() {
        currentIndex = 0;
    }
}
