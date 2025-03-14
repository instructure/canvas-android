package com.instructure.teacher.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.instructure.teacher.R
import kotlin.math.abs
import kotlin.math.min

class DonutChart(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var radius: Float
    private val paint: Paint
    private val textPaint: TextPaint
    private val path: Path
    private val outerCircle: RectF
    private val innerCircle: RectF
    private var selected = 0
    private var total = 0
    private var selectedColor = 0
    private var unselectedColor = ContextCompat.getColor(context, R.color.porcelain)
    private var centerText: String? = ""
    private var centerTextSize = 0f
    private var textX = 0f
    private var textY = 0f
    private var shouldStartAnimation = false
    private var animStartTime: Long = 0
    private val animInterpolator = DecelerateInterpolator()

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.DonutChart, 0, 0)
        try {
            radius = a.getDimension(R.styleable.DonutChart_dc_radius, 48.0f)
            centerText = a.getString(R.styleable.DonutChart_dc_center_text)
            centerTextSize = a.getDimension(R.styleable.DonutChart_dc_center_text_size, 22f)
        } finally {
            a.recycle()
        }

        // Get screen density
        val scale = getContext().resources.displayMetrics.density
        radius = radius * scale + .5f
        paint = Paint()
        paint.isDither = true
        paint.style = Paint.Style.FILL
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.isAntiAlias = true
        paint.strokeWidth = radius / 14.0f
        textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG)
        textPaint.textAlign = Paint.Align.CENTER //Draw text from center
        textPaint.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        textPaint.color = ContextCompat.getColor(context, R.color.textDarkest)

        // Convert the dips to pixels
        val textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            centerTextSize,
            resources.displayMetrics
        )
        textPaint.textSize = textSize
        path = Path()
        outerCircle = RectF()
        innerCircle = RectF()
        val outerAdjust: Float = 2f * scale
        val innerAdjust: Float = outerAdjust + 2f * scale

        outerCircle[outerAdjust, outerAdjust, radius * 2 - outerAdjust] = radius * 2 - outerAdjust
        innerCircle[innerAdjust, innerAdjust, radius * 2 - innerAdjust] = radius * 2 - innerAdjust
    }

    fun setSelected(selected: Int) {
        this.selected = selected
        shouldStartAnimation = true
        invalidate()
    }

    fun setTotal(total: Int) {
        this.total = total
        shouldStartAnimation = true
        invalidate()
    }

    fun setSelectedColor(@ColorInt selectedColor: Int) {
        this.selectedColor = selectedColor
    }

    fun setUnselectedColor(@ColorInt unselectedColor: Int) {
        this.unselectedColor = unselectedColor
    }

    fun setCenterText(centerText: String?) {
        this.centerText = centerText
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (shouldStartAnimation) {
            shouldStartAnimation = false
            animStartTime = System.currentTimeMillis()
        }
        val animProgress = ((System.currentTimeMillis() - animStartTime) / ANIM_DURATION).coerceAtMost(1f)
        paint.shader = null
        var interpolatedProgress = animInterpolator.getInterpolation(animProgress)
        if (interpolatedProgress > 1f) interpolatedProgress = 1f
        val endGray = 359.9999f * interpolatedProgress * selected / total

        // Gray
        paint.color = unselectedColor
        drawDonut(canvas, paint, 0f, 359.9999f)

        // Theme color
        paint.color = selectedColor
        // 270 == top of the circle
        drawDonut(canvas, paint, 270f, endGray)

        if (centerText != null) {
            var drawText = centerText!!
            if (animProgress < 1f) {
                try {
                    var count = centerText!!.toInt()
                    count = (count * interpolatedProgress).toInt()
                    drawText = count.toString()
                } catch (ignore: NumberFormatException) {
                }
            }
            canvas.drawText(drawText, textX, textY, textPaint)
        }
        if (animProgress < 1f) invalidate()
    }

    private fun drawDonut(canvas: Canvas, paint: Paint, start: Float, sweep: Float) {
        path.reset()
        path.arcTo(outerCircle, start, sweep, false)
        path.arcTo(innerCircle, start + sweep, -sweep, false)
        path.close()
        canvas.drawPath(path, paint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Get the starting x and y coordinates of the text in the middle of the view
        val metrics = textPaint.fontMetrics
        val height = abs(metrics.top - metrics.bottom)
        textX = width / 2f
        textY = getHeight() / 2 + height / 4
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val desiredWidth = radius.toInt() * 2
        val desiredHeight = radius.toInt() * 2
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val width: Int
        val height: Int

        // 70dp exact
        width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(desiredWidth, widthSize) // wrap content
            else -> desiredWidth
        }

        // Measure Height
        height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredHeight, heightSize)
            else -> desiredHeight
        }

        // MUST CALL THIS
        setMeasuredDimension(width, height)
    }

    companion object {
        private const val ANIM_DURATION = 500f
    }
}
