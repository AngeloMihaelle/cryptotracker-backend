package com.backend.handlers;

import com.backend.services.CoinGeckoService;
import com.sun.net.httpserver.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.ChartUtils;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;

import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Calendar;
import java.util.Date;

public class OverlayChartHandler implements HttpHandler {
    private final CoinGeckoService svc;

    public OverlayChartHandler(CoinGeckoService svc) {
        this.svc = svc;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        System.out.println("[INFO] /api/chart/overlay invoked: " + ex.getRequestURI());
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(405, -1);
            return;
        }

        Map<String, String> params = queryToMap(ex.getRequestURI().getQuery());
        String symbolsParam = params.get("symbols");
        String startParam = params.get("start");
        String endParam = params.get("end");
        if (symbolsParam == null || startParam == null || endParam == null) {
            System.out.println("[ERROR] Missing required params: symbols, start, end");
            ex.sendResponseHeaders(400, -1);
            return;
        }

        List<String> symbols = Arrays.asList(symbolsParam.split(","));
        System.out.println("[INFO] Symbols: " + symbols);

        Date startTime, endTime;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            startTime = sdf.parse(startParam);
            endTime = sdf.parse(endParam);
        } catch (Exception e) {
            System.out.println("[ERROR] Invalid time format: " + e.getMessage());
            ex.sendResponseHeaders(400, -1);
            return;
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        for (String sym : symbols) {
            List<List<Double>> history = svc.getPriceHistory(sym, 24);
            TimeSeries ts = new TimeSeries(sym.toUpperCase());
            for (List<Double> p : history) {
                long tsMs = p.get(0).longValue();
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(tsMs);
                int h = cal.get(Calendar.HOUR_OF_DAY), m = cal.get(Calendar.MINUTE);
                if ((h > startTime.getHours() || (h == startTime.getHours() && m >= startTime.getMinutes())) &&
                    (h < endTime.getHours()   || (h == endTime.getHours()   && m <= endTime.getMinutes()))) {
                    ts.addOrUpdate(new Minute(new Date(tsMs)), p.get(1));
                }
            }
            dataset.addSeries(ts);
            System.out.println("[INFO] Added series '" + sym + "' with " + ts.getItemCount() + " points");
        }

        // Create overlay chart using ChartBuilder
        JFreeChart chart = ChartBuilder.buildStyledOverlayChart(dataset, "Overlay Chart", "Hora", "USD", symbols.size());
        ex.getResponseHeaders().add("Content-Type", "image/png");
        ex.sendResponseHeaders(200, 0);
        try (OutputStream os = ex.getResponseBody()) {
            ChartUtils.writeChartAsPNG(os, chart, 1000, 600);
            System.out.println("[INFO] Overlay image sent.");
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to write PNG: " + e.getMessage());
        }
    }

    // Utilidad para parsing de query params
    private static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2) result.put(pair[0], pair[1]);
        }
        return result;
    }
}
