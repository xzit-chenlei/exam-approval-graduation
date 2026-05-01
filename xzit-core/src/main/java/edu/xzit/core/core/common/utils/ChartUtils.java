package edu.xzit.core.core.common.utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.text.DecimalFormat;

/**
 * 图表相关工具类，全平台中文不乱码版
 */
public class ChartUtils {

    private static String NO_DATA_MSG = "暂无数据";
    
    private static final String FONT_NAME = Font.SANS_SERIF;
    private static Font FONT = new Font(FONT_NAME, Font.PLAIN, 20);

    public static Color[] CHART_COLORS = {
            new Color(143, 143, 143),
            new Color(104, 104, 104),
            new Color(43, 43, 43),
            new Color(0, 0, 0),
            new Color(153, 158, 255),
            new Color(255, 117, 153),
            new Color(253, 236, 109),
            new Color(128, 133, 232),
            new Color(158, 90, 102),
            new Color(255, 204, 102)
    };

    static {
        setChartTheme();
    }

    public static StandardChartTheme createChartTheme(String fontName) {
        StandardChartTheme theme = new StandardChartTheme("unicode") {
            public void apply(JFreeChart chart) {
                chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                super.apply(chart);
            }
        };
        theme.setExtraLargeFont(getDefaultFont(Font.PLAIN, 20f));
        theme.setLargeFont(getDefaultFont(Font.PLAIN, 14f));
        theme.setRegularFont(getDefaultFont(Font.PLAIN, 12f));
        theme.setSmallFont(getDefaultFont(Font.PLAIN, 10f));
        return theme;
    }

    public static Font getDefaultFont(int style, Float size) {
        return new Font(FONT_NAME, Font.BOLD, size.intValue());
    }

    public static void setChartTheme() {
        StandardChartTheme chartTheme = new StandardChartTheme("CN");

        chartTheme.setExtraLargeFont(FONT);
        chartTheme.setRegularFont(FONT);
        chartTheme.setLargeFont(FONT);
        chartTheme.setSmallFont(FONT);

        chartTheme.setTitlePaint(new Color(51, 51, 51));
        chartTheme.setSubtitlePaint(new Color(85, 85, 85));

        chartTheme.setLegendBackgroundPaint(Color.WHITE);
        chartTheme.setLegendItemPaint(Color.BLACK);
        chartTheme.setChartBackgroundPaint(Color.WHITE);

        Paint[] OUTLINE_PAINT_SEQUENCE = new Paint[]{Color.WHITE};
        DefaultDrawingSupplier drawingSupplier = new DefaultDrawingSupplier(CHART_COLORS, CHART_COLORS, OUTLINE_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE, DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE);
        chartTheme.setDrawingSupplier(drawingSupplier);

        chartTheme.setPlotBackgroundPaint(Color.WHITE);
        chartTheme.setPlotOutlinePaint(Color.WHITE);
        chartTheme.setLabelLinkPaint(new Color(8, 55, 114));
        chartTheme.setLabelLinkStyle(PieLabelLinkStyle.CUBIC_CURVE);

        chartTheme.setAxisOffset(new RectangleInsets(5, 12, 5, 12));
        chartTheme.setDomainGridlinePaint(new Color(192, 208, 224));
        chartTheme.setRangeGridlinePaint(new Color(192, 192, 192));

        chartTheme.setBaselinePaint(Color.WHITE);
        chartTheme.setCrosshairPaint(Color.BLUE);
        chartTheme.setAxisLabelPaint(new Color(51, 51, 51));
        chartTheme.setTickLabelPaint(new Color(67, 67, 72));
        chartTheme.setBarPainter(new StandardBarPainter());
        chartTheme.setXYBarPainter(new StandardXYBarPainter());

        chartTheme.setItemLabelPaint(Color.black);
        chartTheme.setThermometerPaint(Color.white);

        ChartFactory.setChartTheme(chartTheme);
    }

    public static JFreeChart genMultiSeriesChart(String title, DefaultCategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createBarChart(
                title, "", "", dataset,
                PlotOrientation.VERTICAL, true, true, false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        for (int i = 0; i < dataset.getRowCount(); i++) {
            renderer.setSeriesPaint(i, CHART_COLORS[i % CHART_COLORS.length]);
        }
        renderer.setMaximumBarWidth(0.15);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator(
                StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING,
                new DecimalFormat("0.000")
        ));

        renderer.setDefaultItemLabelFont(new Font(FONT_NAME, Font.BOLD, 20));
        renderer.setDefaultItemLabelPaint(Color.BLACK);

        TextTitle textTitle = chart.getTitle();
        if (textTitle != null) {
            textTitle.setFont(new Font(FONT_NAME, Font.PLAIN, 40));
        }

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLabelFont(new Font(FONT_NAME, Font.PLAIN, 22));
        domainAxis.setTickLabelFont(new Font(FONT_NAME, Font.PLAIN, 22));

        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font(FONT_NAME, Font.PLAIN, 22));

        if (rangeAxis instanceof NumberAxis) {
            NumberAxis numberAxis = (NumberAxis) rangeAxis;
            numberAxis.setUpperMargin(0.1);
            numberAxis.setAutoRangeIncludesZero(true);
            numberAxis.setLowerBound(0);
        }

        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setItemFont(new Font(FONT_NAME, Font.PLAIN, 22));
        }

        return chart;
    }

    private static double getMaxDataValue(JFreeChart chart) {
        double maxValue = 0.0;
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        CategoryDataset dataset = plot.getDataset();

        for (int row = 0; row < dataset.getRowCount(); row++) {
            for (int col = 0; col < dataset.getColumnCount(); col++) {
                Number value = dataset.getValue(row, col);
                if (value != null) {
                    maxValue = Math.max(maxValue, value.doubleValue());
                }
            }
        }
        return maxValue;
    }
}
