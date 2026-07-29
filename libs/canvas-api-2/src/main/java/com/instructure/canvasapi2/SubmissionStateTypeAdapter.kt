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
import com.instructure.canvasapi2.utils.toDate
import java.lang.reflect.Type

class SubmissionStateTypeAdapter : JsonDeserializer<SubmissionState?> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): SubmissionState? {
        if (json?.isJsonObject == true) {
            val submitted = json.asJsonObject.optBoolean("submitted")
            val missing = json.asJsonObject.optBoolean("missing")
            val late = json.asJsonObject.optBoolean("late")
            val excused = json.asJsonObject.optBoolean("excused")
            val graded = json.asJsonObject.optBoolean("graded")
            val needsGrading = json.asJsonObject.optBoolean("needs_grading")
            val withFeedback = json.asJsonObject.optBoolean("with_feedback")
            val redoRequest = json.asJsonObject.optBoolean("redo_request")
            val postedAt = json.asJsonObject?.get("posted_at")?.takeIf { !it.isJsonNull }?.asString?.toDate()
            return SubmissionState(submitted, missing, late, excused, graded, needsGrading, withFeedback, redoRequest, postedAt)
        } else {
            return null
        }
    }

    /**
     * Reads a boolean field from this [JsonObject], treating both an absent key and a
     * present key whose value is JSON `null` as `false`. Gson's [JsonObject.get] returns a
     * Kotlin null only when the key is absent; when the key is present with a JSON `null`
     * value it returns a [JsonNull] instance, whose [JsonElement.getAsBoolean] throws.
     */
    private fun JsonObject.optBoolean(key: String): Boolean =
        get(key)?.takeIf { !it.isJsonNull }?.asBoolean ?: false

}