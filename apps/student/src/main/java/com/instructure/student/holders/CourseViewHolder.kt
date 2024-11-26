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

package com.instructure.student.holders

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.CourseGrade
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.convertPercentToPointBased
import com.instructure.pandautils.features.dashboard.DashboardCourseItem
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.getContentDescriptionForMinusGradeString
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.setCourseImage
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.databinding.ViewholderCourseCardBinding
import com.instructure.student.interfaces.CourseAdapterToFragmentCallback
import com.instructure.student.util.StudentPrefs

class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    companion object {
        const val HOLDER_RES_ID: Int = R.layout.viewholder_course_card
    }

    @SuppressLint("SetTextI18n")
    fun bind(courseItem: DashboardCourseItem, isOfflineEnabled: Boolean, callback: CourseAdapterToFragmentCallback): Unit = with(ViewholderCourseCardBinding.bind(itemView)) {
        val course = courseItem.course

        titleTextView.text = course.name
        courseCode.text = course.courseCode

        titleTextView.setTextColor(course.color)

        courseImageView.setCourseImage(
            course = course,
            courseColor = course.color,
            applyColor = !StudentPrefs.hideCourseColorOverlay
        )

        courseColorIndicator.backgroundTintList = ColorStateList.valueOf(course.color)
        courseColorIndicator.setVisible(StudentPrefs.hideCourseColorOverlay)

        if (courseItem.available || !isOfflineEnabled) {
            cardView.alpha = 1f
            cardView.isEnabled = true
            overflow.isEnabled = true
        } else {
            cardView.alpha = 0.5f
            cardView.isEnabled = false
            overflow.isEnabled = false
        }

        offlineSyncIcon.setVisible(courseItem.availableOffline)

        cardView.setOnClickListener { callback.onCourseSelected(course)}

        overflow.onClickWithRequireNetwork {
            val popup = PopupMenu(it.context, it, Gravity.START.and(Gravity.TOP))
            val menu = popup.menu

            // Add things to the popup menu
            menu.add(0, 0, 0, R.string.editNickname)
            menu.add(0, 1, 1, R.string.editCourseColor)
            if (isOfflineEnabled) {
                menu.add(0, 2, 2, R.string.course_menu_manage_offline_content)
            }

            // Add click listener
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    0 -> callback.onEditCourseNickname(course)
                    1 -> callback.onPickCourseColor(course)
                    2 -> callback.onManageOfflineContent(course)
                }
                true
            }
            popup.show()
        }
        overflow.contentDescription = root.context.getString(R.string.courseOptionsFormatted, course.name)

        val courseGrade = course.getCourseGrade(false)

        if (StudentPrefs.showGradesOnCard && courseGrade != null) {
            gradeLayout.setVisible()
            if(courseGrade.isLocked) {
                gradeTextView.setGone()
                lockedGradeImage.setVisible()
                lockedGradeImage.setImageDrawable(ColorKeeper.getColoredDrawable(root.context, R.drawable.ic_lock, course.color))
            } else {
                gradeTextView.setVisible()
                lockedGradeImage.setGone()
                setGradeView(
                    gradeTextView,
                    courseGrade,
                    course.color,
                    root.context,
                    course.settings?.restrictQuantitativeData ?: false,
                    course.pointsBasedGradingScheme,
                    course.scalingFactor
                )
            }
        } else {
            gradeLayout.setGone()
        }
    }

    private fun setGradeView(
        textView: TextView,
        courseGrade: CourseGrade,
        color: Int,
        context: Context,
        restrictQuantitativeData: Boolean,
        pointBased: Boolean,
        scalingFactor: Double
    ) {
        if(courseGrade.noCurrentGrade) {
            textView.text = context.getString(R.string.noGradeText)
        } else {
            if (restrictQuantitativeData) {
                if (courseGrade.currentGrade.isNullOrEmpty()) {
                    textView.text = context.getString(R.string.noGradeText)
                } else {
                    textView.text = "${courseGrade.currentGrade.orEmpty()}"
                    textView.contentDescription = getContentDescriptionForMinusGradeString(
                        courseGrade.currentGrade.orEmpty(),
                        context
                    )
                }
            } else {
                val scoreString = if (pointBased) {
                    convertPercentToPointBased(courseGrade.currentScore.orDefault(), scalingFactor)
                } else {
                    NumberHelper.doubleToPercentage(courseGrade.currentScore, 2)
                }
                textView.text =
                    if (courseGrade.hasCurrentGradeString()) "${courseGrade.currentGrade} $scoreString" else scoreString
                textView.contentDescription = getContentDescriptionForMinusGradeString(
                    courseGrade.currentGrade ?: "",
                    context
                )
            }
        }
        textView.setTextColor(color)
    }

}
