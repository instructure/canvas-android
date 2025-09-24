/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.interaction

import android.text.Html
import androidx.compose.ui.platform.ComposeView
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.web.webdriver.Locator
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addDiscussionTopicToCourse
import com.instructure.canvas.espresso.mockCanvas.addFileToCourse
import com.instructure.canvas.espresso.mockCanvas.addItemToModule
import com.instructure.canvas.espresso.mockCanvas.addLTITool
import com.instructure.canvas.espresso.mockCanvas.addModuleToCourse
import com.instructure.canvas.espresso.mockCanvas.addPageToCourse
import com.instructure.canvas.espresso.mockCanvas.addQuestionToQuiz
import com.instructure.canvas.espresso.mockCanvas.addQuizToCourse
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeCustomGradeStatusesManager
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.di.graphql.CustomGradeStatusModule
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.LockInfo
import com.instructure.canvasapi2.models.LockedModule
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.QuizAnswer
import com.instructure.canvasapi2.models.Tab
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.student.R
import com.instructure.student.ui.pages.WebViewTextCheck
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers
import org.junit.Test
import java.net.URLEncoder

@HiltAndroidTest
@UninstallModules(CustomGradeStatusModule::class)
class ModuleInteractionTest : StudentComposeTest() {

    @BindValue
    @JvmField
    val customGradeStatusesManager: CustomGradeStatusesManager = FakeCustomGradeStatusesManager()

    override fun displaysPageObjects() = Unit // Not used for interaction tests

    // A collection of things that we create during initialization and remember for use during
    // various tests.
    private var topicHeader: DiscussionTopicHeader? = null
    private var assignment: Assignment? = null
    private var page: Page? = null
    private val fileName = "ModuleFile.html"
    private var fileCheck: WebViewTextCheck? = null
    private var quiz: Quiz? = null

