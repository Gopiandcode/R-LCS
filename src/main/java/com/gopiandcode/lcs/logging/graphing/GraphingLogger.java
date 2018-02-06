package com.gopiandcode.lcs.logging.graphing;

import org.jfree.chart.JFreeChart;

public interface GraphingLogger {
    void recordValueAtIteration(LoggableDataType type, double value, int iteration);

    JFreeChart constructGraph(int ticks);
}
