/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.parentapp.features.calendarevent

import android.content.res.Resources
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.pandautils.features.calendarevent.details.EventViewModelBehavior
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.utils.eventHtmlUrl
import com.instructure.parentapp.R
import com.instructure.parentapp.util.ParentPrefs


class ParentEventViewModelBehavior(
    private val resources: Resources,
    private val parentPrefs: ParentPrefs
) : EventViewModelBehavior {

    override val shouldShowMessageFab = true

    override fun getInboxComposeOptions(canvasContext: CanvasContext?, event: ScheduleItem): InboxComposeOptions {
        val courseContextId = canvasContext?.contextId.orEmpty()
        var options = InboxComposeOptions.buildNewMessage()
        options = options.copy(
            defaultValues = options.defaultValues.copy(
                contextCode = courseContextId,
                contextName = canvasContext?.name.orEmpty(),
                subject = resources.getString(
                    R.string.regardingHiddenMessageWithEventPrefix,
                    parentPrefs.currentStudent?.name.orEmpty(),
                    event.title.orEmpty()
                )
            ),
            disabledFields = options.disabledFields.copy(
                isContextDisabled = true
            ),
            autoSelectRecipientsFromRoles = listOf(EnrollmentType.TeacherEnrollment),
            hiddenBodyMessage = resources.getString(
                R.string.regardingHiddenMessage,
                parentPrefs.currentStudent?.name.orEmpty(),
                event.eventHtmlUrl.orEmpty()
            )
        )

        return options
    }

    override fun updateWidget() = Unit
}
