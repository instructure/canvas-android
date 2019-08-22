/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.student.util

import android.content.Context
import com.instructure.canvasapi2.managers.ExternalToolManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatterBuilder
import java.util.*

suspend fun Long.isStudioEnabled(): Boolean {
    val context = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, this)
    return ExternalToolManager.getExternalToolsForCanvasContextAsync(context, true).await().dataOrNull?.any {
        it.url?.contains("instructuremedia.com/lti/launch") ?: false
    } ?: false
}

suspend fun Long.getStudioLTITool(): DataResult<LTITool> {
    val canvasContext = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, this)
    val studioLTITool = ExternalToolManager.getExternalToolsForCanvasContextAsync(canvasContext, true).await()
        .dataOrNull?.firstOrNull {
        it.url?.contains("instructuremedia.com/lti/launch") ?: false
    }
    return if (studioLTITool != null)
        DataResult.Success(studioLTITool)
    else DataResult.Fail()
}

fun LTITool.getResourceSelectorUrl(canvasContext: CanvasContext, assignment: Assignment) =
    String.format(Locale.getDefault(), "%s/%s/external_tools/%d/resource_selection?launch_type=homework_submission&assignment_id=%d", ApiPrefs.fullDomain, canvasContext.toAPIString(), this.id, assignment.id)

fun String.toDueAtString(context: Context): String {
    val dueDateTime = OffsetDateTime.parse(this).withOffsetSameInstant(OffsetDateTime.now().offset)
    return context.getString(com.instructure.pandares.R.string.submissionDetailsDueAt, dueDateTime.getShortMonthAndDay(), dueDateTime.getTime())
}

fun OffsetDateTime.getShortMonthAndDay(): String {
    // Get year if the year of the due date isn't the current year
    val pattern = if (LocalDate.now().year != this.year) DateTimeFormatter.ofPattern("MMM d, Y") else DateTimeFormatter.ofPattern("MMM d")
    return format(pattern)
}

fun OffsetDateTime.getTime(): String {
    val pattern = DateTimeFormatterBuilder().appendPattern("h:mm a").toFormatter()
    return format(pattern).toLowerCase()
}
