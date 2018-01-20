package com.gopiandcode.lcs.graphing;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.geom.Ellipse2D;

public class AccuracyGraphingLogger implements GraphingLogger {
    private XYSeries dataset;
    private String title;
    private int maxIteration = 0;
    public AccuracyGraphingLogger(String title) {
       dataset = new XYSeries("accuracy");
       this.title = title;
    }

    @Override
    public void recordValueAtIteration(LoggableDataType type, double value, int iteration) {
        if(type == LoggableDataType.ACCURACY) {
            dataset.add(iteration, value);
            maxIteration = iteration;
        }
    }

    @Override
    public JFreeChart constructGraph(int ticks) {
//       JFreeChart chart = ChartFactory.createXYLineChart(
//               title,
//               "Iteration (count)",
//               "Accuracy (%)",
//               new XYSeriesCollection(dataset),
//               PlotOrientation.VERTICAL,
//               false,
//               false,
//              false
//               );
//        XYPlot xyPlot = chart.getXYPlot();
//        XYSplineRenderer renderer = new XYSplineRenderer();
//        renderer.setSeriesShapesVisible(0, false);
//        xyPlot.setRenderer(renderer);
//
//
//        NumberAxis domainAxis = (NumberAxis) xyPlot.getDomainAxis();
//        domainAxis.setTickUnit(new NumberTickUnit(Math.max(Math.floor((double)maxIteration/ticks), 1)));
//        return chart;


        XYSplineRenderer renderer = new XYSplineRenderer();
        NumberAxis xAxis = new NumberAxis("Iteration Count");
        xAxis.setTickUnit(new NumberTickUnit(Math.max(Math.floor((double)maxIteration/ticks), 1)));
        NumberAxis yAxis = new NumberAxis("Accuracy (%)");
        yAxis.setRange(0, 100);

        XYSeriesCollection dataset = new XYSeriesCollection(this.dataset);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.getRenderer().setBaseShape(new Ellipse2D.Double(0,0,1,1));

        JFreeChart jFreeChart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        return jFreeChart;
    }


}
