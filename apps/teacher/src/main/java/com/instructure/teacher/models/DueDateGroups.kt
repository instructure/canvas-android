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
package com.instructure.teacher.models

import android.os.Parcelable
import com.instructure.canvasapi2.models.CanvasComparable
import com.instructure.canvasapi2.utils.toApiString
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class CoreDates(
        var dueDate: Date? = null,
        var lockDate: Date? = null,
        var unlockDate: Date? = null
) : Parcelable

@Parcelize
data class DueDateGroup(
        var isEveryone: Boolean = false,
        var sectionIds: List<Long> = emptyList(),
        var groupIds: List<Long> = emptyList(),
        var studentIds: List<Long> = emptyList(),
        var coreDates: CoreDates = CoreDates()
) : CanvasComparable<DueDateGroup>() {
    override val id get() = hashCode().toLong()
    override val comparisonDate get() = coreDates.dueDate ?: coreDates.unlockDate ?: coreDates.lockDate
    override val comparisonString get() = coreDates.dueDate?.toApiString() ?: coreDates.unlockDate?.toApiString() ?: coreDates.unlockDate?.toApiString() ?: ""

    val hasOverrideAssignees: Boolean
    get() = sectionIds.isNotEmpty() || groupIds.isNotEmpty() || studentIds.isNotEmpty()
}