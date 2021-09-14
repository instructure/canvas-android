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
package com.instructure.canvasapi2

import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.instructure.canvasapi2.models.SubmissionState
import java.lang.reflect.Type

class SubmissionStateTypeAdapter : JsonDeserializer<SubmissionState?> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): SubmissionState? {
        if (json?.isJsonObject == true) {
            val submitted = json.asJsonObject?.get("submitted")?.asBoolean ?: false
            val missing = json.asJsonObject?.get("missing")?.asBoolean ?: false
            val late = json.asJsonObject?.get("late")?.asBoolean ?: false
            val excused = json.asJsonObject?.get("excused")?.asBoolean ?: false
            val graded = json.asJsonObject?.get("graded")?.asBoolean ?: false
            val needsGrading = json.asJsonObject?.get("needs_grading")?.asBoolean ?: false
            val withFeedback = json.asJsonObject?.get("with_feedback")?.asBoolean ?: false
            val redoRequest = json.asJsonObject?.get("redo_request")?.asBoolean ?: false
            return SubmissionState(submitted, missing, late, excused, graded, needsGrading, withFeedback, redoRequest)
        } else {
            return null
        }
    }

}