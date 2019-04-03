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

import com.instructure.soseedy.Course
import com.instructure.soseedy.Enrollment
import com.instructure.soseedy.producers.EnrollmentProducer.produceEnrollment
import com.instructure.soseedy.producers.Pipelines.coursesPipeline
import com.instructure.soseedy.producers.Pipelines.usersPipeline
import com.instructure.soseedy.utils.FileWriter.write
import kotlinx.coroutines.experimental.runBlocking
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(
        name        = "SeedCourses",
        header      = ["Create 1 user with many course enrollments."],
        description = [
            "Creates 1 user and enrolls the user in any number of courses.",
            "Each enrollment type will be the same and is specified by a parameter."]
)
object SeedCourses : Task() {
    @Option(names       = ["-n"],
            paramLabel  = "<num>",
            description = ["The number of course enrollments for the user."],
            required    = true)
    private var numberOfCourses: Int = 0

    @Option(names       = ["-r"],
            paramLabel  = "<role>",
            description = [
                "The enrollment role for the user in each course.",
                "Allowed inputs: 'STUDENT', 'TEACHER', 'TA', 'OBSERVER'."],
            required    = true)
    private var role: String = ""

    override fun run() = runBlocking {
        role = checkEnrollmentType(role)

        val user = usersPipeline.receive()
        val courses = mutableListOf<Course>()
        val enrollments = mutableListOf<Enrollment>()

        repeat(numberOfCourses) {
            val course = coursesPipeline.receive()
            courses.add(course)
            enrollments.add(produceEnrollment(user, course, role).receive())
        }

        write(listOf(user), courses, enrollments)
    }
}
