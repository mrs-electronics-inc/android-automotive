package com.mrselectronics.gaugecluster

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

import java.util.Locale

/**
 * Compact label / value / unit readout for the cluster's tertiary status rail.
 * Optionally renders a thin horizontal progress bar below the readout (used for
 * things like fuel level where a 0..1 progress is meaningful).
 */
class StatusReadoutView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(150, 160, 160)
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.12f
    }

    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(150, 160, 160)
        textAlign = Paint.Align.LEFT
    }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(32, 42, 42)
        style = Paint.Style.FILL
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.system_ui_blue_bright)
        style = Paint.Style.FILL
    }

    private val barRect = RectF()

    private var label = ""
    private var unit = ""
    private var minValue = 0f
    private var maxValue = 0f
    private var value = 0f
    private var showBar = false

    fun configure(
        label: String,
        unit: String,
        minValue: Float = 0f,
        maxValue: Float = 0f,
        showBar: Boolean = false
    ) {
        this.label = label.uppercase(Locale.getDefault())
        this.unit = unit
        this.minValue = minValue
        this.maxValue = maxValue
        this.showBar = showBar
        invalidate()
    }

    fun setValue(value: Float) {
        this.value = value
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) {
            return
        }

        // Size text proportionally to the view height so the rail scales cleanly.
        labelPaint.textSize = h * 0.16f
        valuePaint.textSize = h * 0.42f
        unitPaint.textSize = h * 0.17f

        val centerX = w / 2f

        // LABEL across the top.
        val labelMetrics = labelPaint.fontMetrics
        val labelTop = h * 0.10f
        val labelBaseline = labelTop - labelMetrics.top
        canvas.drawText(label, centerX, labelBaseline, labelPaint)

        // VALUE + UNIT on a shared baseline below the label.
        val valueText = formatValue()
        val valueMetrics = valuePaint.fontMetrics
        val valueTop = labelBaseline + labelMetrics.descent + h * 0.06f
        val valueBaseline = valueTop - valueMetrics.top
        val valueWidth = valuePaint.measureText(valueText)
        val unitGap = h * 0.06f
        val unitWidth = if (unit.isNotEmpty()) unitPaint.measureText(unit) else 0f
        val totalWidth = valueWidth + (if (unit.isNotEmpty()) unitGap + unitWidth else 0f)
        val groupLeft = centerX - totalWidth / 2f
        val valueDrawX = groupLeft + valueWidth / 2f
        canvas.drawText(valueText, valueDrawX, valueBaseline, valuePaint)
        if (unit.isNotEmpty()) {
            val unitX = groupLeft + valueWidth + unitGap
            // Align unit visually to the value's baseline-ish (use value descent line).
            canvas.drawText(unit, unitX, valueBaseline, unitPaint)
        }

        // Optional progress bar below.
        if (showBar) {
            val barHeight = h * 0.06f
            val barInset = w * 0.18f
            val barLeft = barInset
            val barRight = w - barInset
            val barBottom = h * 0.92f
            val barTop = barBottom - barHeight
            val radius = barHeight / 2f

            barRect.set(barLeft, barTop, barRight, barBottom)
            canvas.drawRoundRect(barRect, radius, radius, trackPaint)

            val fillRight = barLeft + (barRight - barLeft) * progress
            if (fillRight > barLeft) {
                barRect.set(barLeft, barTop, fillRight, barBottom)
                canvas.drawRoundRect(barRect, radius, radius, fillPaint)
            }
        }
    }

    private val progress: Float
        get() {
            if (maxValue <= minValue) {
                return 0f
            }
            return ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
        }

    private fun formatValue(): String = String.format(Locale.US, "%.0f", value)
}
