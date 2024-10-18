package com.instructure.pandautils.features.assignments.details.mobius.gradeCell

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.DP

class GradeStatisticsView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var stats: GradeCellViewState.GradeStats? = null

    private val sidePadding: Float = context.DP(2)
    private val endMarkerHeight: Float = context.DP(16)
    private val minMaxHeight: Float = context.DP(16)
    private val scoreCircleRadius: Float = context.DP(7)
    private val viewHeight: Int = context.DP(36).toInt()

    private val linePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.SQUARE
        isAntiAlias = true
        strokeWidth = context.DP(2)
        color = ContextCompat.getColor(context, R.color.backgroundMedium)
    }

    private val darkLinePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.SQUARE
        isAntiAlias = true
        strokeWidth = context.DP(3)
        color = ContextCompat.getColor(context, R.color.textDark)
    }

    private val meanLinePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.SQUARE
        isAntiAlias = true
        strokeWidth = context.DP(3)
        color = ContextCompat.getColor(context, R.color.textDarkest)
    }

    private val circlePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.backgroundInfo)
        style = Paint.Style.FILL
    }

    init {
        if (isInEditMode) {
            stats = GradeCellViewState.GradeStats(
                score = 81.0,
                outOf = 100.0,
                min = 38.0,
                max = 97.0,
                mean = 76.0,
                minText = "Low: 38",
                maxText = "High: 97",
                meanText = "Mean: 76"
            )
        }
    }

    fun setAccentColor(@ColorInt color: Int) {
        circlePaint.color = color
        invalidate()
    }

    fun setStats(stats: GradeCellViewState.GradeStats) {
        this.stats = stats
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val maxPossible = stats?.outOf ?: return

        val range = 0.0..maxPossible

        val minScore = stats?.min?.coerceIn(range) ?: return
        val maxScore = stats?.max?.coerceIn(range) ?: return
        val meanScore = stats?.mean?.coerceIn(range) ?: return
        val youScore = stats?.score?.coerceIn(range) ?: return

        val centerY = height / 2.0f

        // Draw centerline
        canvas.drawLine(sidePadding, centerY, width - sidePadding, centerY, linePaint)

        // Draw left and right bound
        canvas.drawLine(
            sidePadding,
            centerY + endMarkerHeight / 2.0f,
            sidePadding,
            centerY - endMarkerHeight / 2.0f,
            linePaint
        )
        canvas.drawLine(
            width - sidePadding,
            centerY + endMarkerHeight / 2.0f,
            width - sidePadding,
            centerY - endMarkerHeight / 2.0f,
            linePaint
        )


        // Draw Min/Max/Mean
        val usableWidth = width - 2 * sidePadding
        val minX = ((minScore / maxPossible) * usableWidth + sidePadding).toFloat()
        val maxX = ((maxScore / maxPossible) * usableWidth + sidePadding).toFloat()
        val meanX = ((meanScore / maxPossible) * usableWidth + sidePadding).toFloat()
        val youX = ((youScore / maxPossible) * usableWidth + sidePadding).toFloat()

        // Draw darker line between min and max
        canvas.drawLine(minX, centerY, maxX, centerY, darkLinePaint)

        canvas.drawLine(minX, centerY + minMaxHeight / 2.0f, minX, centerY - minMaxHeight / 2.0f, darkLinePaint)
        canvas.drawLine(maxX, centerY + minMaxHeight / 2.0f, maxX, centerY - minMaxHeight / 2.0f, darkLinePaint)
        canvas.drawLine(meanX, centerY + endMarkerHeight / 2.0f, meanX, centerY - endMarkerHeight / 2.0f, meanLinePaint)

        // Draw your score
        canvas.drawCircle(youX, centerY, scoreCircleRadius, circlePaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightSpec = MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY)
        setMeasuredDimension(widthMeasureSpec, heightSpec)
    }
}
