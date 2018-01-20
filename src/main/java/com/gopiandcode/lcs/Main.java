package com.gopiandcode.lcs;

import com.gopiandcode.lcs.dataset.BinaryClassifierDataset;
import com.gopiandcode.lcs.dataset.LocalBinaryClassifierDataset;
import com.gopiandcode.lcs.graphing.*;
import com.gopiandcode.lcs.rcs.RCSBinaryClassifier;
import com.gopiandcode.lcs.xcs.XCSBinaryClassifier;
import org.jfree.chart.axis.NumberAxis;

import java.awt.*;
import java.io.FileNotFoundException;


public class Main {
    public static void main(String[] args) throws FileNotFoundException {
       RCSBinaryClassifier xcs = new RCSBinaryClassifier(3, 8);
       xcs.setN(100);

        NumberAxis axis = new NumberAxis();
      GraphingLogger logger = new MultiGraphingLogger("RCSBinaryClassifier Accuracy over Training Iterations", LoggableDataType.ACCURACY, LoggableDataType.INTERMEDIATE_CLASSIFIER_COUNT, LoggableDataType.OUTPUT_CLASSIFIER_COUNT, LoggableDataType.POPULATION_SIZE);
//        setXCSParameters(xcs);
        BinaryClassifierDataset testDataset = LocalBinaryClassifierDataset.loadFromFile("testData6.txt");
        BinaryClassifierDataset trainDataset = LocalBinaryClassifierDataset.loadFromFile("trainData6.txt");

        BinaryClassifierTestRunner runner = new RCSBinaryClassifierTestRunner(trainDataset, testDataset, xcs);

        runner.setLogger(logger, 100);
        runner.setShouldReset(true);

//        runner.runTrainIterations(50000);
        runner.runTrainIterations(5000);


        axis.setLabel("No of Population");
        axis.setRange(0, xcs.getPopulationSize());
        LoggableDataType.INTERMEDIATE_CLASSIFIER_COUNT.setRangeAxis(axis);
        LoggableDataType.INTERMEDIATE_CLASSIFIER_COUNT.setSeriesPaint(Color.GREEN);
        LoggableDataType.OUTPUT_CLASSIFIER_COUNT.setRangeAxis(axis);
        LoggableDataType.OUTPUT_CLASSIFIER_COUNT.setSeriesPaint(Color.YELLOW);
        LoggableDataType.POPULATION_SIZE.setRangeAxis(axis);
        LoggableDataType.POPULATION_SIZE.setSeriesPaint(Color.BLUE);



        System.out.println("Final Accuracy: " + runner.runTestIterations(1000));
        GraphRenderer renderer = new GraphRenderer(logger, 1280, 720,20);
        renderer.save("result6_rcs_6.png");

            new RCSGraphGenerator(xcs).writeToDot("./examples/result_graph.dot");
        System.out.println("Finished");


    }

    private static void setXCSParameters(XCSBinaryClassifier xcs) {
//        xcs.setMew(0.001);
//        xcs.setTheta_GA(10000);
//        xcs.setN(20000);
        xcs.setDoGASubsumption(false);
        xcs.setDoActionSetSubsumption(false);
    }
}
