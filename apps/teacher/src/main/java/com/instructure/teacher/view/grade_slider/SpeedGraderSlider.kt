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
import android.graphics.Rect
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
import com.instructure.pandautils.utils.setVisible
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
    private var isOverGraded: Boolean = false

    init {
        View.inflate(context, R.layout.view_speed_grader_slider, this)

        noGradeButton.setOnClickListener {
            notGraded = true
            updateGrade(null)
        }

        excuseButton.setOnClickListener {
            isExcused = true
            updateGrade(null)
        }

        slider.max = 0
    }

    fun setData(assignment: Assignment, submission: Submission?, assignee: Assignee) {
        this.assignment = assignment
        this.submission = submission
        this.assignee = assignee

        tooltipView.assigneeId = this.assignee.id

        isOverGraded = this.assignment.pointsPossible < this.submission?.score?.toInt() ?: 0

        if (assignment.gradingType?.let { Assignment.getGradingTypeFromAPIString(it) } == Assignment.GradingType.POINTS) {
            if (isOverGraded) {
                if (slider.max < this.submission!!.score.toInt()) {
                    slider.max = this.submission!!.score.toInt()
                    maxGrade.text = NumberHelper.formatDecimal(this.submission!!.score, 0, true)
                }
            } else {
                slider.max = this.assignment.pointsPossible.toInt()
                maxGrade.text = NumberHelper.formatDecimal(this.assignment.pointsPossible, 0, true)
            }
            slider.progress = this.submission?.score?.toInt() ?: 0
            minGrade.text = 0.toString()

        } else if (assignment.gradingType?.let { Assignment.getGradingTypeFromAPIString(it) } == Assignment.GradingType.PERCENT) {
            if (isOverGraded) {
                slider.max = 200
                maxGrade.text = "200%"
            } else {
                slider.max = 100
                maxGrade.text = "100%"
            }
            slider.progress = this.submission?.score?.div(this.assignment.pointsPossible)?.times(100)?.toInt()
                    ?: 0
            minGrade.text = "0%"
        }

        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (assignment.gradingType?.let { Assignment.getGradingTypeFromAPIString(it) } == Assignment.GradingType.PERCENT) {
                    EventBus.getDefault().post(ShowSliderGradeEvent(seekBar, this@SpeedGraderSlider.assignee.id, "$progress%"))
                } else {
                    EventBus.getDefault().post(ShowSliderGradeEvent(seekBar, this@SpeedGraderSlider.assignee.id, progress.toString()))
                }

                notGraded = false
                isExcused = false
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                updateGrade(seekBar?.progress)
            }

        })

    }

    private fun updateGrade(progress: Int?) {
        val grade = when {
            notGraded -> {
                context.getString(R.string.not_graded)
            }
            isExcused -> {
                context.getString(R.string.excused)
            }
            else -> {
                progress.toString()
            }
        }
        if (assignment.gradingType?.let { Assignment.getGradingTypeFromAPIString(it) } == Assignment.GradingType.PERCENT) {
            onGradeChanged("$grade%", isExcused)
        } else {
            onGradeChanged(grade, isExcused)
        }
    }

}