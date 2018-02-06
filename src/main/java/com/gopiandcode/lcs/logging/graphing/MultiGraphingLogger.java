package com.gopiandcode.lcs.logging.graphing;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.stream.Collectors;


public class MultiGraphingLogger implements GraphingLogger {
    private HashMap<LoggableDataType, XYSeries> dataset;
    private String title;
    private int maxIteration = 0;

    public MultiGraphingLogger(String title, LoggableDataType... types) {
        dataset = new HashMap<>();
        for (LoggableDataType type : types) {
            dataset.put(type, new XYSeries(type.getDataName()));
        }
        this.title = title;
    }

    @Override
    public void recordValueAtIteration(LoggableDataType type, double value, int iteration) {
        if (dataset.containsKey(type)) {
            dataset.get(type).add(iteration, value);
            maxIteration = iteration;
        }
    }

    @Override
    public JFreeChart constructGraph(int ticks) {

        NumberAxis xAxis = new NumberAxis("Iteration Count");

        xAxis.setTickUnit(new NumberTickUnit(Math.max(Math.floor((double) maxIteration / ticks), 1)));
        NumberAxis yAxis = new NumberAxis("Value");

        XYPlot plot = new XYPlot();
        plot.setDomainAxis(xAxis);

        int rangeAxisIndex = 0;
        int dataTypeIndex = 0;
        Set<LoggableDataType> loggableDataTypes = this.dataset.keySet();
        Map<NumberAxis, List<LoggableDataType>> collect = loggableDataTypes.stream().collect(Collectors.groupingBy((LoggableDataType o) -> o.getRangeAxis()));
        for (NumberAxis rangeAxis : collect.keySet()) {
            plot.setRangeAxis(rangeAxisIndex, rangeAxis);
            for (LoggableDataType loggableDataType : collect.get(rangeAxis)) {
                XYSeriesCollection dataset = new XYSeriesCollection();
                dataset.addSeries(this.dataset.get(loggableDataType));
                plot.setDataset(dataTypeIndex, dataset);
                plot.mapDatasetToRangeAxes(dataTypeIndex, Collections.singletonList(rangeAxisIndex));
                XYSplineRenderer renderer = new XYSplineRenderer();
                renderer.setSeriesFillPaint(0, loggableDataType.getSeriesPaint());
                plot.setRenderer(dataTypeIndex, renderer);
                dataTypeIndex++;
            }
            rangeAxisIndex++;
        }
        plot.getRenderer().setBaseShape(new Ellipse2D.Double(0, 0, 1, 1));

        JFreeChart jFreeChart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        return jFreeChart;
    }


}
