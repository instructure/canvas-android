/*
 * Copyright (C) 2020 - present  Instructure, Inc.
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
package com.instructure.teacher.view.grade_slider

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import com.instructure.canvasapi2.models.Assignee
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.utils.setGone
import com.instructure.teacher.R
import kotlinx.android.synthetic.main.view_speed_grader_slider.view.*
import org.greenrobot.eventbus.EventBus
import kotlin.properties.Delegates

class SpeedGraderSlider @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var onGradeChanged: (String, Boolean) -> Unit by Delegates.notNull()

    private lateinit var assignment: Assignment
    private var submission: Submission? = null
    private lateinit var assignee: Assignee

    private var isExcused: Boolean = false
    private var notGraded: Boolean = false

    private val longPressHandler = Handler()
    private val longPressRunnable = Runnable {
        if (slider.progress == 0) {
            EventBus.getDefault().post(ShowSliderGradeEvent(slider, assignee.id, context.getString(R.string.not_graded)))
            notGraded = true
        } else if (slider.progress == slider.max) {
            EventBus.getDefault().post(ShowSliderGradeEvent(slider, assignee.id, context.getString(R.string.excused)))
            isExcused = true
        }
    }

    init {
        View.inflate(context, R.layout.view_speed_grader_slider, this)
    }

    fun setData(assignment: Assignment, submission: Submission?, assignee: Assignee) {
        this.assignment = assignment
        this.submission = submission
        this.assignee = assignee

        tooltipView.assigneeId = this.assignee.id

        if (assignment.gradingType?.let { Assignment.getGradingTypeFromAPIString(it) } == Assignment.GradingType.POINTS) {
            slider.max = this.assignment.pointsPossible.toInt()
            slider.progress = this.submission?.score?.toInt() ?: 0

            minGrade.text = 0.toString()
            maxGrade.text = NumberHelper.formatDecimal(this.assignment.pointsPossible, 0, true)
        } else if (assignment.gradingType?.let { Assignment.getGradingTypeFromAPIString(it) } == Assignment.GradingType.PERCENT) {
            slider.max = 100
            slider.progress = this.submission?.score?.div(this.assignment.pointsPossible)?.times(100)?.toInt()
                    ?: 0

            minGrade.text = "0%"
            maxGrade.text = "100%"
        }



        if (this.submission?.excused == true) {
            slider.progress = slider.max
        }

        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (assignment.gradingType?.let { Assignment.getGradingTypeFromAPIString(it) } == Assignment.GradingType.PERCENT) {
                        EventBus.getDefault().post(ShowSliderGradeEvent(seekBar, this@SpeedGraderSlider.assignee.id, "$progress%"))
                    } else {
                        EventBus.getDefault().post(ShowSliderGradeEvent(seekBar, this@SpeedGraderSlider.assignee.id, progress.toString()))
                    }
                    if (progress == 0 || progress == seekBar?.max) {
                        startLongPressHandler()
                    } else {
                        stopLongPressHandler()
                    }
                }
                notGraded = false
                isExcused = false
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (seekBar?.progress == 0 || seekBar?.progress == seekBar?.max) {
                    startLongPressHandler()
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                stopLongPressHandler()
                updateGrade(seekBar?.progress)
            }

        })

    }

    private fun updateGrade(progress: Int?) {
        val grade = if (notGraded) {
            context.getString(R.string.not_graded)
        } else {
            progress.toString()
        }
        if (assignment.gradingType?.let { Assignment.getGradingTypeFromAPIString(it) } == Assignment.GradingType.PERCENT) {
            onGradeChanged("$grade%", isExcused)
        } else {
            onGradeChanged(grade, isExcused)
        }
    }

    private fun startLongPressHandler() {
        longPressHandler.postDelayed(longPressRunnable, 1000)
    }

    private fun stopLongPressHandler() {
        longPressHandler.removeCallbacks(longPressRunnable)
    }

}