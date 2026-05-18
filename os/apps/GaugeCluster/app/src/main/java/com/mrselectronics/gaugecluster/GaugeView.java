package com.mrselectronics.gaugecluster;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import java.util.Locale;

public class GaugeView extends View {
    private static final float START_ANGLE = 135f;
    private static final float SWEEP_ANGLE = 270f;

    private final Paint mArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mArcBounds = new RectF();

    private String mLabel = "";
    private String mUnit = "";
    private float mMinValue = 0f;
    private float mMaxValue = 100f;
    private float mValue = 0f;

    public GaugeView(Context context) {
        super(context);
        init();
    }

    public GaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mTrackPaint.setColor(Color.rgb(32, 42, 42));
        mTrackPaint.setStyle(Paint.Style.STROKE);
        mTrackPaint.setStrokeCap(Paint.Cap.ROUND);

        mArcPaint.setColor(ContextCompat.getColor(getContext(), R.color.system_ui_blue));
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);

        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setFakeBoldText(true);

        mLabelPaint.setColor(Color.rgb(150, 160, 160));
        mLabelPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void configure(String label, String unit, float minValue, float maxValue) {
        mLabel = label;
        mUnit = unit;
        mMinValue = minValue;
        mMaxValue = maxValue;
        invalidate();
    }

    public void setValue(float value) {
        mValue = value;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float size = Math.min(width, height);
        float centerX = width / 2f;
        float centerY = height * 0.56f;
        float strokeWidth = size * 0.045f;
        float radius = size * 0.36f;

        mTrackPaint.setStrokeWidth(strokeWidth);
        mArcPaint.setStrokeWidth(strokeWidth);
        mTextPaint.setTextSize(size * 0.17f);
        mLabelPaint.setTextSize(size * 0.075f);

        mArcBounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        canvas.drawArc(mArcBounds, START_ANGLE, SWEEP_ANGLE, false, mTrackPaint);
        canvas.drawArc(mArcBounds, START_ANGLE, SWEEP_ANGLE * getProgress(), false, mArcPaint);

        drawCenteredTextInBand(
                canvas,
                mLabel,
                centerX,
                size * 0.06f,
                mArcBounds.top - size * 0.08f,
                mLabelPaint);
        drawCenteredValueAndUnit(canvas, centerX, centerY, size);
    }

    private void drawCenteredValueAndUnit(Canvas canvas, float centerX, float centerY, float size) {
        float spacing = size * 0.006f;

        String valueText = formatValue();
        Paint.FontMetrics valueMetrics = mTextPaint.getFontMetrics();
        Paint.FontMetrics unitMetrics = mLabelPaint.getFontMetrics();
        float valueHeight = valueMetrics.bottom - valueMetrics.top;
        float unitHeight = unitMetrics.bottom - unitMetrics.top;

        if (mUnit.isEmpty()) {
            float valueBaseline = centerY - (valueMetrics.ascent + valueMetrics.descent) / 2f;
            canvas.drawText(valueText, centerX, valueBaseline, mTextPaint);
            return;
        }

        float groupHeight = valueHeight + spacing + unitHeight;
        float groupTop = centerY - groupHeight / 2f;
        float valueBaseline = groupTop - valueMetrics.top;
        float unitTop = groupTop + valueHeight + spacing;
        float unitBaseline = unitTop - unitMetrics.top;

        canvas.drawText(valueText, centerX, valueBaseline, mTextPaint);
        canvas.drawText(mUnit, centerX, unitBaseline, mLabelPaint);
    }

    private void drawCenteredTextInBand(
            Canvas canvas, String text, float centerX, float top, float bottom, Paint paint) {
        if (text.isEmpty() || bottom <= top) {
            return;
        }

        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float textHeight = fontMetrics.bottom - fontMetrics.top;
        float availableHeight = bottom - top;
        float baseline = top + (availableHeight - textHeight) / 2f - fontMetrics.top;
        canvas.drawText(text, centerX, baseline, paint);
    }

    private float getProgress() {
        if (mMaxValue <= mMinValue) {
            return 0f;
        }
        float progress = (mValue - mMinValue) / (mMaxValue - mMinValue);
        return Math.max(0f, Math.min(1f, progress));
    }

    private String formatValue() {
        return String.format(Locale.US, "%.0f", mValue);
    }
}
