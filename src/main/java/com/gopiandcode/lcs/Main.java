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
//       rcs.setRewardForCorrectClassification(100);
        rcs.setMew(0.05);
        rcs.setN(100);
//        rcs.setTheta_mna(1.0);
        rcs.setX(0.1);
    }
    public static void main(String[] args) throws FileNotFoundException {
        RCSBinaryClassifier rcs = new RCSBinaryClassifier(3, 8);
        configureXCSParamters(rcs);

        GraphingLogger testlogger = new AccuracyGraphingLogger("XCS Test Accuracy");
//        CSVRCSTrainingLogger testlogger = new CSVRCSTrainingLogger("rcsBasicData20.csv");
        GraphingLogger logger = new AccuracyGraphingLogger("final constrained XCS Train Accuracy");


        BinaryClassifierDataset testDataset = LocalBinaryClassifierDataset.loadFromFile("testData6.txt");
        BinaryClassifierDataset trainDataset = LocalBinaryClassifierDataset.loadFromFile("trainData6.txt");
        RCSBinaryClassifierTestRunner runner = new RCSBinaryClassifierTestRunner(trainDataset, testDataset, rcs);

        runner.setLogger(new ClassifierTrainingLoggerAdapter(logger), 1000);
//        runner.setTestLogger(testlogger, 1000, 100);
        runner.setTestLogger(new ClassifierTrainingLoggerAdapter(testlogger), 10000, 1000);


        runner.setShouldReset(true);
        configureGraphParams(100000);
        runner.runTrainIterations(100000);


        System.out.println("Final Accuracy: " + runner.runTestIterations(1000));
        new GraphRenderer(logger, 1080, 720, 1720);
        new GraphRenderer(testlogger, 1080, 720, 1720);

//        testlogger.close();
    }

}
