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
 */
package com.instructure.teacher.ui

import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.SubmissionApiModel
import com.instructure.dataseeding.model.SubmissionListApiModel
import com.instructure.espresso.ditto.Ditto
import com.instructure.espresso.ditto.DittoMode
import com.instructure.dataseeding.model.FileType
import com.instructure.dataseeding.model.SubmissionType.ONLINE_UPLOAD
import com.instructure.teacher.ui.utils.*
import org.junit.Test

class SpeedGraderFilesPageTest : TeacherTest() {

    // MBL-11593: We set DittoMode to LIVE for all of these tests because they potentially
    //            involve the downloading of PDF files, which can mess up the Ditto/OkReplay
    //            yaml logic.

    @Test
    @Ditto(mode= DittoMode.LIVE)
    override fun displaysPageObjects() {
        goToSpeedGraderFilesPage(
                submissions = listOf(
                        SubmissionsApi.SubmissionSeedInfo(
                                amount = 1,
                                submissionType = ONLINE_UPLOAD,
                                fileType = FileType.TEXT
                        )
                )
        )
        speedGraderFilesPage.assertPageObjects()
    }

    // MBL-12387: This is broken right now due to a canvas bug.
//    @Test
//    @Ditto(mode= DittoMode.LIVE)
//    fun displaysEmptyFilesView() {
//        goToSpeedGraderFilesPage()
//        speedGraderFilesPage.assertDisplaysEmptyView()
//    }

    @Test
    @Ditto(mode= DittoMode.LIVE)
    fun displaysFilesList() {
        val submissions = goToSpeedGraderFilesPage(
                submissions = listOf(
                        SubmissionsApi.SubmissionSeedInfo(
                                amount = 1,
                                submissionType = ONLINE_UPLOAD,
                                fileType = FileType.TEXT
                        )
                )
        )
        speedGraderFilesPage.assertHasFiles(submissions.submissionList[0].submissionAttachments!!.toMutableList())
    }

    @Test
    @Ditto(mode= DittoMode.LIVE)
    fun displaysSelectedFile() {
        goToSpeedGraderFilesPage(
                submissions = listOf(
                        SubmissionsApi.SubmissionSeedInfo(
                                amount = 1,
                                submissionType = ONLINE_UPLOAD,
                                fileType = FileType.TEXT
                        )
                )
        )
        val position = 0

        speedGraderFilesPage.selectFile(position)
        speedGraderFilesPage.assertFileSelected(position)
    }

    private fun goToSpeedGraderFilesPage(assignments: Int = 1,
                                         withDescription: Boolean = false,
                                         lockAt: String = "",
                                         unlockAt: String = "",
                                         submissions: List<SubmissionsApi.SubmissionSeedInfo> = emptyList(),
                                         submissionComments: List<SubmissionsApi.CommentSeedInfo> = emptyList()): SubmissionListApiModel {
        val data = seedData(teachers = 1, favoriteCourses = 1, students = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student = data.studentsList[0]
        val assignments = seedAssignments(
                assignments = assignments,
                courseId = course.id,
                withDescription = withDescription,
                lockAt = lockAt,
                unlockAt = unlockAt,
                submissionTypes = submissions.map { it.submissionType },
                teacherToken = teacher.token)

        val submissionList = seedAssignmentSubmission(
                submissionSeeds = submissions,
                assignmentId = assignments.assignmentList[0].id,
                courseId = course.id,
                studentToken = if (data.studentsList.isEmpty()) "" else data.studentsList[0].token,
                commentSeeds = submissionComments
        )

        tokenLogin(teacher)
        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignments.assignmentList[0])
        assignmentDetailsPage.openSubmissionsPage()
        assignmentSubmissionListPage.clickSubmission(student)

        speedGraderPage.selectFilesTab()
        return submissionList
    }
}
