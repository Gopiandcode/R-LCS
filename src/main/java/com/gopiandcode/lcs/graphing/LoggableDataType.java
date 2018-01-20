package com.gopiandcode.lcs.graphing;

import org.jfree.chart.axis.NumberAxis;

import java.awt.*;
import java.util.Optional;

public enum LoggableDataType {
    ACCURACY("Accuracy (%)", Constants.ACCURACY_AXIS, Color.RED),
    POPULATION_SIZE("Population size"),
    INTERMEDIATE_CLASSIFIER_COUNT("Intermediate Classifier Count"),
    OUTPUT_CLASSIFIER_COUNT("Output Classifier Count");

    private final String dataName;
    private Optional<NumberAxis> rangeAxis = Optional.empty();
    private  Optional<Paint> seriesPaint = Optional.empty();

    LoggableDataType(String dataName) {
        this.dataName = dataName;
    }
    LoggableDataType(String dataName, NumberAxis axis, Paint paint) {
        this.dataName = dataName;
        this.rangeAxis = Optional.of(axis);
        seriesPaint = Optional.of(paint);
    }

    public String getDataName() {
        return dataName;
    }

    public NumberAxis getRangeAxis() {
        return rangeAxis.get();
    }
    public void setRangeAxis(NumberAxis axis) {
        rangeAxis = Optional.of(axis);
    }

    public Paint getSeriesPaint() {
        return seriesPaint.get();
    }

    public void setSeriesPaint(Paint paint) {
        seriesPaint = Optional.of(paint);
    }

    private static class Constants {
        public static final NumberAxis ACCURACY_AXIS = new NumberAxis();
        static {
            ACCURACY_AXIS.setRange(0, 100);
            ACCURACY_AXIS.setLabel("Accuracy (%)");
        }
    }
}
