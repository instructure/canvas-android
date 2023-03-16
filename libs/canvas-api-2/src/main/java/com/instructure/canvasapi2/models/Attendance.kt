/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 */
package com.instructure.canvasapi2.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.utils.toDate
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class Attendance(
        @SerializedName("id")
        var statusId: Long? = null, // If null then unmarked; Normally we'd use this for the id override, but since it seems to be dynamic, we can't use it (see override below)
        @SerializedName("student_id")
        val studentId: Long = 0,
        @SerializedName("teacher_id")
        val teacherId: Long = 0,
        @SerializedName("section_id")
        val sectionId: Long = 0,
        @SerializedName("course_id")
        val courseId: Long = 0,
        @SerializedName("student")
        val student: User? = null,
        @SerializedName("class_date")
        var date: String? = null,
        @SerializedName("attendance")
        var attendance: String? = null, // present, absent, or late, unmarked when null
        var _postingAttendance: String? = null, // Used to store the attendance status in case of an API failure.
        @SerializedName("seated")
        val seated: Boolean = false,
        @SerializedName("row")
        val row: Int = 0,
        @SerializedName("col")
        val column: Int = 0
) : CanvasModel<Attendance>(), Parcelable {
    override val comparisonString get() = attendance
    override val id get() = studentId

    enum class Attendance {
        PRESENT, ABSENT, LATE, UNMARKED
    }

    fun attendanceStatus(): Attendance = when (attendance) {
        "present" -> Attendance.PRESENT
        "absent" -> Attendance.ABSENT
        "late" -> Attendance.LATE
        else -> Attendance.UNMARKED
    }

    fun setAttendanceStatus(statusTo: Attendance) {
        attendance = when (statusTo) {
            Attendance.PRESENT -> "present"
            Attendance.ABSENT -> "absent"
            Attendance.LATE -> "late"
            Attendance.UNMARKED -> null
        }
    }

    fun setDate(calendar: Calendar) {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        formatter.timeZone = calendar.timeZone
        date = formatter.format(calendar.time)
    }

    override val comparisonDate: Date? get() = date.toDate()
}