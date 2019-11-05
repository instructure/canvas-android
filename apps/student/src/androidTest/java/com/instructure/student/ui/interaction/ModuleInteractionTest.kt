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

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.mockCanvas.AssignmentGroupType
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addDiscussionTopicToCourse
import com.instructure.canvas.espresso.mockCanvas.addFileToCourse
import com.instructure.canvas.espresso.mockCanvas.addItemToModule
import com.instructure.canvas.espresso.mockCanvas.addModuleToCourse
import com.instructure.canvas.espresso.mockCanvas.addPageToCourse
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.LockInfo
import com.instructure.canvasapi2.models.LockedModule
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.Tab
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.R
import com.instructure.student.ui.pages.WebViewTextCheck
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.core.AllOf.allOf
import org.junit.Test

class ModuleInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    // A collection of things that we create during initialization and remember for use during
    // various tests.
    private var topicHeader: DiscussionTopicHeader? = null
    private var assignment: Assignment? = null
    private var page: Page? = null
    private val fileName = "ModuleFile.html"
    private var fileCheck: WebViewTextCheck? = null
    private val externalUrl = "https://www.google.com"

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.MODULES, TestCategory.INTERACTION, true, FeatureCategory.ASSIGNMENTS)
    fun testModules_launchesIntoAssignment() {
        // Tapping an Assignment module item should navigate to that item's detail page

        // Basic mock setup
        val data = getToCourse(studentCount = 1, courseCount = 1)
        val course1 = data.courses.values.first()
        val module = data.courseModules[course1.id]!!.first()

        // Go to the modules page, and run the test
        courseBrowserPage.selectModules()
        modulesPage.assertModuleDisplayed(module)
        modulesPage.assertModuleItemDisplayed(module, assignment!!.name!!)
        modulesPage.clickModuleItem(module, assignment!!.name!!)
        assignmentDetailsPage.verifyAssignmentDetails(assignment!!)

    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.MODULES, TestCategory.INTERACTION, true, FeatureCategory.DISCUSSIONS)
    fun testModules_launchesIntoDiscussion() {
        // Tapping a Discussion module item should navigate to that item's detail page

        // Basic mock setup
        val data = getToCourse(studentCount = 1, courseCount = 1)
        val course1 = data.courses.values.first()
        val module = data.courseModules[course1.id]!!.first()

        // Go to the modules page, and run the test
        courseBrowserPage.selectModules()
        modulesPage.assertModuleDisplayed(module)
        modulesPage.assertModuleItemDisplayed(module, topicHeader!!.title!!)
        modulesPage.clickModuleItem(module, topicHeader!!.title!!)
        discussionDetailsPage.assertTopicInfoShowing(topicHeader!!)
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.MODULES, TestCategory.INTERACTION, true)
    fun testModules_launchesIntoExternalTool() {
        // Tapping an ExternalTool module item should navigate to that item's detail page
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.MODULES, TestCategory.INTERACTION, true)
    fun testModules_launchesIntoExternalURL() {
        // Tapping an ExternalURL module item should navigate to that item's detail page

        // Basic mock setup
        val data = getToCourse(studentCount = 1, courseCount = 1)
        val course1 = data.courses.values.first()
        val module = data.courseModules[course1.id]!!.first()

        // Go to the modules page, click the external module
        courseBrowserPage.selectModules()
        modulesPage.clickModuleItem(module,externalUrl)
        // Not much we can test here, as it is an external URL, but testModules_navigateToNextAndPreviousModuleItems
        // will test that the module name and module item name are displayed correctly.
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.MODULES, TestCategory.INTERACTION, true, FeatureCategory.FILES)
    fun testModules_launchesIntoFile() {
        // Tapping a File module item should navigate to that item's detail page

        // Basic mock setup
        val data = getToCourse(studentCount = 1, courseCount = 1)
        val course1 = data.courses.values.first()
        val module = data.courseModules[course1.id]!!.first()

        // Go to the modules page, click the file module, and verify that the file appears
        courseBrowserPage.selectModules()
        modulesPage.clickModuleItem(module,fileName,true)
        // TODO: Move this check to a more centralized place, like webViewPage?
        onWebView(allOf(withId(R.id.canvasWebView), isDisplayed())) // There could be multiple other webviews in the stack, not being shown.
                .withElement(findElement(fileCheck!!.locatorType, fileCheck!!.locatorValue))
                .check(webMatches(getText(), containsString(fileCheck!!.textValue)))

    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.MODULES, TestCategory.INTERACTION, true, FeatureCategory.PAGES)
    fun testModules_launchesIntoPage() {
        // Tapping a Page module item should navigate to that item's detail page

        // Basic mock setup
        val data = getToCourse(studentCount = 1, courseCount = 1)
        val course1 = data.courses.values.first()
        val module = data.courseModules[course1.id]!!.first()

        // Go to the modules page, and run the test
        courseBrowserPage.selectModules()
        modulesPage.assertModuleDisplayed(module)
        modulesPage.assertModuleItemDisplayed(module, page!!.title!!)
        modulesPage.clickModuleItem(module, page!!.title!!)
        //discussionDetailsPage.assertTopicInfoShowing(topicHeader!!)

        Log.d("launchesIntoPage", "Here") // TODO: Add a content check for displayed page

    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.MODULES, TestCategory.INTERACTION, true, FeatureCategory.QUIZZES)
    fun testModules_launchesIntoQuiz() {
        // Tapping a Quiz module item should navigate to that item's detail page
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.MODULES, TestCategory.INTERACTION, true)
    fun testModules_modulesExpandAndCollapse() {
        // Tapping a module should collapse and hide all of that module's items in the module list
        // Tapping a collapsed module should expand it

        // Basic mock setup
        val data = getToCourse(studentCount = 1, courseCount = 1)
        val course1 = data.courses.values.first()
        val module = data.courseModules[course1.id]!!.first()
        val firstModuleItem = module.items[0]

        // Go to the modules page, and run the test
        // We're going on the assumption that the lone module is initially expanded
        courseBrowserPage.selectModules()
        modulesPage.assertModuleDisplayed(module)
        modulesPage.assertModuleItemDisplayed(module,firstModuleItem.title!!)
        modulesPage.clickModule(module)
        modulesPage.assertModuleItemNotDisplayed(firstModuleItem.title!!)
        modulesPage.clickModule(module)
        modulesPage.assertModuleItemDisplayed(module,firstModuleItem.title!!)

    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.MODULES, TestCategory.INTERACTION, true)
    fun testModules_navigateBackToModuleListFromModuleItem() {
        // After entering the detail page for a module item, pressing the back button or back arrow should navigate back
        // to the module list. This should also work if the detail page is accessed via deep link

        // Basic mock setup
        val data = getToCourse(studentCount = 1, courseCount = 1)
        val course1 = data.courses.values.first()
        val module = data.courseModules[course1.id]!!.first()

        courseBrowserPage.selectModules()

        // For each module item, go into the module detail page, click the back button,
        // and verify that we've returned to the module list page.
        for(moduleItem in module.items) {
            modulesPage.clickModuleItem(module,moduleItem.title!!)
            Espresso.pressBack()
            modulesPage.assertModuleItemDisplayed(module,moduleItem.title!!)
        }

    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.MODULES, TestCategory.INTERACTION, true)
    fun testModules_navigateToNextAndPreviousModuleItems() {
        // When viewing the detail page for an item in a module with multiple items, the detail page should have
        // 'next' and 'previous' navigation buttons. Clicking these should navigate to the next/previous module items.

        // Basic mock setup
        val data = getToCourse(studentCount = 1, courseCount = 1)
        val course1 = data.courses.values.first()
        val module = data.courseModules[course1.id]!!.first()

        // Go to the modules page, and iterate through the module items
        courseBrowserPage.selectModules()
        val moduleItemList = module.items
        modulesPage.clickModuleItem(module,moduleItemList[0].title!!)

        var moduleIndex = 0; // we start here
        while(moduleIndex < moduleItemList.count()) {
            val moduleItem = moduleItemList[moduleIndex]

            // Make sure that the previous button is appropriately displayed/gone
            if(moduleIndex == 0) {
                moduleProgressionPage.assertPreviousButtonInvisible()
            }
            else {
                moduleProgressionPage.assertPreviousButtonDisplayed()
            }

            // Make sure that the next button is appropriately displayed/gone
            if(moduleIndex == moduleItemList.count() - 1) {
                moduleProgressionPage.assertNextButtonInvisible()
            }
            else {
                moduleProgressionPage.assertNextButtonDisplayed()
            }

            // Make sure that the module title is displayed
            moduleProgressionPage.assertModuleTitle(module.name!!)

            // Make sure that the module item title is displayed
            moduleProgressionPage.assertModuleItemTitleDisplayed(moduleItem.title!!)

            // Let's navigate to our next page
            moduleIndex += 1
            if(moduleIndex < moduleItemList.count()) {
                moduleProgressionPage.clickNextButton()
            }
        }

        if(moduleItemList.count() > 1) {
            // Let's make sure that the "previous" button works as well.
            moduleProgressionPage.clickPreviousButton()
            val moduleItem = moduleItemList[moduleItemList.count() - 2]
            moduleProgressionPage.assertModuleItemTitleDisplayed(moduleItem.title!!)
        }
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.MODULES, TestCategory.INTERACTION, true)
    fun testModules_moduleLockedWithUnfulfilledPrerequisite() {
        // Module can't be accessed unless all prerequisites have been fulfilled

        // Basic mock setup
        val data = getToCourse(studentCount = 1, courseCount = 1)
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
                groupType = AssignmentGroupType.UPCOMING,
                submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
                // Man, this is a bit hokey, but it's what I had to do to get the assignment to show
                // up as unavailable in the assignment details page
                lockInfo = LockInfo(
                        modulePrerequisiteNames = arrayListOf(module.name!!),
                        contextModule = LockedModule(name = module.name!!) )
        )
        data.addItemToModule(
                course = course1,
                moduleId = module2.id,
                item = unavailableAssignment
        )


        // Navigate to the modules page, select module2, and assert that unavailableAssignment is locked
        courseBrowserPage.selectModules()
        modulesPage.clickModule(module2)
        modulesPage.clickModuleItem(module2,unavailableAssignment.name!!)
        assignmentDetailsPage.verifyAssignmentLocked()
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.MODULES, TestCategory.INTERACTION, true)
    fun testModules_moduleLockedUntilAvailabilityDate() {
        // Module can't be accessed until the availability date has passed

        // Basic mock setup
        val data = getToCourse(studentCount = 1, courseCount = 1)
        val course1 = data.courses.values.first()
        val module = data.courseModules[course1.id]!!.first()

        // Let's add a second module with a lockUntil setting
        val module2 = data.addModuleToCourse(
                course = course1,
                moduleName = "Locked Module",
                unlockAt = 2.days.fromNow.iso8601
        )

        // Navigate to the modules page and assert that module2 is locked
        courseBrowserPage.selectModules()
        modulesPage.clickModule(module2)
        modulesPage.assertAnyModuleLocked()
    }

    // Mock a specified number of students and courses, add some assorted assignments, discussions, etc...
    // in the form of modules, and navigate to the course
    private fun getToCourse(
            studentCount: Int = 1,
            courseCount: Int = 1): MockCanvas {

        // Basic info
        val data = MockCanvas.init(
                studentCount = studentCount,
                courseCount = courseCount,
                favoriteCourseCount = courseCount)

        // Add a course tab
        val course1 = data.courses.values.first()
        val user1 = data.users.values.first()
        val modulesTab = Tab(position = 2, label = "Modules", visibility = "public", tabId = Tab.MODULES_ID)
        data.courseTabs[course1.id]!! += modulesTab

        // Create a module
        val module = data.addModuleToCourse(
                course = course1,
                moduleName = "Big Module"
        )

        // Create a discussion and add it as a module item
        topicHeader = data.addDiscussionTopicToCourse(
                course = course1,
                user = user1,
                topicTitle = "Discussion in module",
                topicDescription = "In. A. Module."
        )
        data.addItemToModule(
                course = course1,
                moduleId = module.id,
                item = topicHeader!!
        )

        // Create an assignment and add it as a module item
        assignment = data.addAssignment(
                courseId = course1.id,
                groupType = AssignmentGroupType.UPCOMING,
                submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY
        )
        data.addItemToModule(
                course = course1,
                moduleId = module.id,
                item = assignment!!
        )

        // Create a page and add it as a module item
        page = data.addPageToCourse(
                courseId = course1.id,
                pageId = data.newItemId(),
                published = true
        )
        data.addItemToModule(
                course = course1,
                moduleId = module.id,
                item = page!!
        )

        // Create a file and add it as a module item
        val fileContent = "<h1 id=\"heading1\">A Heading</h1>"
        fileCheck = WebViewTextCheck(Locator.ID,"heading1","A Heading")

        val fileId = data.addFileToCourse(
                courseId = course1.id,
                displayName = fileName,
                fileContent = fileContent,
                contentType = "text/html"
        )
        val rootFolderId = data.courseRootFolders[course1.id]!!.id
        val fileFolder = data.folderFiles[rootFolderId]?.find {it.id == fileId}
        data.addItemToModule(
                course = course1,
                moduleId = module.id,
                item = fileFolder!!
        )

        // Create an external URL and add it as a module item
        data.addItemToModule(
                course = course1,
                moduleId = module.id,
                item = externalUrl
        )


        // Sign in
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()

        // Navigate to the (first) course
        dashboardPage.selectCourse(course1)

        return data
    }

}
