package com.instructure.teacher.view.grade_slider

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.instructure.pandautils.utils.SP
import com.instructure.pandautils.utils.toPx
import com.instructure.teacher.R
import com.instructure.teacher.utils.getColorCompat

class PossiblePointView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var anchorRect = Rect()

    private var label: String = ""

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = context.SP(16f)
        textAlign = Paint.Align.CENTER
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColorCompat(R.color.white)
        strokeWidth = 4.toPx.toFloat()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawLine(anchorRect.left.toFloat(), anchorRect.bottom.toFloat() - 4.toPx, anchorRect.left.toFloat(), anchorRect.bottom.toFloat(), linePaint)
        canvas?.drawText(label, anchorRect.left.toFloat(), anchorRect.bottom.toFloat() + 24.toPx, textPaint)
    }

    fun showPossiblePoint(anchorRect: Rect, label: String) {
        this.anchorRect = anchorRect
        this.label = label
        invalidate()
    }
}