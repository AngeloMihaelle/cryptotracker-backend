package com.backend.handlers;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Collections;
import java.util.List;

public class ChartBuilder {

    public static JFreeChart buildStyledLineChart(List<List<Double>> data, String title, String xLabel, String yLabel) {
        // Dataset
        TimeSeries series = new TimeSeries("Precio");
        for (List<Double> p : data) {
            long t = p.get(0).longValue();
            double price = p.get(1);
            series.addOrUpdate(new Millisecond(new java.util.Date(t)), price);
        }
        XYDataset dataset = new TimeSeriesCollection(series);

        // Crear gráfico
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title, xLabel, yLabel, dataset,
                false, false, false
        );

        XYPlot plot = chart.getXYPlot();

        // Renderer con puntos sin etiquetas
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
        renderer.setSeriesPaint(0, new Color(102, 255, 178));
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesShape(0, new Ellipse2D.Double(-1, -1, 2, 2));
        renderer.setDefaultItemLabelGenerator(new StandardXYItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(false); // Ocultar valores en puntos
        renderer.setDefaultItemLabelPaint(Color.WHITE);

        applyDarkTheme(chart, plot, renderer);
        return chart;
    }

    public static JFreeChart buildStyledOverlayChart(TimeSeriesCollection originalDataset, String title, String xLabel, String yLabel, int seriesCount) {
        // Normalizar cada serie dividiendo por el primer valor
        TimeSeriesCollection normalizedDataset = new TimeSeriesCollection();

        for (int i = 0; i < originalDataset.getSeriesCount(); i++) {
            TimeSeries original = originalDataset.getSeries(i);
            if (original.isEmpty()) continue;

            TimeSeries normalized = new TimeSeries(original.getKey());
            Number firstValue = original.getValue(0);

            if (firstValue == null || firstValue.doubleValue() == 0.0) continue;

            for (int j = 0; j < original.getItemCount(); j++) {
                Number value = original.getValue(j);
                if (value != null) {
                    double norm = value.doubleValue() / firstValue.doubleValue();
                    normalized.addOrUpdate(original.getTimePeriod(j), norm);
                }
            }

            normalizedDataset.addSeries(normalized);
        }

        JFreeChart chart = ChartFactory.createTimeSeriesChart(title, xLabel, yLabel, normalizedDataset, true, false, false);
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);

        for (int i = 0; i < seriesCount; i++) {
            renderer.setSeriesPaint(i, Color.getHSBColor((float) i / seriesCount, 0.8f, 1f));
            renderer.setSeriesStroke(i, new BasicStroke(2.5f));
            renderer.setSeriesShape(i, new Ellipse2D.Double(-1, -1, 2, 2));
        }

        applyDarkTheme(chart, plot, renderer);
        return chart;
    }

    private static void applyDarkTheme(JFreeChart chart, XYPlot plot, XYLineAndShapeRenderer renderer) {
        Color darkBg = new Color(13, 17, 23);
        Color gridColor = new Color(44, 50, 60);
        Color axisTextColor = Color.WHITE;

        // Tema visual
        StandardChartTheme theme = (StandardChartTheme) StandardChartTheme.createJFreeTheme();
        theme.setRegularFont(new Font("SansSerif", Font.PLAIN, 12));
        theme.setLargeFont(new Font("SansSerif", Font.PLAIN, 14));
        theme.setExtraLargeFont(new Font("SansSerif", Font.BOLD, 16));
        theme.setTitlePaint(Color.WHITE);
        theme.setPlotBackgroundPaint(darkBg);
        theme.setChartBackgroundPaint(darkBg);
        theme.setRangeGridlinePaint(gridColor);
        theme.apply(chart);

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(darkBg);
        plot.setDomainGridlinePaint(gridColor);
        plot.setRangeGridlinePaint(gridColor);
        plot.getDomainAxis().setTickLabelPaint(axisTextColor);
        plot.getRangeAxis().setTickLabelPaint(axisTextColor);
        plot.getDomainAxis().setLabelPaint(axisTextColor);
        plot.getRangeAxis().setLabelPaint(axisTextColor);

        chart.setBackgroundPaint(darkBg);
        if (chart.getTitle() != null) {
            chart.getTitle().setPaint(Color.WHITE);
        }

        chart.setAntiAlias(true);
    }
}
