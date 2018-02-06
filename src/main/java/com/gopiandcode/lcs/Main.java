package com.gopiandcode.lcs;

import com.gopiandcode.lcs.dataset.BinaryClassifierDataset;
import com.gopiandcode.lcs.dataset.LocalBinaryClassifierDataset;
import com.gopiandcode.lcs.logging.csv.CSVTrainingLogger;
import com.gopiandcode.lcs.logging.graphing.*;
import com.gopiandcode.lcs.rcs.RCSBinaryClassifier;
import com.gopiandcode.lcs.xcs.XCSBinaryClassifier;
import org.jfree.chart.axis.NumberAxis;

import java.awt.*;
import java.io.FileNotFoundException;


public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        XCSBinaryClassifier xcs = new XCSBinaryClassifier();

        CSVTrainingLogger logger = new CSVTrainingLogger("testData6.csv");


        BinaryClassifierDataset testDataset = LocalBinaryClassifierDataset.loadFromFile("testData6.txt");
        BinaryClassifierDataset trainDataset = LocalBinaryClassifierDataset.loadFromFile("trainData6.txt");

        SimpleBinaryClassifierTestRunner runner = new SimpleBinaryClassifierTestRunner(trainDataset, testDataset, xcs);

        runner.setLogger(logger, 10);
        runner.setShouldReset(true);

        runner.runTrainIterations(5000);


        System.out.println("Final Accuracy: " + runner.runTestIterations(1000));

        logger.close();
    }

}
