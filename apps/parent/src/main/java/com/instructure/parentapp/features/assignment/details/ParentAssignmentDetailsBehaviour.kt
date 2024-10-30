/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.features.assignment.details

import android.content.Context
import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.pandautils.binding.setTint
import com.instructure.pandautils.databinding.FragmentAssignmentDetailsBinding
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsBehaviour
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.utils.DP
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.studentColor
import com.instructure.parentapp.R
import com.instructure.parentapp.util.ParentPrefs
import javax.inject.Inject


class ParentAssignmentDetailsBehaviour @Inject constructor(
    private val parentPrefs: ParentPrefs,
    private val apiPrefs: ApiPrefs,
): AssignmentDetailsBehaviour() {
    @ColorInt override val dialogColor: Int = parentPrefs.currentStudent.studentColor

    private var fab: FloatingActionButton? = null

    override fun applyTheme(
        activity: FragmentActivity,
        binding: FragmentAssignmentDetailsBinding?,
        bookmark: Bookmarker,
        toolbar: Toolbar,
        course: LiveData<Course>,
        assignment: Assignment?,
        routeToCompose: ((InboxComposeOptions) -> Unit)?
    ) {
        ViewStyler.themeToolbarColored(activity, toolbar, parentPrefs.currentStudent.studentColor, activity.getColor(R.color.textLightest))
        ViewStyler.setStatusBarDark(activity, parentPrefs.currentStudent.studentColor)

        binding?.assignmentDetailsPage?.addView(messageFAB(activity, course, assignment, routeToCompose))

        binding?.scrollView?.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (binding.scrollView.scrollY == 0) {
                fab?.show()
            } else if (binding.scrollView.getChildAt(0).bottom <= (binding.scrollView.height + binding.scrollView.scrollY)) {
                fab?.hide()
            } else if (scrollY < oldScrollY) {
                fab?.show()
            } else if (scrollY > oldScrollY) {
                fab?.hide()
            }
        }
    }

    private fun messageFAB(context: Context, course: LiveData<Course>, assignment: Assignment?, routeToCompose: ((InboxComposeOptions) -> Unit)?): FloatingActionButton {
        return FloatingActionButton(context).apply {
            setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_chat))
            contentDescription = context.getString(R.string.sendMessageAboutAssignment)
            setTint(R.color.textLightest)
            backgroundTintList = ColorStateList.valueOf(parentPrefs.currentStudent.studentColor)
            layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                marginEnd = context.DP(16).toInt()
                bottomMargin = context.DP(16).toInt()
            }
            onClick {
                routeToCompose?.invoke(getInboxComposeOptions(context, course.value, assignment))
            }
        }
        .also { fab = it }
    }

    private fun getInboxComposeOptions(context: Context, course: Course?, assignment: Assignment?): InboxComposeOptions {
        val courseContextId = course?.contextId.orEmpty()
        var options = InboxComposeOptions.buildNewMessage()
        options = options.copy(
            defaultValues = options.defaultValues.copy(
                contextCode = courseContextId,
                contextName = course?.name.orEmpty(),
                subject = context.getString(
                    R.string.regardingHiddenMessageWithAssignmentPrefix,
                    parentPrefs.currentStudent?.name.orEmpty(),
                    assignment?.name.orEmpty()
                )
            ),
            disabledFields = options.disabledFields.copy(
                isContextDisabled = true
            ),
            autoSelectRecipientsFromRoles = listOf(EnrollmentType.TEACHERENROLLMENT),
            hiddenBodyMessage = context.getString(
                R.string.regardingHiddenMessage,
                parentPrefs.currentStudent?.name.orEmpty(),
                getContextURL(course?.id.orDefault(), assignment?.id.orDefault())
            )
        )

        return options
    }

    private fun getContextURL(courseId: Long, assignmentId: Long): String {
        return "${apiPrefs.fullDomain}/courses/$courseId/assignments/$assignmentId"
    }
}