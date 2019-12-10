/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.interaction

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getArguments
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.instructure.annotations.BuildConfig
import com.instructure.annotations.FileCaching.FileCache
import com.instructure.annotations.FileCaching.SimpleDiskCache
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.mockCanvas.*
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.routeTo
import com.instructure.student.ui.utils.tokenLogin
import org.junit.Assert
import org.junit.Test
import java.util.*

class PdfInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true, FeatureCategory.ANNOTATIONS)
    fun testAnnotations_viewPdfSubmission() {
        // Attachment content type must be pdf
        // attachment.contentType == "application/pdf
        // preview url must be valid "url" to assets/samplepdf.pdf
        goToAssignmentPdfSubmission()
        // TODO: Make this file show up in the files list or find another way to confirm the pdf is open
//        submissionDetailsPage.assertFileDisplayed(pdfFileName)
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true, FeatureCategory.ANNOTATIONS)
    fun testAnnotations_viewAndSelectAnnotationsInSubmission() {

    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true, FeatureCategory.ANNOTATIONS)
    fun testAnnotations_selectAndCommentOnAnnotationWithNoExistingComments() {

    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true, FeatureCategory.ANNOTATIONS)
    fun testAnnotations_selectAndCommentOnAnnotationWithExistingComments() {

    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.FILES, TestCategory.INTERACTION, true, FeatureCategory.ANNOTATIONS)
    fun testAnnotations_openPdfFilesInPSPDFKit() {
        // Annotation toolbar icon needs to be present
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, true, FeatureCategory.ANNOTATIONS)
    fun testAnnotations_openPdfsInPSPDFKitFromLinksInAssignment() {
        // Annotation toolbar icon needs to be present, this link is specific to assignment details, as that was the advertised use case
    }

//    val pdfUrl = "https://mock-data.instructure.com/canvadoc?=samplepdf.pdf"
//    val pdfDownloadUrl = """https://mock-data.instructure.com/api/v1/sessions/eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJjIjoxNTc1ODU5MDE0Njc1LCJkIjoidFZkZDJCZ2sxb3d4c3I1cGJXajJMQlpkbWk5Wm5mIiwiZSI6MTU3NTg5NTAxNCwiciI6InBkZmpzIiwibCI6ImVuIiwiZyI6InVzLWVhc3QtMSIsImgiOnt9LCJhIjp7ImMiOiJkZWZhdWx0IiwicCI6InJlYWR3cml0ZSIsImwiOiJibWNBTSIsInUiOiIxMDAwMDAwNTgzNDgxNyIsIm4iOiJIb2RvciAoaGUvaGltL2hpcykiLCJyIjoic3R1ZGVudCJ9LCJpYXQiOjE1NzU4NTkwMTQsImV4cCI6MTU3NTg5NTAxM30.9fGuDynCPJzOKSdC-w3GVUa3dl9SA-plWjD0Du47L3DZGg4wb3W2RQq_KIrJ0Wfjcgx2irrHyg5I-5daeG_Qeg/file/file.pdf"""
//    val sessionPdfDownloadUrl = """https://mock-data.instructure.com/1/sessions/eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJjIjoxNTc1ODU5MDE0Njc1LCJkIjoidFZkZDJCZ2sxb3d4c3I1cGJXajJMQlpkbWk5Wm5mIiwiZSI6MTU3NTg5NTAxNCwiciI6InBkZmpzIiwibCI6ImVuIiwiZyI6InVzLWVhc3QtMSIsImgiOnt9LCJhIjp7ImMiOiJkZWZhdWx0IiwicCI6InJlYWR3cml0ZSIsImwiOiJibWNBTSIsInUiOiIxMDAwMDAwNTgzNDgxNyIsIm4iOiJIb2RvciAoaGUvaGltL2hpcykiLCJyIjoic3R1ZGVudCJ9LCJpYXQiOjE1NzU4NTkwMTQsImV4cCI6MTU3NTg5NTAxM30.9fGuDynCPJzOKSdC-w3GVUa3dl9SA-plWjD0Du47L3DZGg4wb3W2RQq_KIrJ0Wfjcgx2irrHyg5I-5daeG_Qeg/file/file.pdf"""
    val pdfFileName = "samplepdf.pdf"
    private lateinit var assignment: Assignment
    private lateinit var attachment: Attachment

    private fun goToAssignmentPdfSubmission() {
        // Test clicking on the Submission and Rubric button to load the Submission Details Page
        val data = MockCanvas.init(
                studentCount = 1,
                courseCount = 1
        )

        val course = data.courses.values.first()
        val student = data.students[0]
        val teacher = data.teachers[0]
        val token = data.tokenFor(student)!!
        data.addAssignmentsToGroups(course)
        val docSession = data.addAnnotations(student, teacher)
        assignment = data.assignments.values.first()

        val previewUrl = """/api/v1/canvadoc_session?blob={"moderated_grading_whitelist":null,"enable_annotations":true,
        |"enrollment_type":"student","anonymous_instructor_annotations":false,"submission_id":90429763,
        |"user_id":10000005834817,"attachment_id":159367362,"type":"canvadoc"}
        |&hmac=5960bf0d43b5b710e0b3e4e8af3cdafb7810fe0b""".trimMargin()

        attachment = Attachment(
            id = data.newItemId(),
            contentType = "application/pdf",
            displayName = pdfFileName,
            filename = pdfFileName,
            previewUrl = previewUrl,
            createdAt = Date()
        )

        val submission = data.addSubmissionForAssignment(
            assignmentId = assignment.id,
            userId = student.id,
            type = Assignment.SubmissionType.ONLINE_UPLOAD.apiString,
            attachment = attachment
        )

        setupCache(docSession.annotationUrls.pdfDownload)

        tokenLogin(data.domain, token, student)
        routeTo("courses/${course.id}/assignments", data.domain)

        // Let's find and click an assignment with a submission, so that we get meaningful
        // data in the submission details.
        Assert.assertNotNull("Expected at least one assignment with a submission", assignment)
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.goToSubmissionDetails()
    }

    private fun setupCache(pdfUrl: String) {
        val inputStream = InstrumentationRegistry.getInstrumentation().context.resources.assets.open(pdfFileName)
        FileCache.putInputStream("https://mock-data.instructure.com$pdfUrl", inputStream)
    }

}