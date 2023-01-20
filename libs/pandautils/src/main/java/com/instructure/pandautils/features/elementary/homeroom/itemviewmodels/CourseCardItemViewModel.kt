/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.features.elementary.homeroom.itemviewmodels

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.homeroom.CourseCardViewData
import com.instructure.pandautils.mvvm.ItemViewModel

class CourseCardItemViewModel(
    val data: CourseCardViewData,
    val onCardClick: () -> Unit,
    val onDueTextClick: () -> Unit,
    val onAnnouncementClick: () -> Unit
) : ItemViewModel {

    override val layoutId: Int = R.layout.item_course_card

    fun getAssignmentsInfo(context: Context): SpannableString {
        if (data.assignmentsMissingText.isEmpty()) {
            return SpannableString(data.assignmentsDueText)
        } else {
            val separator = " | "
            val completeString = SpannableString(data.assignmentsDueText + separator + data.assignmentsMissingText)
            val spanColor = context.getColor(R.color.textDanger)
            completeString.setSpan(ForegroundColorSpan(spanColor), data.assignmentsDueText.length + separator.length, completeString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return completeString
        }
    }
}