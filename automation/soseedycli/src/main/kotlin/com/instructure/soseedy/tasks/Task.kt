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


package com.instructure.soseedy.tasks

import com.instructure.dataseeding.model.EnrollmentTypes
import com.instructure.soseedy.utils.InvalidEnrollmentException
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(
        sortOptions          = false,
        headerHeading        = "@|bold,underline Usage:|@%n%n",
        synopsisHeading      = "%n",
        descriptionHeading   = "%n@|bold,underline Description:|@%n%n",
        parameterListHeading = "%n@|bold,underline Parameters:|@%n",
        optionListHeading    = "%n@|bold,underline Options:|@%n"
)
abstract class Task : Runnable {
    val validRoles = listOf("student", "teacher", "ta", "observer")

    @Option(names       = ["-h", "--help"],
            usageHelp   = true,
            description = ["Display this help and exit"])
    var help: Boolean = false

    fun checkEnrollmentType(role: String): String {
        if (!validRoles.contains(role.toLowerCase())) {
            throw InvalidEnrollmentException("Invalid Enrollment Type! Expected ${SeedCourses.validRoles.map { "'$it'"}}. Got '$role'")
        }

        return when (role.toLowerCase()) {
            "teacher"  -> EnrollmentTypes.TEACHER_ENROLLMENT
            "ta"       -> EnrollmentTypes.TA_ENROLLMENT
            "observer" -> EnrollmentTypes.OBSERVER_ENROLLMENT
            else       -> EnrollmentTypes.STUDENT_ENROLLMENT
        }
    }
}
