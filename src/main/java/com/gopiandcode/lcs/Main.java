package com.gopiandcode.lcs;

import com.gopiandcode.lcs.dataset.BinaryClassifierDataset;
import com.gopiandcode.lcs.dataset.LocalBinaryClassifierDataset;
import com.gopiandcode.lcs.logging.RCSClassifierTrainingLogger;
import com.gopiandcode.lcs.logging.csv.CSVRCSTrainingLogger;
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

    static void configureXCSParamters(RCSBinaryClassifier rcs) {
       rcs.setP_explr(0.01);
       rcs.setGamma(0.1);
        rcs.setMew(0.05);
        rcs.setN(10000);
    }
    public static void main(String[] args) throws FileNotFoundException {
        RCSBinaryClassifier rcs = new RCSBinaryClassifier(5, 22);
        configureXCSParamters(rcs);

        CSVRCSTrainingLogger testlogger = new CSVRCSTrainingLogger("rcsBasicData20.csv");
        GraphingLogger logger = new AccuracyGraphingLogger("final constrained XCS Train Accuracy");


        BinaryClassifierDataset testDataset = LocalBinaryClassifierDataset.loadFromFile("testData20.txt");
        BinaryClassifierDataset trainDataset = LocalBinaryClassifierDataset.loadFromFile("trainData20.txt");
        RCSBinaryClassifierTestRunner runner = new RCSBinaryClassifierTestRunner(trainDataset, testDataset, rcs);

        runner.setLogger(new ClassifierTrainingLoggerAdapter(logger), 100);
        runner.setTestLogger(testlogger, 1000, 100);


        runner.setShouldReset(true);
        configureGraphParams(50000);
        runner.runTrainIterations(50000);


        System.out.println("Final Accuracy: " + runner.runTestIterations(1000));
        new GraphRenderer(logger, 1080, 720, 1720);

        testlogger.close();
    }

}
