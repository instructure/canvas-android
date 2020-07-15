package com.instructure.student.mobius.assignmentDetails.ui.gradeCell

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.instructure.pandautils.utils.DP
import com.instructure.student.R

class GradeStatisticsView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var gradeData: GradeCellViewState.GradeData? = null;

    private val linePaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
        strokeWidth = context.DP(3)
        color = ContextCompat.getColor(
                context,
                R.color.canvasTextMedium
        )
    }

    private val darkLinePaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
        strokeWidth = context.DP(3)
        color = ContextCompat.getColor(
                context,
                R.color.canvasTextDark
        )
    }

    private val meanLinePaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
        strokeWidth = context.DP(3)
        color = Color.BLACK
    }

    private val circlePaint: Paint = Paint().apply {
        color = ContextCompat.getColor(
                context,
                R.color.canvasDefaultAccent
        )
        style = Paint.Style.FILL
    }

    fun setState(state: GradeCellViewState.GradeData) {
        gradeData = state

        invalidate()
    }


    private val SIDE_PAD = 20.0f
    private val ENDMARKER_HEIGHT = 20.0f
    private val MINMAX_HEIGHT = 15.0f

    private val MAX_HEIGHT = 200


    override fun onDraw(canvas: Canvas) {
        val maxPossible = gradeData?.outOfDouble ?: return

        val range = 0.0 .. maxPossible

        val minScore = gradeData?.statisticsMin?.coerceIn(range)
        val maxScore = gradeData?.statisticsMax?.coerceIn(range)
        val meanScore= gradeData?.statisticsMean?.coerceIn(range)
        val youScore= gradeData?.scoreDouble?.coerceIn(range)


        if (minScore == null || maxScore == null || meanScore == null || youScore == null || maxPossible == null) {
            return;
        }

        val centerY = canvas.height / 2.0f

        // Draw centerline
        canvas.drawLine(SIDE_PAD, centerY, canvas.width - SIDE_PAD, centerY, linePaint)

        // Draw left and right bound
        canvas.drawLine(SIDE_PAD, centerY + ENDMARKER_HEIGHT/2.0f, SIDE_PAD, centerY - ENDMARKER_HEIGHT/2.0f, linePaint)
        canvas.drawLine(canvas.width - SIDE_PAD, centerY + ENDMARKER_HEIGHT/2.0f, canvas.width - SIDE_PAD, centerY - ENDMARKER_HEIGHT/2.0f, linePaint)

        // Draw Min/Max/Mean
        val usableWidth = canvas.width - 2 * SIDE_PAD
        val minX = ((minScore / maxPossible) * usableWidth + SIDE_PAD).toFloat()
        val maxX = ((maxScore / maxPossible) * usableWidth + SIDE_PAD).toFloat()
        val meanX = ((meanScore / maxPossible) * usableWidth + SIDE_PAD).toFloat()
        val youX = ((youScore / maxPossible) * usableWidth + SIDE_PAD).toFloat()


        canvas.drawLine( minX, centerY + MINMAX_HEIGHT/2.0f, minX,centerY - MINMAX_HEIGHT/2.0f, darkLinePaint)
        canvas.drawLine( maxX, centerY + MINMAX_HEIGHT/2.0f, maxX,centerY - MINMAX_HEIGHT/2.0f, darkLinePaint)
        canvas.drawLine( meanX, centerY + ENDMARKER_HEIGHT/2.0f, meanX,centerY - ENDMARKER_HEIGHT/2.0f, meanLinePaint)

        // Draw darker line between min and max
        canvas.drawLine(minX, centerY, maxX, centerY, darkLinePaint)

        // Draw your score
        canvas.drawCircle(youX, centerY, 20.0f, circlePaint)

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(widthMeasureSpec, if (heightMeasureSpec < MAX_HEIGHT) heightMeasureSpec else MAX_HEIGHT)
    }
}