/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.teacher.view.edit_rubric

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.*
import android.text.TextUtils.TruncateAt
import android.util.AttributeSet
import android.view.View
import com.instructure.pandautils.utils.DP
import com.instructure.pandautils.utils.SP
import com.instructure.pandautils.utils.isRTL
import com.instructure.pandautils.utils.positionOnScreen
import com.instructure.teacher.R
import com.instructure.teacher.utils.TeacherPrefs
import com.instructure.teacher.utils.getColorCompat
import com.instructure.teacher.view.TutorialSwipeView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RubricTooltipView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** Maximum tooltip width */
    private val maxWidth = context.DP(MAX_WIDTH_DP).toInt()

    /** The [Layout] used for measuring and laying out the tooltip text */
    private var textLayout: Layout? = null

    /** Rect of the anchor view */
    private var anchorRect = Rect()

    /** The animation used to fade the tooltip in an out */
    private var animator: Animator? = null

    /** The size of the triangular tail attaching the tooltip to the anchor point */
    private val tailSize = context.DP(5f)

    /** Rounded corner radius of the tooltip */
    private val cornerRadius = context.DP(5f)

    /** A [RectF] which holds the target bounding box of the tooltip bubble */
    private val bubbleRect = RectF()

    /** Vertical offset of the tooltip from the anchor point */
    private val bubbleOffset = context.DP(4f)

    /** Path which holds the tooltip attachment point / triangle */
    private lateinit var tailPath: Path

    /** A reference to a view whose padding should be used to set the margin around the text bubble */
    private var bubbleConstraintReference: Int = 0

    /** The margins around the bubble */
    private val bubbleMargin: Rect by lazy {
        Rect().also { rect ->
            (context as? Activity)?.findViewById<View>(bubbleConstraintReference)?.let {
                rect.left = it.paddingLeft
                rect.top = it.paddingTop
                rect.right = it.paddingRight
                rect.bottom = it.paddingBottom
            }
            if (isRTL()) {
                // Swap left and right for RTL locales
                val tmp = rect.left
                rect.left = rect.right
                rect.right = tmp
            }
        }
    }

    /** Paint used in drawing the tooltip text */
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = context.SP(16f)
    }

    /** ID Of the assignee associated with this rubric assessment */
    var assigneeId: Long = 0

    /** Paint used to draw the tooltip bubble and tail */
    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColorCompat(R.color.defaultTextDark)
    }

    init {
        // A11y events will happen on each rating button and not on the tooltip
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS

        // Obtain view reference for padding constraint
        val a = context.obtainStyledAttributes(attrs, R.styleable.RubricTooltipView)
        bubbleConstraintReference = a.getResourceId(R.styleable.RubricTooltipView_rtt_constrainBubbleToPaddingOf, 0)
        a.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        EventBus.getDefault().unregister(this)
        super.onDetachedFromWindow()
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        // Fake tooltip for UI preview
        if (isInEditMode) {
            @Suppress("DEPRECATION")
            textLayout = StaticLayout("This is a tooltip", textPaint, right - left, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false)
        }
        // Create path for tail
        tailPath = Path().apply {
            moveTo(0f, 0f)
            lineTo(tailSize, -tailSize - 0.5f)
            lineTo(-tailSize, -tailSize - 0.5f)
            close()
        }
        super.onLayout(changed, left, top, right, bottom)
    }

    @Suppress("DEPRECATION")
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {

        // Draw the tutorial element if the user hasn't seen it previously.
        // Allocations during draw are acceptable as this will only happen once
        if (!TeacherPrefs.hasViewedRubricTutorial) {
            val circle = TutorialSwipeView(context)
            circle.isDrawingCacheEnabled = true
            circle.showTrail = false
            val dim = resources.getDimension(R.dimen.speedGraderTutorialTouchSize).toInt()
            val dimSpec = MeasureSpec.makeMeasureSpec(dim, MeasureSpec.EXACTLY)
            circle.measure(dimSpec, dimSpec)
            val radius = dim / 2f
            circle.layout(0, 0, dim, dim)

            canvas.save()
            canvas.translate(anchorRect.left - radius, anchorRect.centerY() - radius)
            circle.buildDrawingCache()
            canvas.drawBitmap(circle.drawingCache, 0f, 0f, null)
            canvas.restore()
            circle.isDrawingCacheEnabled = false
        }

        // Draw the tutorial bubble and text
        textLayout?.let { layout ->
            val textWidth = layout.maxLineWidth().toFloat()
            val textHeight: Float = layout.height.toFloat()
            val bubbleWidth: Float = textWidth + paddingStart + paddingEnd
            val bubbleHeight: Float = textHeight + paddingTop + paddingBottom


            // Determine location for tooltip bubble. Center above anchor without going off the edge
            with(bubbleRect) {
                left = (anchorRect.left - (bubbleWidth / 2f)).coerceAtLeast(bubbleMargin.left.toFloat())
                right = left + bubbleWidth
                if (right > width) {
                    right = width.toFloat()
                    left = right - bubbleWidth
                }
                top = anchorRect.top - bubbleHeight - tailSize - bubbleOffset
                bottom = top + bubbleHeight
            }

            // Determine if there is enough space to draw above the tooltip
            val drawBelow = bubbleHeight + tailSize + bubbleOffset > anchorRect.top

            // Offset bubble if we have to draw below
            if (drawBelow) bubbleRect.offset(0f, bubbleRect.height() + anchorRect.height() + 2 * (bubbleOffset + tailSize))

            // Draw bubble
            canvas.drawRoundRect(bubbleRect, cornerRadius, cornerRadius, bubblePaint)

            // Draw tail
            with(tailPath) {
                canvas.save()
                canvas.translate(anchorRect.left.toFloat(), anchorRect.top.toFloat() - bubbleOffset)
                if (drawBelow) {
                    canvas.translate(0f, anchorRect.height().toFloat() + (bubbleOffset * 2))
                    canvas.scale(1f, -1f)
                }
                canvas.drawPath(this, bubblePaint)
                canvas.restore()
            }

            // Draw text
            canvas.save()
            canvas.translate(bubbleRect.left + paddingStart, bubbleRect.top + paddingTop)
            layout.draw(canvas)
            canvas.restore()
        }

    }

    /** Subscription to [ShowRatingDescriptionEvent] */
    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showTip(event: ShowRatingDescriptionEvent) {
        if (!TeacherPrefs.hasViewedRubricTutorial && !event.isTutorialTip) {
            TeacherPrefs.hasViewedRubricTutorial = true
            hideTip()
        }
        if (event.anchor == null) {
            hideTip()
        } else if (event.assigneeId == assigneeId) {
            val (x, y) = event.anchor.positionOnScreen
            val (thisX, thisY) = positionOnScreen
            val anchorX = x - thisX + (event.anchor.width / 2)
            val anchorY = y - thisY + computeVerticalScrollOffset()
            val rect = Rect(anchorX, anchorY, anchorX + event.anchor.width, anchorY + event.anchor.height)
            showTip(event.description, rect)
        }
    }

    private fun hideTip() {
        animator?.cancel()
        alpha = 0f
    }

    private fun showTip(text: String, anchorRect: Rect) {
        animator?.cancel()
        this.anchorRect = anchorRect
        val maxWidth = (width - paddingStart - paddingEnd - bubbleMargin.left - bubbleMargin.right).coerceAtMost(maxWidth)
        textLayout = buildLayout(text, maxWidth)
        invalidate()
        alpha = 1f

        val animIn = ObjectAnimator.ofFloat(this@RubricTooltipView, "alpha", 0f, 1f).apply {
            duration = 200
        }

        val animOut = ObjectAnimator.ofFloat(this@RubricTooltipView, "alpha", 1f, 0f).apply {
            val visibleLength = textLayout?.visibleTextLength ?: 0
            startDelay = 3000 + (visibleLength * 20L)
            duration = 200
        }

        if (TeacherPrefs.hasViewedRubricTutorial) {
            animator = AnimatorSet().apply {
                playSequentially(animIn, animOut)
                start()
            }
        } else {
            animIn.start()
        }
    }

    private fun buildLayout(text: String, maxWidth: Int): Layout {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                // Use reflection on pre-Marshmallow devices to expose constructor with max lines parameter
                val constructor = StaticLayout::class.java.getDeclaredConstructor(
                        CharSequence::class.java,
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType,
                        TextPaint::class.java,
                        Int::class.javaPrimitiveType,
                        Layout.Alignment::class.java,
                        TextDirectionHeuristic::class.java,
                        Float::class.javaPrimitiveType,
                        Float::class.javaPrimitiveType,
                        Boolean::class.javaPrimitiveType,
                        TruncateAt::class.java,
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType
                )
                constructor.isAccessible = true
                constructor.newInstance(text, 0, text.length, textPaint, maxWidth,
                        Layout.Alignment.ALIGN_NORMAL, TextDirectionHeuristics.FIRSTSTRONG_LTR, 1f,
                        0f, true, TruncateAt.END, Int.MAX_VALUE, MAX_LINES)
            } catch (e: Exception) {
                @Suppress("DEPRECATION")
                StaticLayout(text, 0, text.length, textPaint, maxWidth, Layout.Alignment.ALIGN_NORMAL,
                        1f, 0f, true, TruncateAt.END, Int.MAX_VALUE)
            }
        } else {
            StaticLayout.Builder
                    .obtain(text, 0, text.length, textPaint, maxWidth)
                    .setMaxLines(MAX_LINES)
                    .setEllipsize(TruncateAt.END)
                    .setIncludePad(true)
                    .build()
        }
    }

    /** Returns the length of the longest line in this layout */
    private fun Layout.maxLineWidth() = (0 until lineCount).map { getLineWidth(it) }.max()?.toInt() ?: 0

    /** Returns the length of the visible text */
    private val Layout.visibleTextLength get() = (0 until lineCount).sumBy { getLineVisibleEnd(it) }

    companion object {
        /** Maximum line count before truncating */
        private const val MAX_LINES = 3

        /** Maximum tooltip width in DP */
        private const val MAX_WIDTH_DP = 400
    }

}
