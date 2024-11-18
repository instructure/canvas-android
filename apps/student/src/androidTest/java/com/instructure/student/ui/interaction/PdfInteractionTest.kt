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

import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.annotations.FileCaching.FileCache
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAnnotation
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addAssignmentsToGroups
import com.instructure.canvas.espresso.mockCanvas.addFileToCourse
import com.instructure.canvas.espresso.mockCanvas.addSubmissionForAssignment
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.loaders.OpenMediaAsyncTaskLoader
import com.instructure.student.R
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.routeTo
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.Date

@HiltAndroidTest
class PdfInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    val pdfFileName = "samplepdf.pdf"
    private lateinit var assignment: Assignment
    private lateinit var attachment: Attachment

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, SecondaryFeatureCategory.SUBMISSIONS_ANNOTATIONS)
    fun testAnnotations_viewPdfSubmission() {
        goToAssignmentPdfSubmission()
        submissionDetailsPage.assertFileDisplayed(pdfFileName)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, SecondaryFeatureCategory.SUBMISSIONS_ANNOTATIONS)
    fun testAnnotations_viewAndSelectAnnotationsInSubmission() {
        goToAssignmentPdfSubmission()
        submissionDetailsPage.clickSubmissionContentAtPosition(.5f, .5f)
        submissionDetailsPage.assertPdfAnnotationSelected()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, SecondaryFeatureCategory.SUBMISSIONS_ANNOTATIONS)
    fun testAnnotations_selectAndCommentOnAnnotationWithNoExistingComments() {
        val sentCommentContents = "what up dog"
        // Configure the comment to be sent in mock Canvas
        goToAssignmentPdfSubmission(hasSentComment = true, sentCommentContents = sentCommentContents)
        submissionDetailsPage.clickSubmissionContentAtPosition(.5f, .5f)
        // Send first comment through dialog
        submissionDetailsPage.addFirstAnnotationComment(sentCommentContents)
        submissionDetailsPage.openPdfComments()
        annotationCommentListPage.assertCommentDisplayed(sentCommentContents)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, SecondaryFeatureCategory.SUBMISSIONS_ANNOTATIONS)
    fun testAnnotations_selectAndCommentOnAnnotationWithExistingComments() {
        val sentCommentContents = "what up dog"
        // Configure the comment to be sent in mock Canvas and the existing comment
        goToAssignmentPdfSubmission(hasComment = true, hasSentComment = true, commentContents =  "hodor", sentCommentContents = sentCommentContents)
        submissionDetailsPage.clickSubmissionContentAtPosition(.5f, .5f)
        submissionDetailsPage.openPdfComments()
        // Send new comment through AnnotationCommentList
        annotationCommentListPage.sendComment(sentCommentContents)
        annotationCommentListPage.assertCommentDisplayed(sentCommentContents)
    }


    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.FILES, TestCategory.INTERACTION, SecondaryFeatureCategory.SUBMISSIONS_ANNOTATIONS)
    fun testAnnotations_openPdfFilesInPSPDFKit() {
        // Annotation toolbar icon needs to be present
        val data = getToCourse()
        val course = data.courses.values.first()
        val student = data.students[0]

        data.addFileToCourse(
                courseId = course.id,
                displayName = pdfFileName,
                contentType = "application/pdf")

        val uniqueFileName = OpenMediaAsyncTaskLoader.makeFilenameUnique(pdfFileName, data.folderFiles.values.first().first().url!!)

        cacheFile(student.id.toString(), uniqueFileName)

        courseBrowserPage.selectFiles()
        fileListPage.selectItem(pdfFileName)
        fileListPage.assertPdfPreviewDisplayed()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, SecondaryFeatureCategory.SUBMISSIONS_ANNOTATIONS)
    fun testAnnotations_openPdfsInPSPDFKitFromLinksInAssignment() {
        // Annotation toolbar icon needs to be present, this link is specific to assignment details, as that was the advertised use case
        val data = MockCanvas.init(
                studentCount = 1,
                courseCount = 1
        )

        val course = data.courses.values.first()
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        data.addAssignmentsToGroups(course)
        tokenLogin(data.domain, token, student)
        routeTo("courses/${course.id}/assignments", data.domain)

        val fileId = data.newItemId()
        val url = """https://mock-data.instructure.com/courses/${course.id}/files/${fileId}/download?"""

        data.addFileToCourse(
            courseId = course.id,
            displayName = pdfFileName,
            contentType = "application/pdf",
            fileId = fileId,
            url = url
        )

        val uniqueFileName = OpenMediaAsyncTaskLoader.makeFilenameUnique(pdfFileName, url, fileId.toString())

        cacheFile(student.id.toString(), uniqueFileName)

        val pdfUrlElementId = "testLinkElement"
        val assignmentDescriptionHtml = """<a id="$pdfUrlElementId" href="$url">pdf baby!!!</a>"""

        val assignment = data.addAssignment(courseId = course.id, submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_UPLOAD), description = assignmentDescriptionHtml)

        assignmentListPage.waitForPage()
        assignmentListPage.refresh()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.assertAssignmentDetails(assignment)

        // Scroll to the description, as it will likely be offscreen for landscape tests
        assignmentDetailsPage.scrollToAssignmentDescription()

        // Click the url in the description to load the pdf
        Web.onWebView(withId(R.id.contentWebView))
            .withElement(DriverAtoms.findElement(Locator.ID, pdfUrlElementId))
            .perform(DriverAtoms.webClick())
        fileListPage.assertPdfPreviewDisplayed()
    }

    private fun getToCourse(): MockCanvas {
        val data = MockCanvas.init(
                studentCount = 1,
                courseCount = 1,
                favoriteCourseCount = 1)

        val course1 = data.courses.values.first()
        val pagesTab = Tab(position = 2, label = "Pages", visibility = "public", tabId = Tab.PAGES_ID)
        val filesTab = Tab(position = 3, label = "Files", visibility = "public", tabId = Tab.FILES_ID)
        data.courseTabs[course1.id]!! += pagesTab
        data.courseTabs[course1.id]!! += filesTab

        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()

        dashboardPage.selectCourse(course1)
        return data
    }

    private fun goToAssignmentPdfSubmission(hasComment: Boolean = false, hasSentComment: Boolean = false, commentContents: String? = null, sentCommentContents: String? = null) {
        // Test clicking on the Submission and Rubric button to load the Submission Details Page
        val data = MockCanvas.init(
                studentCount = 1,
                teacherCount = 1,
                courseCount = 1
        )

        val course = data.courses.values.first()
        val student = data.students[0]
        val teacher = data.teachers[0]
        val token = data.tokenFor(student)!!
        data.addAssignmentsToGroups(course)
        val docSession = data.addAnnotation(
            signedInUser = student,
            annotationAuthor = teacher,
            hasComment = hasComment,
            hasSentComment = hasSentComment,
            commentContents = commentContents,
            sentCommentContents = sentCommentContents
        )
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

        data.addSubmissionForAssignment(
            assignmentId = assignment.id,
            userId = student.id,
            type = Assignment.SubmissionType.ONLINE_UPLOAD.apiString,
            attachment = attachment
        )

        setupCache(docSession.annotationUrls.pdfDownload)

        tokenLogin(data.domain, token, student)
        routeTo("courses/${course.id}/assignments", data.domain)
        assignmentListPage.waitForPage()

        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.goToSubmissionDetails()
    }

    private fun setupCache(pdfUrl: String) {
        val inputStream = InstrumentationRegistry.getInstrumentation().context.resources.assets.open(pdfFileName)
        FileCache.putInputStream("https://mock-data.instructure.com$pdfUrl", inputStream)
    }

    private fun cacheFile(userid: String, fileName: String) {
        // We need to copy the file from our assets directory to the cache dirctory, so OpenMediaAsyncTaskLoader
        // will find it and assume it was downloaded previously
        var inputStream : InputStream? = null
        var outputStream : OutputStream? = null

        try {
            inputStream = InstrumentationRegistry.getInstrumentation().context.resources.assets.open(pdfFileName)
            val dir = File(InstrumentationRegistry.getInstrumentation().getTargetContext().filesDir, "pdfs-$userid")
            dir.mkdirs()
            val file = File(dir, fileName)
            outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
        }
        finally {
            if(inputStream != null) inputStream.close()
            if(outputStream != null) {
                outputStream.flush()
                outputStream.close()
            }
        }
    }

}
