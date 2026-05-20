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

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(32, 42, 42)
        style = Paint.Style.FILL
    }

    private val fillPaintHigh = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(76, 175, 80)  // Green
        style = Paint.Style.FILL
    }

    private val fillPaintMid = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 193, 7)  // Yellow/Amber
        style = Paint.Style.FILL
    }

    private val fillPaintLow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(244, 67, 54)  // Red
        style = Paint.Style.FILL
    }

    private val pipRect = RectF()

    private var label = ""
    private var minValue = 0f
    private var maxValue = 0f
    private var value = 0f
    private var showBar = false

    fun configure(
        label: String,
        minValue: Float = 0f,
        maxValue: Float = 0f,
        showBar: Boolean = false
    ) {
        this.label = label.uppercase(Locale.getDefault())
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

        val centerX = w / 2f

        // LABEL across the top.
        val labelMetrics = labelPaint.fontMetrics
        val labelTop = h * 0.10f
        val labelBaseline = labelTop - labelMetrics.top
        canvas.drawText(label, centerX, labelBaseline, labelPaint)

        // VALUE below the label, centered.
        val valueText = formatValue()
        val valueMetrics = valuePaint.fontMetrics
        val valueTop = labelBaseline + labelMetrics.descent + h * 0.06f
        val valueBaseline = valueTop - valueMetrics.top
        canvas.drawText(valueText, centerX, valueBaseline, valuePaint)

        // Optional segmented level indicator below.
        if (showBar) {
            drawSegmentedLevelIndicator(canvas, w, h)
        }
    }

    private fun drawSegmentedLevelIndicator(canvas: Canvas, w: Float, h: Float) {
        val pipCount = 10
        val pipHeight = h * 0.05f
        val totalPipWidth = w * 0.64f  // Width for all 10 pips
        val pipGap = totalPipWidth * 0.08f  // Gap between pips
        val pipWidth = (totalPipWidth - pipGap * (pipCount - 1)) / pipCount

        val barInset = w * 0.18f
        val barLeft = barInset
        val barBottom = h * 0.92f
        val barTop = barBottom - pipHeight

        val filledCount = (progress * pipCount).toInt().coerceIn(0, pipCount)

        // Draw all pips (filled and unfilled)
        for (i in 0 until pipCount) {
            val pipLeft = barLeft + i * (pipWidth + pipGap)
            val pipRight = pipLeft + pipWidth
            pipRect.set(pipLeft, barTop, pipRight, barBottom)

            if (i < filledCount) {
                // Filled pip: color based on level
                val fillPaint = when {
                    progress > 0.5f -> fillPaintHigh    // Green for > 50%
                    progress > 0.2f -> fillPaintMid     // Yellow for 20-50%
                    else -> fillPaintLow                // Red for < 20%
                }
                canvas.drawRoundRect(pipRect, pipWidth * 0.2f, pipHeight * 0.2f, fillPaint)
            } else {
                // Empty pip: track color
                canvas.drawRoundRect(pipRect, pipWidth * 0.2f, pipHeight * 0.2f, trackPaint)
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
