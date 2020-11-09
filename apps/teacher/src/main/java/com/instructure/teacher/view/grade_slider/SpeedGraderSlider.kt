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
package com.instructure.teacher.view.grade_slider

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import com.instructure.canvasapi2.models.Assignee
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.utils.setGone
import com.instructure.teacher.R
import com.instructure.teacher.view.edit_rubric.ShowRatingDescriptionEvent
import kotlinx.android.synthetic.main.view_speed_grader_slider.view.*
import org.greenrobot.eventbus.EventBus
import kotlin.properties.Delegates

class SpeedGraderSlider @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var onGradeChanged: (String, Boolean) -> Unit by Delegates.notNull()

    private lateinit var mAssignment: Assignment
    private var mSubmission: Submission? = null
    private lateinit var mAssignee: Assignee

    private var isExcused: Boolean = false
    private var notGraded: Boolean = false

    private val longPressHandler = Handler()
    private val longPressRunnable = Runnable {
        if (slider.progress == 0) {
            EventBus.getDefault().post(ShowSliderGradeEvent(slider, mAssignee.id, context.getString(R.string.not_graded)))
            notGraded = true
        } else if (slider.progress == slider.max) {
            EventBus.getDefault().post(ShowSliderGradeEvent(slider, mAssignee.id, context.getString(R.string.excused)))
            isExcused = true
        }
    }

    init {
        View.inflate(context, R.layout.view_speed_grader_slider, this)
    }

    fun setData(assignment: Assignment, submission: Submission?, assignee: Assignee) {
        mAssignment = assignment
        mSubmission = submission
        mAssignee = assignee

        if (mAssignment.rubric != null && mAssignment.rubric!!.isNotEmpty() && mAssignment.gradingType?.let { Assignment.getGradingTypeFromAPIString(it) } == Assignment.GradingType.POINTS) {
            setGone()
            return
        }

        tooltipView.assigneeId = mAssignee.id

        slider.max = mAssignment.pointsPossible.toInt()
        slider.progress = mSubmission?.score?.toInt() ?: 0

        minGrade.text = 0.toString()
        maxGrade.text = NumberHelper.formatDecimal(mAssignment.pointsPossible, 0, true)

        if (mSubmission?.excused == true) {
            slider.progress = slider.max
        }

        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    announceForAccessibility(progress.toString())
                    EventBus.getDefault().post(ShowSliderGradeEvent(seekBar, mAssignee.id, progress.toString()))
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
        onGradeChanged(grade, isExcused)
    }

    private fun startLongPressHandler() {
        longPressHandler.postDelayed(longPressRunnable, 1000)
    }

    private fun stopLongPressHandler() {
        longPressHandler.removeCallbacks(longPressRunnable)
    }

}