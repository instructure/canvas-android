package com.instructure.teacher.view.grade_slider

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import com.instructure.teacher.utils.TeacherPrefs
import com.instructure.teacher.view.TooltipView
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SpeedGraderSliderTooltipView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TooltipView(context, attrs, defStyleAttr) {

    /** Subscription to [ShowSliderGradeEvent] */
    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showTip(event: ShowSliderGradeEvent) {
        if (!TeacherPrefs.hasViewedRubricTutorial && !event.isTutorialTip) {
            TeacherPrefs.hasViewedRubricTutorial = true
            hideTip()
        }
        if (event.seekBar == null) {
            hideTip()
        } else if (event.assigneeId == assigneeId) {
            val thumbRect = event.seekBar.thumb.bounds
            val rect = Rect(thumbRect)
            //This is needed because of the changing thumb boundaries
            rect.left = thumbRect.left + (3 * thumbRect.width()) / 2
            showTip(event.description, rect)
        }
    }
}