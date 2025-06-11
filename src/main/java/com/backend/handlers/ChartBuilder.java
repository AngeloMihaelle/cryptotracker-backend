package com.backend.handlers;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;

import java.util.List;

public class ChartBuilder {

    /**
     * Construye un gráfico de línea simple a partir de una lista de precios históricos.
     *
     * @param data Lista con sublistas [timestamp (ms), price]
     * @param title Título del gráfico
     * @param xLabel Etiqueta del eje X
     * @param yLabel Etiqueta del eje Y
     * @return Objeto JFreeChart
     */
    public static JFreeChart buildLineChart(List<List<Double>> data, String title, String xLabel, String yLabel) {
        XYDataset dataset = createDataset(data);
        return ChartFactory.createTimeSeriesChart(
                title, xLabel, yLabel, dataset,
                false, // legend
                false, // tooltips
                false  // urls
        );
    }

    /**
     * Crea un dataset XY basado en datos de precios en el tiempo.
     */
    private static XYDataset createDataset(List<List<Double>> priceHistory) {
        TimeSeries series = new TimeSeries("Precio");

        for (List<Double> point : priceHistory) {
            long timestamp = point.get(0).longValue();
            double price = point.get(1);
            // Convertimos el timestamp en una fecha (con precisión de minutos)
            series.addOrUpdate(new Minute(new java.util.Date(timestamp)), price);
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);
        return dataset;
    }
}
