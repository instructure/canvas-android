/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.util

import android.content.Context
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.GradingSchemeRow
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.pandautils.utils.ColorUtils
import com.instructure.pandautils.utils.getAssignmentIcon
import com.instructure.pandautils.utils.getGrade
import com.instructure.pandautils.utils.setInvisible
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R

object BinderUtils {

    @Suppress("DEPRECATION")
    fun getHtmlAsText(html: String?) = html?.validOrNull()?.let { StringUtilities.simplifyHTML(Html.fromHtml(it)) }

    fun setupGradeText(
        context: Context,
        textView: TextView,
        assignment: Assignment,
        submission: Submission,
        color: Int,
        restrictQuantitativeData: Boolean,
        gradingScheme: List<GradingSchemeRow>
    ) {
        val (grade, contentDescription) = assignment.getGrade(submission, context.resources, restrictQuantitativeData, gradingScheme)
        if (!submission.excused && grade.isValid()) {
            textView.text = grade
            textView.contentDescription = contentDescription
            textView.setTextAppearance(R.style.TextStyle_Grade)
            textView.background =
                ColorUtils.colorIt(color, ContextCompat.getDrawable(context, R.drawable.grade_background)!!)
        } else {
            textView.text = grade
            textView.setTextAppearance(R.style.TextStyle_NoGrade)
            textView.background = null
            textView.contentDescription = grade
        }
    }

    fun getAssignmentIcon(assignment: Assignment?): Int {
        if (assignment == null) return 0
        return assignment.getAssignmentIcon()
    }

    fun updateShadows(isFirstItem: Boolean, isLastItem: Boolean, top: View, bottom: View) {
        if (isFirstItem) top.setVisible() else top.setInvisible()
        if (isLastItem) bottom.setVisible() else bottom.setInvisible()
    }
}
