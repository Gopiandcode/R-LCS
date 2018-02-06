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
    static void configureGraphParams(int iterationCount) {
        NumberAxis axis = new NumberAxis();
        axis.setRange(0, iterationCount);
        LoggableDataType.ACCURACY.setRangeAxis(axis);
    }

    static void configureXCSParamters(XCSBinaryClassifier xcs) {
       xcs.setP_explr(0.01);
       xcs.setGamma(0.1);
        xcs.setMew(0.05);
        xcs.setN(10000);
    }
    public static void main(String[] args) throws FileNotFoundException {
        XCSBinaryClassifier xcs = new XCSBinaryClassifier();
        configureXCSParamters(xcs);

        CSVTrainingLogger testlogger = new CSVTrainingLogger("testData20.csv");
        GraphingLogger logger = new AccuracyGraphingLogger("XCS Train Accuracy");


        BinaryClassifierDataset testDataset = LocalBinaryClassifierDataset.loadFromFile("testData20.txt");
        BinaryClassifierDataset trainDataset = LocalBinaryClassifierDataset.loadFromFile("trainData20.txt");
        SimpleBinaryClassifierTestRunner runner = new SimpleBinaryClassifierTestRunner(trainDataset, testDataset, xcs);

        runner.setLogger(new ClassifierTrainingLoggerAdapter(logger), 1000);
        runner.setTestLogger(testlogger, 1000, 1000);


        runner.setShouldReset(true);
        configureGraphParams(100000);
        runner.runTrainIterations(100000);


        System.out.println("Final Accuracy: " + runner.runTestIterations(1000));
        new GraphRenderer(logger, 1080, 720, 1720);

        testlogger.close();
    }

}
