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


package com.instructure.soseedy.utils

import com.instructure.soseedy.CanvasUser
import com.instructure.soseedy.Course
import com.instructure.soseedy.Enrollment
import java.io.File
import java.time.LocalDateTime

/**
 * Writes seeded data information to disk. This allows information such as
 * user credentials to persist to disk rather than requiring the user to
 * sift through System.out print statements. CSV files will always be written
 * to a folder named "results". Each CSV file will have a unique name based
 * on the date at which the file was written.
 *
 * Example: results/2018-03-05T12:28:09.326.csv
 */
object FileWriter {
    val csvDir: String by lazy {
        val dirName = "results"
        File(dirName).mkdirs()
        dirName
    }

    private fun csvPath(): String = "$csvDir/${LocalDateTime.now()}.csv"

    fun write(user: CanvasUser) {
        File(csvPath()).printWriter().use { out ->
            out.println("User: ${user.loginId}, ${user.password}, ${user.token}")
        }
    }

    fun write(users: List<CanvasUser> = listOf(), courses: List<Course> = listOf(), enrollments: List<Enrollment> = listOf()) {
        File(csvPath()).printWriter().use { out ->
            out.println("Users")
            out.println("Domain, Id, LoginId, Password, Token")
            users.forEach { user ->
                out.println("${user.domain}, ${user.id}, ${user.loginId}, ${user.password}, ${user.token}")
            }

            out.println("\nCourses")
            out.println("Id, Name, Code")
            courses.forEach { course ->
                out.println("${course.id}, ${course.name}, ${course.courseCode}")
            }

            out.println("\nEnrollments")
            out.println("UserId, CourseId, Enrollment Type")
            enrollments.forEach { enrollment ->
                out.println("${enrollment.userId}, ${enrollment.courseId}, ${enrollment.type}")
            }
        }
    }
}
