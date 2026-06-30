package com.example.nuvola.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportChartView extends View {

    private final Paint gridPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint axisPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint linePaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint fillPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint pointPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint textPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final List<String> labels =
            new ArrayList<>();

    private final List<Double> values =
            new ArrayList<>();

    private String title = "";

    private int chartColor =
            Color.rgb(33, 76, 120);

    public ReportChartView(Context context) {
        super(context);
        initialize();
    }

    public ReportChartView(
            Context context,
            @Nullable AttributeSet attrs
    ) {
        super(context, attrs);
        initialize();
    }

    public ReportChartView(
            Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr
    ) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        setMinimumHeight(
                (int) dp(250)
        );

        setLayerType(
                View.LAYER_TYPE_SOFTWARE,
                null
        );

        gridPaint.setStyle(
                Paint.Style.STROKE
        );

        axisPaint.setStyle(
                Paint.Style.STROKE
        );

        linePaint.setStyle(
                Paint.Style.STROKE
        );

        linePaint.setStrokeWidth(
                dp(2.6f)
        );

        linePaint.setStrokeCap(
                Paint.Cap.ROUND
        );

        linePaint.setStrokeJoin(
                Paint.Join.ROUND
        );

        fillPaint.setStyle(
                Paint.Style.FILL
        );

        pointPaint.setStyle(
                Paint.Style.FILL
        );
    }

    public void setData(
            String title,
            List<String> labels,
            List<Double> values
    ) {
        this.title =
                title == null
                        ? ""
                        : title;

        this.labels.clear();
        this.values.clear();

        if (labels != null) {
            this.labels.addAll(labels);
        }

        if (values != null) {
            this.values.addAll(values);
        }

        invalidate();
    }

    public void setBarColor(
            int color
    ) {
        this.chartColor = color;
        invalidate();
    }

    public void setChartColor(
            int color
    ) {
        this.chartColor = color;
        invalidate();
    }

    @Override
    protected void onDraw(
            Canvas canvas
    ) {
        super.onDraw(canvas);

        float width =
                getWidth();

        float height =
                getHeight();

        drawTitle(
                canvas,
                width
        );

        float left =
                dp(48);

        float right =
                width - dp(16);

        float top =
                dp(48);

        float bottom =
                height - dp(48);

        drawGrid(
                canvas,
                left,
                top,
                right,
                bottom
        );

        if (values.isEmpty()) {
            drawEmptyState(
                    canvas,
                    width,
                    height
            );

            return;
        }

        double maximum =
                findMaximum();

        drawYAxis(
                canvas,
                maximum,
                left,
                top,
                bottom
        );

        drawLineChart(
                canvas,
                maximum,
                left,
                top,
                right,
                bottom
        );

        drawXAxisLabels(
                canvas,
                left,
                right,
                bottom
        );
    }

    private void drawTitle(
            Canvas canvas,
            float width
    ) {
        textPaint.setColor(
                Color.rgb(
                        31,
                        41,
                        55
                )
        );

        textPaint.setTextSize(
                sp(15)
        );

        textPaint.setFakeBoldText(
                true
        );

        textPaint.setTextAlign(
                Paint.Align.CENTER
        );

        canvas.drawText(
                title,
                width / 2f,
                dp(26),
                textPaint
        );

        textPaint.setFakeBoldText(
                false
        );
    }

    private void drawGrid(
            Canvas canvas,
            float left,
            float top,
            float right,
            float bottom
    ) {
        gridPaint.setColor(
                Color.rgb(
                        229,
                        231,
                        235
                )
        );

        gridPaint.setStrokeWidth(
                dp(1)
        );

        int horizontalLines = 4;

        for (int index = 0;
             index <= horizontalLines;
             index++) {

            float y =
                    top
                            + (bottom - top)
                            * index
                            / horizontalLines;

            canvas.drawLine(
                    left,
                    y,
                    right,
                    y,
                    gridPaint
            );
        }

        axisPaint.setColor(
                Color.rgb(
                        156,
                        163,
                        175
                )
        );

        axisPaint.setStrokeWidth(
                dp(1)
        );

        canvas.drawLine(
                left,
                top,
                left,
                bottom,
                axisPaint
        );

        canvas.drawLine(
                left,
                bottom,
                right,
                bottom,
                axisPaint
        );
    }

    private void drawYAxis(
            Canvas canvas,
            double maximum,
            float left,
            float top,
            float bottom
    ) {
        textPaint.setColor(
                Color.rgb(
                        107,
                        114,
                        128
                )
        );

        textPaint.setTextSize(
                sp(9)
        );

        textPaint.setTextAlign(
                Paint.Align.RIGHT
        );

        int labelsCount = 4;

        for (int index = 0;
             index <= labelsCount;
             index++) {

            double value =
                    maximum
                            - maximum
                            * index
                            / labelsCount;

            float y =
                    top
                            + (bottom - top)
                            * index
                            / labelsCount;

            canvas.drawText(
                    formatNumber(value),
                    left - dp(7),
                    y + dp(3),
                    textPaint
            );
        }
    }

    private void drawLineChart(
            Canvas canvas,
            double maximum,
            float left,
            float top,
            float right,
            float bottom
    ) {
        int count =
                values.size();

        if (count == 0) {
            return;
        }

        List<Float> xPoints =
                new ArrayList<>();

        List<Float> yPoints =
                new ArrayList<>();

        float availableWidth =
                right - left;

        float stepX =
                count <= 1
                        ? 0
                        : availableWidth
                        / (count - 1);

        for (int index = 0;
             index < count;
             index++) {

            double value =
                    safeValue(
                            values.get(index)
                    );

            float x =
                    count == 1
                            ? left
                            + availableWidth / 2f
                            : left
                            + stepX * index;

            float y =
                    bottom
                            - (float) (
                            value
                                    / maximum
                                    * (bottom - top)
                    );

            xPoints.add(x);
            yPoints.add(y);
        }

        Path linePath =
                buildSmoothPath(
                        xPoints,
                        yPoints
                );

        Path fillPath =
                new Path(linePath);

        fillPath.lineTo(
                xPoints.get(
                        xPoints.size() - 1
                ),
                bottom
        );

        fillPath.lineTo(
                xPoints.get(0),
                bottom
        );

        fillPath.close();

        linePaint.setColor(
                chartColor
        );

        fillPaint.setColor(
                withAlpha(
                        chartColor,
                        42
                )
        );

        canvas.drawPath(
                fillPath,
                fillPaint
        );

        canvas.drawPath(
                linePath,
                linePaint
        );

        drawPointsAndValues(
                canvas,
                xPoints,
                yPoints
        );
    }

    private Path buildSmoothPath(
            List<Float> xPoints,
            List<Float> yPoints
    ) {
        Path path =
                new Path();

        if (xPoints.isEmpty()) {
            return path;
        }

        path.moveTo(
                xPoints.get(0),
                yPoints.get(0)
        );

        if (xPoints.size() == 1) {
            return path;
        }

        for (int index = 1;
             index < xPoints.size();
             index++) {

            float previousX =
                    xPoints.get(
                            index - 1
                    );

            float previousY =
                    yPoints.get(
                            index - 1
                    );

            float currentX =
                    xPoints.get(index);

            float currentY =
                    yPoints.get(index);

            float middleX =
                    (previousX + currentX)
                            / 2f;

            path.cubicTo(
                    middleX,
                    previousY,
                    middleX,
                    currentY,
                    currentX,
                    currentY
            );
        }

        return path;
    }

    private void drawPointsAndValues(
            Canvas canvas,
            List<Float> xPoints,
            List<Float> yPoints
    ) {
        pointPaint.setColor(
                Color.WHITE
        );

        for (int index = 0;
             index < values.size();
             index++) {

            double value =
                    safeValue(
                            values.get(index)
                    );

            float x =
                    xPoints.get(index);

            float y =
                    yPoints.get(index);

            canvas.drawCircle(
                    x,
                    y,
                    dp(4),
                    pointPaint
            );

            pointPaint.setColor(
                    chartColor
            );

            canvas.drawCircle(
                    x,
                    y,
                    dp(2.5f),
                    pointPaint
            );

            pointPaint.setColor(
                    Color.WHITE
            );

            if (value > 0) {
                drawValue(
                        canvas,
                        value,
                        x,
                        y
                );
            }
        }
    }

    private void drawValue(
            Canvas canvas,
            double value,
            float x,
            float y
    ) {
        textPaint.setColor(
                Color.rgb(
                        55,
                        65,
                        81
                )
        );

        textPaint.setTextSize(
                sp(8)
        );

        textPaint.setTextAlign(
                Paint.Align.CENTER
        );

        canvas.drawText(
                formatNumber(value),
                x,
                Math.max(
                        dp(42),
                        y - dp(8)
                ),
                textPaint
        );
    }

    private void drawXAxisLabels(
            Canvas canvas,
            float left,
            float right,
            float bottom
    ) {
        int count =
                labels.size();

        if (count == 0) {
            return;
        }

        textPaint.setColor(
                Color.rgb(
                        107,
                        114,
                        128
                )
        );

        textPaint.setTextSize(
                sp(8)
        );

        textPaint.setTextAlign(
                Paint.Align.CENTER
        );

        float availableWidth =
                right - left;

        float stepX =
                count <= 1
                        ? 0
                        : availableWidth
                        / (count - 1);

        int labelStep =
                Math.max(
                        1,
                        (int) Math.ceil(
                                count / 6.0
                        )
                );

        for (int index = 0;
             index < count;
             index++) {

            if (index % labelStep != 0
                    && index != count - 1) {

                continue;
            }

            float x =
                    count == 1
                            ? left
                            + availableWidth / 2f
                            : left
                            + stepX * index;

            canvas.drawText(
                    shortenDate(
                            labels.get(index)
                    ),
                    x,
                    bottom + dp(18),
                    textPaint
            );
        }
    }

    private void drawEmptyState(
            Canvas canvas,
            float width,
            float height
    ) {
        textPaint.setColor(
                Color.rgb(
                        107,
                        114,
                        128
                )
        );

        textPaint.setTextSize(
                sp(13)
        );

        textPaint.setTextAlign(
                Paint.Align.CENTER
        );

        canvas.drawText(
                "No data for selected period",
                width / 2f,
                height / 2f,
                textPaint
        );
    }

    private double findMaximum() {
        double maximum = 0;

        for (Double value : values) {
            if (value != null
                    && value > maximum) {

                maximum = value;
            }
        }

        if (maximum <= 0) {
            return 1;
        }

        return maximum * 1.15;
    }

    private double safeValue(
            Double value
    ) {
        if (value == null
                || value < 0) {

            return 0;
        }

        return value;
    }

    private int withAlpha(
            int color,
            int alpha
    ) {
        return Color.argb(
                alpha,
                Color.red(color),
                Color.green(color),
                Color.blue(color)
        );
    }

    private String shortenDate(
            String value
    ) {
        if (value == null) {
            return "";
        }

        if (value.length() == 10
                && value.charAt(4) == '-'
                && value.charAt(7) == '-') {

            return value.substring(8, 10)
                    + "."
                    + value.substring(5, 7)
                    + ".";
        }

        return value;
    }

    private String formatNumber(
            double value
    ) {
        if (Math.abs(
                value - Math.round(value)
        ) < 0.001) {

            return String.valueOf(
                    Math.round(value)
            );
        }

        return String.format(
                Locale.US,
                "%.1f",
                value
        );
    }

    private float dp(
            float value
    ) {
        return value
                * getResources()
                .getDisplayMetrics()
                .density;
    }

    private float sp(
            float value
    ) {
        return value
                * getResources()
                .getDisplayMetrics()
                .scaledDensity;
    }
}