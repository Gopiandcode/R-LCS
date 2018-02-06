package com.gopiandcode.lcs.logging.csv;

import com.gopiandcode.lcs.logging.RCSClassifierTrainingLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Optional;

public class CSVRCSTrainingLogger implements RCSClassifierTrainingLogger {
    private PrintWriter writer;
    private int current_iteration = 0;

    private Optional<Double> currentAccuracy = Optional.empty();
    private Optional<Double> currentPopulationSize = Optional.empty();
    private Optional<Double> currentIntermediateClassifierCount = Optional.empty();
    private Optional<Double> currentOutputClassifierCount = Optional.empty();

    public CSVRCSTrainingLogger(String filename) throws FileNotFoundException {
        writer = new PrintWriter(new FileOutputStream(filename), true);
        printHeader();
    }

    private void printHeader() {
       writer.print("iteration,accuracy,population size,intermediate classifier count, output classifier count,\n");
    }

    private void printLine() {
        String accuracy = currentAccuracy.map(Object::toString).orElse("nan");
        String populationSize = currentPopulationSize.map(Object::toString).orElse("nan");
        String intermediateClassifierCount = currentIntermediateClassifierCount.map(Object::toString).orElse("nan");
        String outputClassifierCount = currentOutputClassifierCount.map(Object::toString).orElse("nan");
        writer.printf("%d,%s,%s,%s,%s,\n", current_iteration, accuracy, populationSize, intermediateClassifierCount, outputClassifierCount);

        currentAccuracy = Optional.empty();
        currentPopulationSize= Optional.empty();
        currentIntermediateClassifierCount = Optional.empty();
        currentOutputClassifierCount = Optional.empty();
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

    @Override
    public void logIntermediateClassifierCountAtIteration(int iteration, double intermediateClassifierCount) {
        if(current_iteration != iteration || current_iteration == 0)  {
            printLine();
            current_iteration = iteration;
        }
        currentIntermediateClassifierCount = Optional.of(intermediateClassifierCount);
    }

    @Override
    public void logOutputClassifierCountAtIteration(int iteration, double intermediateClassifierCount) {
        if(current_iteration != iteration || current_iteration == 0)  {
            printLine();
            current_iteration = iteration;
        }
        currentOutputClassifierCount = Optional.of(intermediateClassifierCount);
    }


}
