package com.gopiandcode.lcs.graphing;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class GraphRenderer extends JFrame {
    private final int WIDTH;
    private final int HEIGHT;
    private final JFreeChart chart;

    public GraphRenderer(GraphingLogger log, int width, int height, int ticks) {
        WIDTH = width;
        HEIGHT = height;
        chart = log.constructGraph(ticks);

        ChartPanel panel = new ChartPanel(decorateChart());
        panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.add(panel);
        this.pack();
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

       EventQueue.invokeLater(() -> this.setVisible(true));
    }

    public GraphRenderer(GraphingLogger log, int ticks) {
        this(log, 560, 367, ticks);
    }

    public void save(String filename, int width, int height) {
       File outputfile = new File(filename);
        try {
            ChartUtilities.saveChartAsPNG(outputfile, chart, width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void save(String filename) {
       save(filename, WIDTH, HEIGHT);
    }

    @NotNull
    private JFreeChart decorateChart() {
        XYSplineRenderer renderer = new XYSplineRenderer();
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesShapesVisible(0, false);
        chart.getXYPlot().setRenderer(renderer);
        return chart;
    }

}


