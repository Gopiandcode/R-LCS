package com.gopiandcode.lcs.graphing;

import org.jfree.chart.JFreeChart;

/**
 * Created by Gopiandcode on 10/01/2018.
 */
public interface GraphingLogger {
    void recordValueAtIteration(LoggableDataType type, double value, int iteration);

    JFreeChart constructGraph(int ticks);
}
