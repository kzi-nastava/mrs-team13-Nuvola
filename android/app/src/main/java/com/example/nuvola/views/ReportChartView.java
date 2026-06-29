package com.example.nuvola.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportChartView extends View {

    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint textPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final List<String> labels =
            new ArrayList<>();

    private final List<Double> values =
            new ArrayList<>();

    private String title = "";

    private int barColor =
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
        setMinimumHeight((int) dp(250));
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setData(
            String title,
            List<String> labels,
            List<Double> values
    ) {
        this.title =
                title == null ? "" : title;

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

    public void setBarColor(int barColor) {
        this.barColor = barColor;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        drawTitle(canvas, width);

        float left = dp(46);
        float right = width - dp(14);
        float top = dp(48);
        float bottom = height - dp(48);

        drawGrid(
                canvas,
                left,
                top,
                right,
                bottom
        );

        if (values.isEmpty()) {
            drawEmptyState(canvas, width, height);
            return;
        }

        double maximum = findMaximum();

        drawYAxis(
                canvas,
                maximum,
                left,
                top,
                bottom
        );

        drawBars(
                canvas,
                maximum,
                left,
                top,
                right,
                bottom
        );
    }

    private void drawTitle(
            Canvas canvas,
            float width
    ) {
        textPaint.setColor(
                Color.rgb(31, 41, 55)
        );

        textPaint.setTextSize(sp(15));
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(
                Paint.Align.CENTER
        );

        canvas.drawText(
                title,
                width / 2f,
                dp(26),
                textPaint
        );

        textPaint.setFakeBoldText(false);
    }

    private void drawGrid(
            Canvas canvas,
            float left,
            float top,
            float right,
            float bottom
    ) {
        paint.setColor(
                Color.rgb(229, 231, 235)
        );

        paint.setStrokeWidth(dp(1));

        int lines = 4;

        for (int index = 0;
             index <= lines;
             index++) {

            float y =
                    top
                            + (bottom - top)
                            * index
                            / lines;

            canvas.drawLine(
                    left,
                    y,
                    right,
                    y,
                    paint
            );
        }

        paint.setColor(
                Color.rgb(156, 163, 175)
        );

        canvas.drawLine(
                left,
                top,
                left,
                bottom,
                paint
        );

        canvas.drawLine(
                left,
                bottom,
                right,
                bottom,
                paint
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
                Color.rgb(107, 114, 128)
        );

        textPaint.setTextSize(sp(9));
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
                    left - dp(6),
                    y + dp(3),
                    textPaint
            );
        }
    }

    private void drawBars(
            Canvas canvas,
            double maximum,
            float left,
            float top,
            float right,
            float bottom
    ) {
        int count = values.size();

        float availableWidth =
                right - left;

        float groupWidth =
                availableWidth / count;

        float barWidth =
                Math.min(
                        groupWidth * 0.58f,
                        dp(38)
                );

        paint.setColor(barColor);

        for (int index = 0;
             index < count;
             index++) {

            double value =
                    values.get(index) == null
                            ? 0
                            : values.get(index);

            float barHeight =
                    (float) (
                            value
                                    / maximum
                                    * (bottom - top)
                    );

            float centerX =
                    left
                            + groupWidth * index
                            + groupWidth / 2f;

            RectF rectangle =
                    new RectF(
                            centerX - barWidth / 2f,
                            bottom - barHeight,
                            centerX + barWidth / 2f,
                            bottom
                    );

            canvas.drawRoundRect(
                    rectangle,
                    dp(5),
                    dp(5),
                    paint
            );

            if (value > 0) {
                drawValue(
                        canvas,
                        value,
                        centerX,
                        bottom - barHeight
                );
            }

            int labelStep = Math.max(
                    1,
                    (int) Math.ceil(count / 6.0)
            );

            if (index % labelStep == 0
                    || index == count - 1) {

                drawLabel(
                        canvas,
                        labelAt(index),
                        centerX,
                        bottom
                );
            }
        }
    }

    private void drawValue(
            Canvas canvas,
            double value,
            float centerX,
            float barTop
    ) {
        textPaint.setColor(
                Color.rgb(55, 65, 81)
        );

        textPaint.setTextSize(sp(8));
        textPaint.setTextAlign(
                Paint.Align.CENTER
        );

        canvas.drawText(
                formatNumber(value),
                centerX,
                Math.max(
                        dp(42),
                        barTop - dp(5)
                ),
                textPaint
        );
    }

    private void drawLabel(
            Canvas canvas,
            String label,
            float centerX,
            float bottom
    ) {
        textPaint.setColor(
                Color.rgb(107, 114, 128)
        );

        textPaint.setTextSize(sp(8));
        textPaint.setTextAlign(
                Paint.Align.CENTER
        );

        String displayed =
                shortenDate(label);

        canvas.drawText(
                displayed,
                centerX,
                bottom + dp(18),
                textPaint
        );
    }

    private void drawEmptyState(
            Canvas canvas,
            float width,
            float height
    ) {
        textPaint.setColor(
                Color.rgb(107, 114, 128)
        );

        textPaint.setTextSize(sp(13));
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
        double maximum = 1;

        for (Double value : values) {
            if (value != null
                    && value > maximum) {

                maximum = value;
            }
        }

        return maximum;
    }

    private String labelAt(int index) {
        if (index < 0
                || index >= labels.size()) {

            return "";
        }

        return labels.get(index);
    }

    private String shortenDate(String value) {
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

    private String formatNumber(double value) {
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

    private float dp(float value) {
        return value
                * getResources()
                .getDisplayMetrics()
                .density;
    }

    private float sp(float value) {
        return value
                * getResources()
                .getDisplayMetrics()
                .scaledDensity;
    }
}