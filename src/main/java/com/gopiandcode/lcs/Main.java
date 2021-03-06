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

//        GraphingLogger logger = new AccuracyGraphingLogger("XCS Accuracy");
        CSVTrainingLogger logger = new CSVTrainingLogger("testData6.csv");


        BinaryClassifierDataset testDataset = LocalBinaryClassifierDataset.loadFromFile("testData6.txt");
        BinaryClassifierDataset trainDataset = LocalBinaryClassifierDataset.loadFromFile("trainData6.txt");

        SimpleBinaryClassifierTestRunner runner = new SimpleBinaryClassifierTestRunner(trainDataset, testDataset, xcs);

        runner.setLogger(logger, 100);
        runner.setShouldReset(true);

        runner.runTrainIterations(5000);


        System.out.println("Final Accuracy: " + runner.runTestIterations(1000));

        logger.close();
    }
//       RCSBinaryClassifier xcs = new RCSBinaryClassifier(3, 8);
//
//       // The graphing logger can customize graphs to only include the parameters you need - simply specify the members of the LoggableDataType enum you want in the final graph
//        GraphingLogger logger = new MultiGraphingLogger("RCSBinaryClassifier Accuracy over Training", LoggableDataType.ACCURACY, LoggableDataType.POPULATION_SIZE);
//
//        BinaryClassifierDataset testDataset = LocalBinaryClassifierDataset.loadFromFile("testData6.txt");
//        BinaryClassifierDataset trainDataset = LocalBinaryClassifierDataset.loadFromFile("trainData6.txt");
//
//        BinaryClassifierTestRunner runner = new RCSBinaryClassifierTestRunner(trainDataset, testDataset, xcs);
//
//        runner.setLogger(logger, 100);
//        runner.setShouldReset(true);
//
//        runner.runTrainIterations(5000);
//
//
//        configureGraph(xcs);
//
//        System.out.println("Final Accuracy: " + runner.runTestIterations(1000));
//
//        GraphRenderer renderer = new GraphRenderer(logger, 1280, 720,20);
//        renderer.save("result6_rcs_6.png");
//
//        new RCSGraphGenerator(xcs).writeToDot("./examples/result_graph.dot");
//        System.out.println("Finished");
//    }

    private static void configureGraph(RCSBinaryClassifier xcs) {
        NumberAxis axis = new NumberAxis();
        axis.setLabel("No of Population");
        axis.setRange(0, xcs.getPopulationSize());
        LoggableDataType.INTERMEDIATE_CLASSIFIER_COUNT.setRangeAxis(axis);
        LoggableDataType.INTERMEDIATE_CLASSIFIER_COUNT.setSeriesPaint(Color.GREEN);
        LoggableDataType.OUTPUT_CLASSIFIER_COUNT.setRangeAxis(axis);
        LoggableDataType.OUTPUT_CLASSIFIER_COUNT.setSeriesPaint(Color.YELLOW);
        LoggableDataType.POPULATION_SIZE.setRangeAxis(axis);
        LoggableDataType.POPULATION_SIZE.setSeriesPaint(Color.BLUE);
    }

}
