package edu.xzit.core.core.common.utils.poi;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 将柱状图数据渲染为 PNG 图片字节流，用于插入 Word 等文档。
 * 使用图片替代原生图表可避免 MS Word 对 OOXML 图表的严格校验及 poi-tl 图表策略的 NPE。
 */
public final class ChartImageUtil {

    private static final int PADDING = 50;
    private static final int TITLE_HEIGHT = 30;
    private static final int LEGEND_HEIGHT = 24;
    private static final Color[] SERIES_COLORS = {
            new Color(66, 133, 244),
            new Color(234, 67, 53),
            new Color(251, 188, 5),
            new Color(52, 168, 83)
    };

    private ChartImageUtil() {
    }

    /**
     * 根据多系列柱状图数据生成 PNG 图片字节数组。
     *
     * @param title       图表标题
     * @param categories  分类轴标签
     * @param seriesNames 系列名称
     * @param seriesData  每个系列对应的数值，seriesData[i] 对应 seriesNames[i]
     * @param width       图片宽度
     * @param height      图片高度
     * @return PNG 字节数组
     */
    public static byte[] createBarChartPng(String title, String[] categories,
                                           String[] seriesNames, Double[][] seriesData,
                                           int width, int height) {
        if (categories == null || categories.length == 0 || seriesNames == null || seriesNames.length == 0
                || seriesData == null || seriesData.length != seriesNames.length) {
            throw new IllegalArgumentException("categories/seriesNames/seriesData 不能为空且长度需匹配");
        }
        int categoryCount = categories.length;
        for (Double[] row : seriesData) {
            if (row == null || row.length != categoryCount) {
                throw new IllegalArgumentException("每个系列的数值长度需与 categories 一致");
            }
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        int chartLeft = PADDING;
        int chartRight = width - PADDING;
        int chartTop = TITLE_HEIGHT + PADDING;
        int chartBottom = height - PADDING - LEGEND_HEIGHT;
        int chartWidth = chartRight - chartLeft;
        int chartHeight = chartBottom - chartTop;

        g.setColor(Color.BLACK);
        g.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        FontMetrics fmTitle = g.getFontMetrics();
        int titleX = (width - fmTitle.stringWidth(title)) / 2;
        g.drawString(title, titleX, TITLE_HEIGHT);

        double maxVal = 0;
        for (Double[] row : seriesData) {
            for (Double v : row) {
                if (v != null && v > maxVal) maxVal = v;
            }
        }
        if (maxVal <= 0) maxVal = 1;
        double maxAxis = Math.ceil(maxVal * 1.1);

        g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
        g.setColor(new Color(180, 180, 180));
        int yTickCount = 5;
        for (int i = 0; i <= yTickCount; i++) {
            int y = chartBottom - (chartHeight * i / yTickCount);
            g.drawLine(chartLeft, y, chartRight, y);
            String label = maxAxis <= 1.5 ? String.format("%.1f", maxAxis * i / yTickCount) : String.format("%.0f", maxAxis * i / yTickCount);
            g.setColor(Color.BLACK);
            g.drawString(label, chartLeft - 35, y + 4);
            g.setColor(new Color(180, 180, 180));
        }

        int seriesCount = seriesNames.length;
        int groupCount = categoryCount;
        double groupWidth = (double) chartWidth / groupCount;
        double barGap = groupWidth * 0.15;
        double barSlot = (groupWidth - barGap) / seriesCount;
        double barWidth = barSlot * 0.75;

        for (int gIdx = 0; gIdx < groupCount; gIdx++) {
            double groupStartX = chartLeft + gIdx * groupWidth + barGap / 2;
            for (int sIdx = 0; sIdx < seriesCount; sIdx++) {
                Double val = seriesData[sIdx][gIdx];
                if (val == null || val < 0) continue;
                double barH = (val / maxAxis) * chartHeight;
                double x = groupStartX + sIdx * barSlot;
                int x1 = (int) x;
                int barW = Math.max(2, (int) barWidth);
                int y1 = (int) (chartBottom - barH);
                int h = (int) barH;
                g.setColor(SERIES_COLORS[sIdx % SERIES_COLORS.length]);
                g.fillRect(x1, y1, barW, h);
                g.setColor(g.getColor().darker());
                g.drawRect(x1, y1, barW, h);
            }
        }

        g.setColor(Color.BLACK);
        g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
        FontMetrics fmX = g.getFontMetrics();
        for (int i = 0; i < categoryCount; i++) {
            String label = categories[i];
            if (label != null && !label.isEmpty()) {
                int cx = (int) (chartLeft + (i + 0.5) * groupWidth);
                int tw = fmX.stringWidth(label);
                g.drawString(label, cx - tw / 2, chartBottom + 20);
            }
        }

        int legendY = height - LEGEND_HEIGHT / 2;
        int legendX = chartLeft;
        for (int i = 0; i < seriesNames.length; i++) {
            int lx = legendX + i * 120;
            g.setColor(SERIES_COLORS[i % SERIES_COLORS.length]);
            g.fillRect(lx, legendY - 6, 12, 10);
            g.setColor(Color.BLACK);
            g.drawString(seriesNames[i], lx + 16, legendY + 2);
        }

        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("生成图表 PNG 失败", e);
        }
    }
}
