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



package com.instructure.dataseeding.soseedy

import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.EnrollmentsApi
import com.instructure.dataseeding.api.FileUploadsApi
import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.FileUploadType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.Randomizer
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File
import java.io.FileWriter

@Suppress("TestFunctionName")
class FileUploadsTest {
    private val course = CoursesApi.createCourse()
    private val student = UserApi.createCanvasUser()
    private val teacher = UserApi.createCanvasUser()

    private fun createAssignment(): AssignmentApiModel {
        EnrollmentsApi.enrollUserAsTeacher(course.id, teacher.id)
        EnrollmentsApi.enrollUserAsStudent(course.id, student.id)

        return AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                withDescription = false,
                submissionTypes = listOf(SubmissionType.ONLINE_UPLOAD),
                teacherToken = teacher.token
        ))
    }

    private fun randomFile(): File {
        val tmpDir = System.getProperty("java.io.tmpdir")

        val file = File(
                Randomizer.randomTextFileName(tmpDir))
                .apply { createNewFile() }

        FileWriter(file, true).apply {
            write(Randomizer.randomTextFileContents())
            flush()
            close()
        }

        return file
    }

    @Test
    fun UploadFile() {
        val assignment = createAssignment()
        val file = randomFile()

        val response = FileUploadsApi.uploadFile(
                courseId = course.id,
                assignmentId = assignment.id,
                file = file.readBytes(),
                fileName = file.name,
                token = student.token,
                fileUploadType = FileUploadType.ASSIGNMENT_SUBMISSION
        )

        assertThat(response.displayName, `is`(file.name))
        assertThat(response.fileName, `is`(file.name))
    }
}
