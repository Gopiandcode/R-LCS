package com.gopiandcode.lcs.logging.csv;


import com.gopiandcode.lcs.logging.ClassifierTrainingLogger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Optional;

public class CSVTrainingLogger implements ClassifierTrainingLogger {
    private PrintWriter writer;
    private int current_iteration = 0;

    private Optional<Double> currentAccuracy = Optional.empty();
    private Optional<Double> currentPopulationSize = Optional.empty();

    public CSVTrainingLogger(String filename) throws FileNotFoundException {
        writer = new PrintWriter(new FileOutputStream(filename), true);
        printHeader();
    }

    private void printHeader() {
        writer.print("iteration,accuracy,population size,\n");
    }

    private void printLine() {
        String accuracy = currentAccuracy.map(Object::toString).orElse("nan");
        String populationSize = currentPopulationSize.map(Object::toString).orElse("nan");
        writer.printf("%d,%s,%s,\n", current_iteration, accuracy, populationSize);

        currentAccuracy = Optional.empty();
        currentPopulationSize= Optional.empty();
    }

    public void close() {
        writer.close();
    }


    @Override
    public void logAccuracyAtIteration(int iteration, double accuracy) {
        if(current_iteration != iteration || current_iteration == 0)  {
            printLine();
            current_iteration = iteration;
        }
        currentAccuracy = Optional.of(accuracy);
    }

    @Override
    public void logPopulationSizeAtIteration(int iteration, double populationSize) {
        if(current_iteration != iteration || current_iteration == 0)  {
            printLine();
            current_iteration = iteration;
        }
        currentPopulationSize = Optional.of(populationSize);
    }

}
