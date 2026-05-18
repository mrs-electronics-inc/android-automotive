package com.mrselectronics.gaugecluster

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

import java.util.Locale
import kotlin.math.min

class GaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.system_ui_blue)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(32, 42, 42)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(150, 160, 160)
        textAlign = Paint.Align.CENTER
    }

    private val arcBounds = RectF()

    private var label = ""
    private var unit = ""
    private var minValue = 0f
    private var maxValue = 100f
    private var value = 0f

    fun configure(label: String, unit: String, minValue: Float, maxValue: Float) {
        this.label = label
        this.unit = unit
        this.minValue = minValue
        this.maxValue = maxValue
        invalidate()
    }

    fun setValue(value: Float) {
        this.value = value
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val size = min(viewWidth, viewHeight)
        val centerX = viewWidth / 2f
        val centerY = viewHeight * 0.56f
        val strokeWidth = size * 0.045f
        val radius = size * 0.36f

        trackPaint.strokeWidth = strokeWidth
        arcPaint.strokeWidth = strokeWidth
        valuePaint.textSize = size * 0.17f
        labelPaint.textSize = size * 0.075f

        arcBounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        canvas.drawArc(arcBounds, START_ANGLE, SWEEP_ANGLE, false, trackPaint)
        canvas.drawArc(arcBounds, START_ANGLE, SWEEP_ANGLE * progress, false, arcPaint)

        drawCenteredTextInBand(
            canvas = canvas,
            text = label,
            centerX = centerX,
            top = size * 0.06f,
            bottom = arcBounds.top - size * 0.08f,
            paint = labelPaint
        )
        drawCenteredValueAndUnit(canvas, centerX, centerY, size)
    }

    private fun drawCenteredValueAndUnit(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        size: Float
    ) {
        val spacing = size * 0.006f
        val valueText = formatValue()
        val valueMetrics = valuePaint.fontMetrics
        val unitMetrics = labelPaint.fontMetrics
        val valueHeight = valueMetrics.bottom - valueMetrics.top
        val unitHeight = unitMetrics.bottom - unitMetrics.top

        if (unit.isBlank()) {
            val valueBaseline = centerY - (valueMetrics.ascent + valueMetrics.descent) / 2f
            canvas.drawText(valueText, centerX, valueBaseline, valuePaint)
            return
        }

        val groupHeight = valueHeight + spacing + unitHeight
        val groupTop = centerY - groupHeight / 2f
        val valueBaseline = groupTop - valueMetrics.top
        val unitTop = groupTop + valueHeight + spacing
        val unitBaseline = unitTop - unitMetrics.top

        canvas.drawText(valueText, centerX, valueBaseline, valuePaint)
        canvas.drawText(unit, centerX, unitBaseline, labelPaint)
    }

    private fun drawCenteredTextInBand(
        canvas: Canvas,
        text: String,
        centerX: Float,
        top: Float,
        bottom: Float,
        paint: Paint
    ) {
        if (text.isEmpty() || bottom <= top) {
            return
        }

        val fontMetrics = paint.fontMetrics
        val textHeight = fontMetrics.bottom - fontMetrics.top
        val availableHeight = bottom - top
        val baseline = top + (availableHeight - textHeight) / 2f - fontMetrics.top
        canvas.drawText(text, centerX, baseline, paint)
    }

    private val progress: Float
        get() {
            if (maxValue <= minValue) {
                return 0f
            }

            val rawProgress = (value - minValue) / (maxValue - minValue)
            return rawProgress.coerceIn(0f, 1f)
        }

    private fun formatValue(): String = String.format(Locale.US, "%.0f", value)

    companion object {
        private const val START_ANGLE = 135f
        private const val SWEEP_ANGLE = 270f
    }
}
