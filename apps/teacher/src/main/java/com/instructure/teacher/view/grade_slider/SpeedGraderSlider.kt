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
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.SeekBar
import com.instructure.canvasapi2.models.Assignee
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.utils.accessibleTouchTarget
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.databinding.ViewSpeedGraderSliderBinding
import org.greenrobot.eventbus.EventBus
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class SpeedGraderSlider @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewSpeedGraderSliderBinding

    var onGradeChanged: (String, Boolean) -> Unit by Delegates.notNull()

    private lateinit var assignment: Assignment
    private var submission: Submission? = null
    private lateinit var assignee: Assignee
    private var maxGradeValue: Int = 0

    private var isExcused: Boolean = false
    private var notGraded: Boolean = false
    private var isOverGraded: Boolean = false

    init {
        binding = ViewSpeedGraderSliderBinding.inflate(LayoutInflater.from(context), this, true)

        binding.noGradeButton.apply {
            setOnClickListener {
                notGraded = true
                updateGrade(null)
            }
            accessibleTouchTarget()
        }

        binding.excuseButton.apply {
            setOnClickListener {
                isExcused = true
                updateGrade(null)
            }
            accessibleTouchTarget()
        }

        binding.minGrade.apply {
            setOnClickListener {
                updateGrade(0)
                notGraded = false
                isExcused = false
            }
            accessibleTouchTarget()
        }

        binding.maxGrade.apply {
            setOnClickListener {
                updateGrade(maxGradeValue)
                notGraded = false
                isExcused = false
            }
            accessibleTouchTarget()
        }

        binding.slider.max = maxGradeValue
    }

    fun setData(assignment: Assignment, submission: Submission?, assignee: Assignee) = with(binding) {
        this@SpeedGraderSlider.assignment = assignment
        this@SpeedGraderSlider.submission = submission
        this@SpeedGraderSlider.assignee = assignee

        tooltipView.assigneeId = this@SpeedGraderSlider.assignee.id

        isOverGraded = this@SpeedGraderSlider.assignment.pointsPossible < this@SpeedGraderSlider.submission?.score?.toInt() ?: 0

        this@SpeedGraderSlider.submission?.let {
            enableExcuseButton(!it.excused)
            enableNoGradeButton(it.isGraded || it.excused)
        }

        if (assignment.gradingType?.let { Assignment.getGradingTypeFromAPIString(it) } == Assignment.GradingType.POINTS) {
            if (isOverGraded) {
                if (slider.max < this@SpeedGraderSlider.submission!!.score.toInt()) {
                    slider.max = this@SpeedGraderSlider.submission!!.score.toInt()
                    maxGrade.text = NumberHelper.formatDecimal(this@SpeedGraderSlider.submission!!.score, 0, true)
                }
                showPointsPossibleView()
            } else {
                slider.max = this@SpeedGraderSlider.assignment.pointsPossible.toInt()
                maxGrade.text = NumberHelper.formatDecimal(this@SpeedGraderSlider.assignment.pointsPossible, 0, true)
                pointsPossibleView.setGone()
            }
            slider.progress = this@SpeedGraderSlider.submission?.score?.toInt() ?: 0
            minGrade.text = 0.toString()

        } else if (assignment.gradingType?.let { Assignment.getGradingTypeFromAPIString(it) } == Assignment.GradingType.PERCENT) {
            if (isOverGraded) {
                slider.max = 200
                maxGrade.text = "200%"
                showPointsPossibleView()
            } else {
                slider.max = 100
                maxGrade.text = "100%"
                pointsPossibleView.setGone()
            }
            slider.progress = this@SpeedGraderSlider.submission?.score?.div(this@SpeedGraderSlider.assignment.pointsPossible)?.times(100)?.toInt()
                ?: 0
            minGrade.text = "0%"
        }

        maxGradeValue = slider.max

        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!isExcused && !notGraded) {
                    if (assignment.gradingType?.let { Assignment.getGradingTypeFromAPIString(it) } == Assignment.GradingType.PERCENT) {
                        EventBus.getDefault()
                            .post(ShowSliderGradeEvent(seekBar, this@SpeedGraderSlider.assignee.id, "$progress%"))
                    } else {
                        EventBus.getDefault().post(
                            ShowSliderGradeEvent(
                                seekBar,
                                this@SpeedGraderSlider.assignee.id,
                                progress.toString()
                            )
                        )
                    }
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
                enableNoGradeButton(false)
                enableExcuseButton(true)
                context.getString(R.string.not_graded)
            }
            isExcused -> {
                enableExcuseButton(false)
                enableNoGradeButton(true)
                context.getString(R.string.excused)
            }
            else -> {
                enableExcuseButton(true)
                enableNoGradeButton(true)
                progress.toString()
            }
        }
        if (assignment.gradingType?.let { Assignment.getGradingTypeFromAPIString(it) } == Assignment.GradingType.PERCENT) {
            onGradeChanged("$grade%", isExcused)
        } else {
            onGradeChanged(grade, isExcused)
        }
    }

    private fun enableExcuseButton(isEnabled: Boolean) {
        binding.excuseButton.apply {
            this.isEnabled = isEnabled
            alpha = if (isEnabled) 1.0F else 0.6F
        }
    }

    private fun enableNoGradeButton(isEnabled: Boolean) {
        binding.noGradeButton.apply {
            this.isEnabled = isEnabled
            alpha = if (isEnabled) 1.0F else 0.6F
        }
    }

    private fun showPointsPossibleView() = with(binding) {
        pointsPossibleView.setVisible(true)
        slider.viewTreeObserver.addOnGlobalLayoutListener {
            val anchorRect = Rect()
            val localRect = Rect()
            val width = slider.width - slider.paddingStart - slider.paddingEnd
            val stepWidth = width.toFloat() / slider.max.toFloat()
            slider.getGlobalVisibleRect(anchorRect)
            slider.getLocalVisibleRect(localRect)

            val label: String
            if (assignment.gradingType?.let { Assignment.getGradingTypeFromAPIString(it) } == Assignment.GradingType.POINTS) {
                anchorRect.left =
                    anchorRect.left + slider.paddingLeft + (this@SpeedGraderSlider.assignment.pointsPossible.toInt() * stepWidth).roundToInt()
                label = NumberHelper.formatDecimal(this@SpeedGraderSlider.assignment.pointsPossible, 0, true)
            } else {
                anchorRect.left = anchorRect.left + slider.paddingLeft + width / 2
                label = "100%"
            }

            anchorRect.right = anchorRect.left + 4
            anchorRect.top = localRect.top
            anchorRect.bottom = localRect.bottom
            pointsPossibleView.showPossiblePoint(anchorRect, label)
        }
    }
}
