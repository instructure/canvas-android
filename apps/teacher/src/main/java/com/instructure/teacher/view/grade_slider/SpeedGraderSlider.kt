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
import com.instructure.pandautils.utils.setGone
import com.instructure.teacher.R
import com.instructure.teacher.view.edit_rubric.ShowRatingDescriptionEvent
import kotlinx.android.synthetic.main.view_speed_grader_slider.view.*
import org.greenrobot.eventbus.EventBus

class SpeedGraderSlider @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {


    private lateinit var mAssignment: Assignment
    private var mSubmission: Submission? = null
    private lateinit var mAssignee: Assignee

    private val longPressHandler = Handler()
    private val longPressRunnable = Runnable {
        if (speedGraderSlider.progress == 0) {
            EventBus.getDefault().post(ShowSliderGradeEvent(speedGraderSlider, mAssignee.id, "No grade"))
        } else if (speedGraderSlider.progress == speedGraderSlider.max) {
            EventBus.getDefault().post(ShowSliderGradeEvent(speedGraderSlider, mAssignee.id, "Excused"))
        }
    }

    init {
        View.inflate(context, R.layout.view_speed_grader_slider, this)
    }

    fun setData(assignment: Assignment, submission: Submission?, assignee: Assignee) {
        mAssignment = assignment
        mSubmission = submission
        mAssignee = assignee

        if (assignment.rubric != null && assignment.rubric!!.isNotEmpty()) {
            setGone()
            return
        }

        tooltipView.assigneeId = mAssignee.id
        speedGraderSlider.max = mAssignment.pointsPossible.toInt()

        speedGraderSliderMinGrade.text = 0.toString()
        speedGraderSliderMaxGrade.text = String.format("%.0f", mAssignment.pointsPossible)
        speedGraderSlider.progress = mSubmission?.score?.toInt() ?: 0

        speedGraderSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                EventBus.getDefault().post(ShowSliderGradeEvent(seekBar, mAssignee.id, progress.toString()))
                if (progress == 0 || progress == seekBar?.max) {
                    longPressHandler.postDelayed(longPressRunnable, 1000)
                } else {
                    longPressHandler.removeCallbacks(longPressRunnable)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (seekBar?.progress == 0 || seekBar?.progress == seekBar?.max) {
                    longPressHandler.postDelayed(longPressRunnable, 1000)
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                longPressHandler.removeCallbacks(longPressRunnable)
            }

        })

    }

}