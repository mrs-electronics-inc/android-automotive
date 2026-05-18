package com.mrselectronics.gaugecluster;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

public class GaugeView extends View {
    private static final float START_ANGLE = 135f;
    private static final float SWEEP_ANGLE = 270f;

    private final Paint mArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mNeedlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
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

        mArcPaint.setColor(Color.rgb(0, 255, 120));
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);

        mNeedlePaint.setColor(Color.WHITE);
        mNeedlePaint.setStrokeCap(Paint.Cap.ROUND);

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
        mNeedlePaint.setStrokeWidth(size * 0.018f);
        mTextPaint.setTextSize(size * 0.17f);
        mLabelPaint.setTextSize(size * 0.075f);

        mArcBounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        canvas.drawArc(mArcBounds, START_ANGLE, SWEEP_ANGLE, false, mTrackPaint);
        canvas.drawArc(mArcBounds, START_ANGLE, SWEEP_ANGLE * getProgress(), false, mArcPaint);

        float needleAngle = (float) Math.toRadians(START_ANGLE + SWEEP_ANGLE * getProgress());
        float needleLength = radius * 0.78f;
        canvas.drawLine(
                centerX,
                centerY,
                centerX + (float) Math.cos(needleAngle) * needleLength,
                centerY + (float) Math.sin(needleAngle) * needleLength,
                mNeedlePaint);

        canvas.drawCircle(centerX, centerY, size * 0.035f, mNeedlePaint);
        canvas.drawText(mLabel, centerX, size * 0.16f, mLabelPaint);
        canvas.drawText(formatValue(), centerX, centerY + radius * 0.68f, mTextPaint);
        canvas.drawText(mUnit, centerX, centerY + radius * 0.94f, mLabelPaint);
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