    // Tapping an Assignment module item should navigate to that item's detail page
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.MODULES, TestCategory.INTERACTION, SecondaryFeatureCategory.MODULES_ASSIGNMENTS)
    fun testModules_launchesIntoAssignment() {
        // Basic mock setup
        val data = getToCourseModules(studentCount = 1, courseCount = 1)
        val course1 = data.courses.values.first()
        val module = data.courseModules[course1.id]!!.first()

        // Create an assignment and add it as a module item
        assignment = data.addAssignment(
            courseId = course1.id,
            submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        )
        data.addItemToModule(
            course = course1,
            moduleId = module.id,
            item = assignment!!
        )

        // Verify that we can launch into the assignment from an assignment module item
        modulesPage.refresh()
        modulesPage.assertModuleDisplayed(module)
        modulesPage.assertModuleItemDisplayed(module, assignment!!.name!!)
        modulesPage.clickModuleItem(module, assignment!!.name!!)
        assignmentDetailsPage.assertAssignmentDetails(assignment!!)

    }

    // Tapping a Discussion module item should navigate to that item's detail page
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.MODULES, TestCategory.INTERACTION, SecondaryFeatureCategory.MODULES_DISCUSSIONS)
    fun testModules_launchesIntoDiscussion() {
        // Basic mock setup
        val data = getToCourseModules(studentCount = 1, courseCount = 1)
        val course1 = data.courses.values.first()
        val module = data.courseModules[course1.id]!!.first()
        val user = data.users.values.first()

        // Create a discussion and add it as a module item
        topicHeader = data.addDiscussionTopicToCourse(
            course = course1,
            user = user,
            topicTitle = "Discussion in module",
            topicDescription = "In. A. Module."
        )
        data.addItemToModule(
            course = course1,
            moduleId = module.id,
            item = topicHeader!!
        )

        // Verify that we can launch into a discussion from a discussion module item
        modulesPage.refresh()
        modulesPage.assertModuleDisplayed(module)
        modulesPage.assertModuleItemDisplayed(module, topicHeader!!.title!!)
        modulesPage.clickModuleItem(module, topicHeader!!.title!!)
        discussionDetailsPage.assertToolbarDiscussionTitle(topicHeader!!.title!!)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.MODULES, TestCategory.INTERACTION)
    fun testModules_launchesIntoExternalTool() {
        // Tapping an ExternalTool module item should navigate to that item's detail page
        val data = getToCourseModules(studentCount = 1, courseCount = 1)
        val course1 = data.courses.values.first()
        val module = data.courseModules[course1.id]!!.first()

        val ltiTool = data.addLTITool("Google Drive", "http://google.com", course1, 1234L)
        data.addItemToModule(
            course = course1,
            moduleId = module.id,
            item = ltiTool!!
        )

        modulesPage.refresh()
        modulesPage.clickModuleItem(module, "Google Drive")
        canvasWebViewPage.assertTitle("Google Drive")
    }

    // Tapping an ExternalURL module item should navigate to that item's detail page
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.MODULES, TestCategory.INTERACTION)
    fun testModules_launchesIntoExternalURL() {
        // Basic mock setup
        val externalUrl = "https://www.google.com"
        val data = getToCourseModules(studentCount = 1, courseCount = 1)
        val course1 = data.courses.values.first()
        val module = data.courseModules[course1.id]!!.first()

        // Create an external URL and add it as a module item
        data.addItemToModule(
            course = course1,
            moduleId = module.id,
            item = externalUrl
        )

        // click the external url module item
        modulesPage.refresh()
        modulesPage.clickModuleItem(module, externalUrl)
        // Not much we can test here, as it is an external URL, but testModules_navigateToNextAndPreviousModuleItems
        // will test that the module name and module item name are displayed correctly.
        canvasWebViewPage.checkWebViewURL("https://www.google.com")
    }

    // Tapping a File module item should navigate to that item's detail page
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.MODULES, TestCategory.INTERACTION, SecondaryFeatureCategory.MODULES_FILES)
    fun testModules_launchesIntoFile() {
        // Basic mock setup
        val data = getToCourseModules(studentCount = 1, courseCount = 1)
        val course1 = data.courses.values.first()
        val module = data.courseModules[course1.id]!!.first()

        // Create a file and add it as a module item
        val fileContent = "<h1 id=\"heading1\">A Heading</h1>"
        fileCheck = WebViewTextCheck(Locator.ID, "heading1", "A Heading")

        val fileId = data.addFileToCourse(
            courseId = course1.id,
            displayName = fileName,
            fileContent = fileContent,
            contentType = "text/html"
        )
        val rootFolderId = data.courseRootFolders[course1.id]!!.id
        val fileFolder = data.folderFiles[rootFolderId]?.find { it.id == fileId }
        data.addItemToModule(
            course = course1,
            moduleId = module.id,
            item = fileFolder!!
        )

        // Click the file module and verify that the file appears
        modulesPage.refresh()
        modulesPage.clickModuleItem(module, fileName, R.id.openButton)
        canvasWebViewPage.waitForWebView()
        canvasWebViewPage.runTextChecks(fileCheck!!)
    }

    // Tapping a Page module item should navigate to that item's detail page
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.MODULES, TestCategory.INTERACTION, SecondaryFeatureCategory.MODULES_PAGES)
    fun testModules_launchesIntoPage() {
        // Basic mock setup
        val data = getToCourseModules(studentCount = 1, courseCount = 1)
        val course1 = data.courses.values.first()
        val module = data.courseModules[course1.id]!!.first()

        // Create a page and add it as a module item
        page = data.addPageToCourse(
            courseId = course1.id,
            pageId = data.newItemId(),
            published = true,
            title = "Page In Course",
            url = URLEncoder.encode("Page In Course", "UTF-8")
        )
        data.addItemToModule(
            course = course1,
            moduleId = module.id,
            item = page!!
        )

        // Verify that we can launch into a page from a page module item
        modulesPage.refresh()
        modulesPage.assertModuleDisplayed(module)
        modulesPage.assertModuleItemDisplayed(module, page!!.title!!)
        modulesPage.clickModuleItem(module, page!!.title!!)

        // Check that the page showed up.
        // Strip out any html before comparing
        // Also, just use the first 10 chars because you risk encountering multiple-newlines
        // (which show as single newlines in webview, or even no-newlines if at the end
        // of the string) if you go much longer
        var expectedBody = Html.fromHtml(page!!.body!!).toString().substring(0, 10)
        canvasWebViewPage.runTextChecks(
            WebViewTextCheck(Locator.ID, "content", expectedBody)
        )

    }

    // Tapping a Quiz module item should navigate to that item's detail page
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.MODULES, TestCategory.INTERACTION, SecondaryFeatureCategory.EVENTS_QUIZZES)
    fun testModules_launchesIntoQuiz() {
        // Basic mock setup
        val data = getToCourseModules(studentCount = 1, courseCount = 1)
        val course1 = data.courses.values.first()
        val module = data.courseModules[course1.id]!!.first()

        // Create a quiz and add it as a module item
        quiz = data.addQuizToCourse(
            course = course1
        )

        data.addQuestionToQuiz(
            course = course1,
            quizId = quiz!!.id,
            questionName = "Math 1",
            questionText = "What is 2 + 5?",
            questionType = "multiple_choice_question",
            answers = arrayOf(
                QuizAnswer(answerText = "7"),
                QuizAnswer(answerText = "25"),
                QuizAnswer(answerText = "-7")
            )
        )

        data.addQuestionToQuiz(
            course = course1,
            quizId = quiz!!.id,
            questionName = "Math 2",
            questionText = "Pi is greater than the square root of 2",
            questionType = "true_false_question"
        )

        data.addQuestionToQuiz(
            course = course1,
            quizId = quiz!!.id,
            questionName = "Math 3",
            questionText = "Write an essay on why math is so awesome",
            questionType = "essay_question"
        )

        data.addItemToModule(
            course = course1,
            moduleId = module.id,
            item = quiz!!
        )

        // Verify that we can launch into a quiz from a quiz module item
        modulesPage.refresh()
        modulesPage.assertModuleDisplayed(module)
        modulesPage.assertModuleItemDisplayed(module, quiz!!.title!!)
        /* TODO: Check that the quiz is displayed if/when we can do so via WebView
        modulesPage.clickModuleItem(module, quiz!!.title!!, R.id.goToQuiz/*, R.id.next*/)
        val quizQuestions = data.quizQuestions[quiz!!.id]?.toList()
        quizDetailsPage.assertQuizDisplayed(quiz!!, false, quizQuestions!!)
        Espresso.pressBack()
         */
        moduleProgressionPage.assertModuleItemTitleDisplayed(quiz!!.title!!)
    }

    // Tapping a module should collapse and hide all of that module's items in the module list
    // Tapping a collapsed module should expand it
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.MODULES, TestCategory.INTERACTION)
    fun testModules_modulesExpandAndCollapse() {
        // Basic mock setup
        val data = getToCourseModules(studentCount = 1, courseCount = 1)
        val course1 = data.courses.values.first()
        var module = data.courseModules[course1.id]!!.first()

        // Create an assignment and add it as a module item
        assignment = data.addAssignment(
            courseId = course1.id,
            submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        )
        data.addItemToModule(
            course = course1,
            moduleId = module.id,
            item = assignment!!
        )

        module = data.courseModules[course1.id]!!.first()
        val firstModuleItem = module.items[0]

        // Verify that expanding a module shows the module items and collapsing a module
        // hides/nukes that module's items.
        // We're going on the assumption that the lone module is initially expanded.  Although
        // the initial assertModuleItemDisplayed() would expand the module if it was not expanded
        // already.
        modulesPage.refresh()
        modulesPage.assertModuleDisplayed(module)
        modulesPage.assertModuleItemDisplayed(module, firstModuleItem.title!!)
        modulesPage.clickModule(module)
        modulesPage.assertModuleItemNotDisplayed(firstModuleItem.title!!)
        modulesPage.clickModule(module)
        modulesPage.assertModuleItemDisplayed(module, firstModuleItem.title!!)

    }

    // After entering the detail page for a module item, pressing the back button or back arrow should navigate back
    // to the module list. This should also work if the detail page is accessed via deep link
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.MODULES, TestCategory.INTERACTION)
    fun testModules_navigateBackToModuleListFromModuleItem() {
        // Basic mock setup
        val data = getToCourseModules(studentCount = 1, courseCount = 1)
        val course1 = data.courses.values.first()
        val module = data.courseModules[course1.id]!!.first()

        // For each module item, go into the module detail page, click the back button,
        // and verify that we've returned to the module list page.
        for (moduleItem in module.items) {
            modulesPage.clickModuleItem(module, moduleItem.title!!)
            Espresso.pressBack()
            modulesPage.assertModuleItemDisplayed(module, moduleItem.title!!)
        }

    }

    // When viewing the detail page for an item in a module with multiple items, the detail page should have
    // 'next' and 'previous' navigation buttons. Clicking these should navigate to the next/previous module items.
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.MODULES, TestCategory.INTERACTION)
    fun testModules_navigateToNextAndPreviousModuleItems() {
        // Basic mock setup
        val data = getToCourseModules(studentCount = 1, courseCount = 1)
        val externalUrl = "https://www.google.com"
        val course1 = data.courses.values.first()
        var module = data.courseModules[course1.id]!!.first()
        val user = data.users.values.first()

        // Create an assignment and add it as a module item
        assignment = data.addAssignment(
            courseId = course1.id,
            submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        )
        data.addItemToModule(
            course = course1,
            moduleId = module.id,
            item = assignment!!
        )

        // Create a discussion and add it as a module item
        topicHeader = data.addDiscussionTopicToCourse(
            course = course1,
            user = user,
            topicTitle = "Discussion in module",
            topicDescription = "In. A. Module."
        )
        data.addItemToModule(
            course = course1,
            moduleId = module.id,
            item = topicHeader!!
        )

        val ltiTool = data.addLTITool("Google Drive", "http://google.com", course1, 1234L)
        data.addItemToModule(
            course = course1,
            moduleId = module.id,
            item = ltiTool!!
        )

        // Create a page and add it as a module item
        page = data.addPageToCourse(
            courseId = course1.id,
            pageId = data.newItemId(),
            published = true,
            title = "Page In Course",
            url = URLEncoder.encode("Page In Course", "UTF-8")
        )
        data.addItemToModule(
            course = course1,
            moduleId = module.id,
            item = page!!
        )

        // Create an external URL and add it as a module item
        data.addItemToModule(
            course = course1,
            moduleId = module.id,
            item = externalUrl
        )

        module = data.courseModules[course1.id]!!.first()

        // Iterate through the module items, starting at the first
        val moduleItemList = module.items
        modulesPage.refresh()
        modulesPage.clickModuleItem(module, moduleItemList[0].title!!)

        var moduleIndex = 0; // we start here
        while (moduleIndex < moduleItemList.count()) {
            val moduleItem = moduleItemList[moduleIndex]

            // Make sure that the previous button is appropriately displayed/gone
            if (moduleIndex == 0) {
                moduleProgressionPage.assertPreviousButtonInvisible()
            } else {
                moduleProgressionPage.assertPreviousButtonDisplayed()
            }

            // Make sure that the next button is appropriately displayed/gone
            if (moduleIndex == moduleItemList.count() - 1) {
                moduleProgressionPage.assertNextButtonInvisible()
            } else {
                moduleProgressionPage.assertNextButtonDisplayed()
            }

            // Make sure that the module title is displayed
            moduleProgressionPage.assertModuleTitle(module.name!!)

            // Make sure that the module item title is displayed
            moduleProgressionPage.assertModuleItemTitleDisplayed(moduleItem.title!!)

            // Let's navigate to our next page
            moduleIndex += 1
            if (moduleIndex < moduleItemList.count()) {
                moduleProgressionPage.clickNextButton()
            }
        }

        if (moduleItemList.count() > 1) {
            // Let's make sure that the "previous" button works as well.
            moduleProgressionPage.clickPreviousButton()
            val moduleItem = moduleItemList[moduleItemList.count() - 2]
            moduleProgressionPage.assertModuleItemTitleDisplayed(moduleItem.title!!)
        }
    }

    // Module can't be accessed unless all prerequisites have been fulfilled
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.MODULES, TestCategory.INTERACTION)
    fun testModules_moduleLockedWithUnfulfilledPrerequisite() {
        // Basic mock setup
        val data = getToCourseModules(studentCount = 1, courseCount = 1)
        val course1 = data.courses.values.first()
        val module = data.courseModules[course1.id]!!.first()

        // Let's add a second module that has the first one as a prerequisite
        val module2 = data.addModuleToCourse(
            course = course1,
            moduleName = "Prereq Module",
            prerequisiteIds = longArrayOf(module.id),
            state = ModuleObject.State.Locked.toString()
        )

        // And let's add an assignment to the new module
        var unavailableAssignment = data.addAssignment(
            courseId = course1.id,
            submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY),
            // Man, this is a bit hokey, but it's what I had to do to get the assignment to show
            // up as unavailable in the assignment details page
            lockInfo = LockInfo(
                modulePrerequisiteNames = arrayListOf(module.name!!),
                contextModule = LockedModule(name = module.name!!)
            )
        )
        data.addItemToModule(
            course = course1,
            moduleId = module2.id,
            item = unavailableAssignment
        )

        // Refresh to get module list update, select module2, and assert that unavailableAssignment is locked
        modulesPage.refresh()
        modulesPage.clickModule(module)
        modulesPage.clickModuleItem(module2, unavailableAssignment.name!!)
        assignmentDetailsPage.assertAssignmentLocked()
    }

    // Module can't be accessed until the availability date has passed
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.MODULES, TestCategory.INTERACTION)
    fun testModules_moduleLockedUntilAvailabilityDate() {
        // Basic mock setup
        val data = getToCourseModules(studentCount = 1, courseCount = 1)
        val course1 = data.courses.values.first()
        val module = data.courseModules[course1.id]!!.first()

        // Let's add a second module with a lockUntil setting
        val module2 = data.addModuleToCourse(
            course = course1,
            moduleName = "Locked Module",
            unlockAt = 2.days.fromNow.iso8601
        )

        // And let's create an assignment and add it to the "locked" module.
        val lockedAssignment = data.addAssignment(
            courseId = course1.id,
            submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        )
        data.addItemToModule(
            course = course1,
            moduleId = module2.id,
            item = lockedAssignment
        )

        // Refresh to get module list update, then assert that module2 is locked
        modulesPage.refresh()
        modulesPage.clickModule(module)
        // No need to click on the module since they are expanded by default now
        modulesPage.assertAssignmentLocked(lockedAssignment, course1)
    }

    // Show possible points for assignments in modules if not restricted
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.MODULES, TestCategory.INTERACTION)
    fun testModules_showPossiblePointsIfNotRestricted() {
        val data = getToCourseModules(studentCount = 1, courseCount = 1)
        val course = data.courses.values.first()
        val module = data.courseModules[course.id]!!.first()

        data.courseSettings[course.id] = CourseSettings(restrictQuantitativeData = false)

        val assignment = data.addAssignment(
            courseId = course.id,
            submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY),
            pointsPossible = 10
        )

        data.addItemToModule(
            course = course,
            moduleId = module.id,
            item = assignment,
            moduleContentDetails = ModuleContentDetails(pointsPossible = assignment.pointsPossible.toString())
        )

        modulesPage.refresh()
        modulesPage.assertPossiblePointsDisplayed(assignment.pointsPossible.toInt().toString())
    }

    // Hide possible points for assignments in modules if restricted
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.MODULES, TestCategory.INTERACTION)
    fun testModules_hidePossiblePointsIfRestricted() {
        val data = getToCourseModules(studentCount = 1, courseCount = 1)
        val course = data.courses.values.first()
        val module = data.courseModules[course.id]!!.first()

        data.courseSettings[course.id] = CourseSettings(restrictQuantitativeData = true)

        val assignment = data.addAssignment(
            courseId = course.id,
            submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY),
            pointsPossible = 10
        )

        data.addItemToModule(
            course = course,
            moduleId = module.id,
            item = assignment,
            moduleContentDetails = ModuleContentDetails(pointsPossible = assignment.pointsPossible.toString())
        )

        modulesPage.refresh()
        modulesPage.assertPossiblePointsNotDisplayed(assignment.name.orEmpty())
    }

    // Mock a specified number of students and courses, add some assorted assignments, discussions, etc...
    // in the form of module items, and navigate to the modules page of the course
    private fun getToCourseModules(
        studentCount: Int = 1,
        courseCount: Int = 1
    ): MockCanvas {

        // Basic info
        val data = MockCanvas.init(
            studentCount = studentCount,
            courseCount = courseCount,
            favoriteCourseCount = courseCount
        )

        // Add a course tab
        val course1 = data.courses.values.first()
        val modulesTab = Tab(position = 2, label = "Modules", visibility = "public", tabId = Tab.MODULES_ID)
        data.courseTabs[course1.id]!! += modulesTab

        // Create a module
        data.addModuleToCourse(
            course = course1,
            moduleName = "Big Module"
        )

        // Sign in
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()

        // Navigate to the (first) course
        dashboardPage.selectCourse(course1)
        // Navigate to the modules page for the course
        courseBrowserPage.selectModules()

        return data
    }

    override fun enableAndConfigureAccessibilityChecks() {
        extraAccessibilitySupressions = Matchers.allOf(
            AccessibilityCheckResultUtils.matchesCheck(
                SpeakableTextPresentCheck::class.java
            ),
            AccessibilityCheckResultUtils.matchesViews(
                ViewMatchers.withParent(
                    ViewMatchers.withClassName(
                        Matchers.equalTo(ComposeView::class.java.name)
                    )
                )
            )
        )

        super.enableAndConfigureAccessibilityChecks()
    }

}
