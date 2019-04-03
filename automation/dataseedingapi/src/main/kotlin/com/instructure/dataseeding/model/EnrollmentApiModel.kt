//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//


package com.instructure.dataseeding.model

import com.google.gson.annotations.SerializedName

object EnrollmentTypes {
        const val STUDENT_ENROLLMENT = "StudentEnrollment"
        const val TEACHER_ENROLLMENT = "TeacherEnrollment"
        const val TA_ENROLLMENT = "TaEnrollment"
        const val DESIGNER_ENROLLMENT = "DesignerEnrollment"
        const val OBSERVER_ENROLLMENT = "ObserverEnrollment"
}

data class EnrollmentApiRequestModel(
        @SerializedName("user_id")
        val userId: Long,
        val type: String,
        val role: String = "",
        @SerializedName("enrollment_state")
        val enrollmentState: String = "active",
        @SerializedName("associated_user_id")
        val associatedUserId: Long? = null
)

data class EnrollmentApiModel(
        @SerializedName("course_id")
        val courseId: Long,
        @SerializedName("course_section_id")
        val sectionId: Long,
        @SerializedName("user_id")
        val userId: Long,
        val type: String,
        val role: String,
        @SerializedName("enrollment_state")
        val enrollmentState: String
)

data class CreateEnrollmentApiRequestModel(
        val enrollment: EnrollmentApiRequestModel
)
