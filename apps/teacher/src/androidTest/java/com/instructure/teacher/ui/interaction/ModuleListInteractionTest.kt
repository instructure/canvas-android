/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 *
 *
 */

package com.instructure.teacher.ui.interaction

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addFileToCourse
import com.instructure.canvas.espresso.mockCanvas.addItemToModule
import com.instructure.canvas.espresso.mockCanvas.addModuleToCourse
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.Tab
import com.instructure.dataseeding.util.Randomizer
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.extensions.openOverflowMenu
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class ModuleListInteractionTest : TeacherComposeTest() {

    @Test
    override fun displaysPageObjects() {
        goToModulesPage()
        moduleListPage.assertPageObjects()
    }

    @Test
    fun assertDisplaysMenuItems() {
        goToModulesPage()
        openOverflowMenu()
        moduleListPage.assertToolbarMenuItems()
    }

    @Test
    fun assertDisplaysModuleMenuItems() {
        val data = goToModulesPage()
        val module = data.courseModules.values.first().first()

        moduleListPage.clickItemOverflow(module.name.orEmpty())
        moduleListPage.assertModuleMenuItems()
    }

    @Test
    fun assertPublishedItemActions() {
        val data = goToModulesPage()
        val module = data.courseModules.values.first().first()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(
            courseId = course.id,
            submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        )

        data.addItemToModule(data.courses.values.first(), module.id, assignment, published = true)

        moduleListPage.refresh()

        moduleListPage.clickItemOverflow(assignment.name.orEmpty())
        moduleListPage.assertOverflowItem(R.string.unpublish)
    }

    @Test
    fun assertUnpublishedItemActions() {
        val data = goToModulesPage()
        val module = data.courseModules.values.first().first()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(
            courseId = course.id,
            submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        )

        data.addItemToModule(data.courses.values.first(), module.id, assignment, published = false)

        moduleListPage.refresh()

        moduleListPage.clickItemOverflow(assignment.name.orEmpty())
        moduleListPage.assertOverflowItem(R.string.publish)
    }

    @Test
    fun assertFileEditOpens() {
        val data = goToModulesPage()
        val module = data.courseModules.values.first().first()
        val course = data.courses.values.first()
        val fileId = data.addFileToCourse(course.id)
        val rootFolderId = data.courseRootFolders[course.id]!!.id
        val fileFolder = data.folderFiles[rootFolderId]?.find { it.id == fileId }
        data.addItemToModule(
            course = course,
            moduleId = module.id,
            item = fileFolder!!
        )

        moduleListPage.refresh()

        moduleListPage.clickItemOverflow(fileFolder.displayName.orEmpty())

        moduleListPage.assertFileEditDialogVisible()
    }

    @Test
    fun publishModuleItem() {
        val data = goToModulesPage()
        val module = data.courseModules.values.first().first()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(
            courseId = course.id,
            submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        )

        data.addItemToModule(data.courses.values.first(), module.id, assignment, published = false)

        moduleListPage.refresh()

        moduleListPage.clickItemOverflow(assignment.name.orEmpty())
        moduleListPage.clickOnText(R.string.publishModuleItemAction)
        moduleListPage.clickOnText(R.string.publishDialogPositiveButton)

        moduleListPage.assertSnackbarText(R.string.moduleItemPublished)
        moduleListPage.assertModuleItemIsPublished(assignment.name.orEmpty())
    }

    @Test
    fun unpublishModuleItem() {
        val data = goToModulesPage()
        val module = data.courseModules.values.first().first()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(
            courseId = course.id,
            submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        )

        data.addItemToModule(data.courses.values.first(), module.id, assignment, published = true)

        moduleListPage.refresh()

        moduleListPage.clickItemOverflow(assignment.name.orEmpty())
        moduleListPage.clickOnText(R.string.unpublishModuleItemAction)
        moduleListPage.clickOnText(R.string.unpublishDialogPositiveButton)

        moduleListPage.assertSnackbarText(R.string.moduleItemUnpublished)
        moduleListPage.assertModuleItemNotPublished(assignment.name.orEmpty())
    }

    @Test
    fun publishModuleOnly() {
        val data = goToModulesPage(publishedModuleCount = 0, unpublishedModuleCount = 1)
        val unpublishedModule = data.courseModules.values.first().first { it.published == false }
        val assignment = data.addAssignment(courseId = data.courses.values.first().id)

        data.addItemToModule(data.courses.values.first(), unpublishedModule.id, assignment, published = false)
        moduleListPage.refresh()

        moduleListPage.clickItemOverflow(unpublishedModule.name.orEmpty())
        moduleListPage.clickOnText(R.string.publishModuleOnly)
        moduleListPage.clickOnText(R.string.publishDialogPositiveButton)

        moduleListPage.assertSnackbarText(R.string.onlyModulePublished)
        moduleListPage.assertModuleIsPublished(unpublishedModule.name.orEmpty())
        moduleListPage.assertModuleItemNotPublished(assignment.name.orEmpty())
    }

    @Test
    fun publishModuleAndItems() {
        val data = goToModulesPage(publishedModuleCount = 0, unpublishedModuleCount = 1)
        val unpublishedModule = data.courseModules.values.first().first { it.published == false }
        val assignment = data.addAssignment(courseId = data.courses.values.first().id)

        data.addItemToModule(data.courses.values.first(), unpublishedModule.id, assignment, published = false)
        moduleListPage.refresh()

        moduleListPage.clickItemOverflow(unpublishedModule.name.orEmpty())
        moduleListPage.clickOnText(R.string.publishModuleAndItems)
        moduleListPage.clickOnText(R.string.publishDialogPositiveButton)

        progressPage.clickDone()

        moduleListPage.assertSnackbarText(R.string.moduleAndAllItemsPublished)
        moduleListPage.assertModuleIsPublished(unpublishedModule.name.orEmpty())
        moduleListPage.assertModuleItemIsPublished(assignment.name.orEmpty())
    }

    @Test
    fun unpublishModuleAndItems() {
        val data = goToModulesPage(publishedModuleCount = 1, unpublishedModuleCount = 0)
        val publishedModule = data.courseModules.values.first().first { it.published == true }
        val assignment = data.addAssignment(courseId = data.courses.values.first().id)

        data.addItemToModule(data.courses.values.first(), publishedModule.id, assignment, published = true)
        moduleListPage.refresh()

        moduleListPage.clickItemOverflow(publishedModule.name.orEmpty())
        moduleListPage.clickOnText(R.string.unpublishModuleAndItems)
        moduleListPage.clickOnText(R.string.unpublishDialogPositiveButton)

        progressPage.clickDone()

        moduleListPage.assertSnackbarText(R.string.moduleAndAllItemsUnpublished)
        moduleListPage.assertModuleNotPublished(publishedModule.name.orEmpty())
        moduleListPage.assertModuleItemNotPublished(assignment.name.orEmpty())
    }

    @Test
    fun publishModulesOnly() {
        val data = goToModulesPage(publishedModuleCount = 0, unpublishedModuleCount = 2)
        val unpublishedModules = data.courseModules.values.first().filter { it.published == false }
        val assignment1 = data.addAssignment(courseId = data.courses.values.first().id)
        val assignment2 = data.addAssignment(courseId = data.courses.values.first().id)

        data.addItemToModule(data.courses.values.first(), unpublishedModules[0].id, assignment1, published = false)
        data.addItemToModule(data.courses.values.first(), unpublishedModules[1].id, assignment2, published = false)

        moduleListPage.refresh()

        openOverflowMenu()
        moduleListPage.clickOnText(R.string.publishModulesOnly)
        moduleListPage.clickOnText(R.string.publishDialogPositiveButton)

        progressPage.clickDone()

        moduleListPage.assertSnackbarText(R.string.onlyModulesPublished)
        moduleListPage.assertModuleIsPublished(unpublishedModules[0].name.orEmpty())
        moduleListPage.assertModuleIsPublished(unpublishedModules[1].name.orEmpty())
        moduleListPage.assertModuleItemNotPublished(assignment1.name.orEmpty())
        moduleListPage.assertModuleItemNotPublished(assignment2.name.orEmpty())
    }

    @Test
    fun publishModulesAndItems() {
        val data = goToModulesPage(publishedModuleCount = 0, unpublishedModuleCount = 2)
        val unpublishedModules = data.courseModules.values.first().filter { it.published == false }
        val assignment1 = data.addAssignment(courseId = data.courses.values.first().id)
        val assignment2 = data.addAssignment(courseId = data.courses.values.first().id)

        data.addItemToModule(data.courses.values.first(), unpublishedModules[0].id, assignment1, published = false)
        data.addItemToModule(data.courses.values.first(), unpublishedModules[1].id, assignment2, published = false)

        moduleListPage.refresh()

        openOverflowMenu()
        moduleListPage.clickOnText(R.string.publishAllModulesAndItems)
        moduleListPage.clickOnText(R.string.publishDialogPositiveButton)

        progressPage.clickDone()

        moduleListPage.assertSnackbarText(R.string.allModulesAndAllItemsPublished)
        moduleListPage.assertModuleIsPublished(unpublishedModules[0].name.orEmpty())
        moduleListPage.assertModuleIsPublished(unpublishedModules[1].name.orEmpty())
        moduleListPage.assertModuleItemIsPublished(assignment1.name.orEmpty())
        moduleListPage.assertModuleItemIsPublished(assignment2.name.orEmpty())
    }

    @Test
    fun unpublishModulesAndItems() {
        val data = goToModulesPage(publishedModuleCount = 2, unpublishedModuleCount = 0)
        val unpublishedModules = data.courseModules.values.first().filter { it.published == true }
        val assignment1 = data.addAssignment(courseId = data.courses.values.first().id)
        val assignment2 = data.addAssignment(courseId = data.courses.values.first().id)

        data.addItemToModule(data.courses.values.first(), unpublishedModules[0].id, assignment1, published = true)
        data.addItemToModule(data.courses.values.first(), unpublishedModules[1].id, assignment2, published = true)

        moduleListPage.refresh()

        openOverflowMenu()
        moduleListPage.clickOnText(R.string.unpublishAllModulesAndItems)
        moduleListPage.clickOnText(R.string.unpublishDialogPositiveButton)

        progressPage.clickDone()

        moduleListPage.assertSnackbarText(R.string.allModulesAndAllItemsUnpublished)
        moduleListPage.assertModuleNotPublished(unpublishedModules[0].name.orEmpty())
        moduleListPage.assertModuleNotPublished(unpublishedModules[1].name.orEmpty())
        moduleListPage.assertModuleItemNotPublished(assignment1.name.orEmpty())
        moduleListPage.assertModuleItemNotPublished(assignment2.name.orEmpty())
    }

    @Test
    fun unpublishFileModuleItem() {
        val data = goToModulesPage()
        val module = data.courseModules.values.first().first()
        val course = data.courses.values.first()
        val fileId = data.addFileToCourse(course.id)
        val rootFolderId = data.courseRootFolders[course.id]!!.id
        val fileFolder = data.folderFiles[rootFolderId]?.find { it.id == fileId }
        data.addItemToModule(
            course = course,
            moduleId = module.id,
            item = fileFolder!!,
            contentId = fileId,
            published = true,
            moduleContentDetails = ModuleContentDetails(
                hidden = false,
                locked = false
            )
        )

        moduleListPage.refresh()

        moduleListPage.clickItemOverflow(fileFolder.displayName.orEmpty())

        updateFilePermissionsPage.swipeUpBottomSheet()
        updateFilePermissionsPage.clickUnpublishRadioButton()
        updateFilePermissionsPage.clickUpdateButton()

        moduleListPage.assertModuleItemNotPublished(fileFolder.displayName.orEmpty())
    }

    @Test
    fun publishFileModuleItem() {
        val data = goToModulesPage()
        val module = data.courseModules.values.first().first()
        val course = data.courses.values.first()
        val fileId = data.addFileToCourse(course.id)
        val rootFolderId = data.courseRootFolders[course.id]!!.id
        val fileFolder = data.folderFiles[rootFolderId]?.find { it.id == fileId }
        data.addItemToModule(
            course = course,
            moduleId = module.id,
            item = fileFolder!!,
            contentId = fileId,
            published = false,
            moduleContentDetails = ModuleContentDetails(
                hidden = false,
                locked = true
            )
        )

        moduleListPage.refresh()

        moduleListPage.clickItemOverflow(fileFolder.displayName.orEmpty())

        updateFilePermissionsPage.swipeUpBottomSheet()
        updateFilePermissionsPage.clickPublishRadioButton()
        updateFilePermissionsPage.clickUpdateButton()

        moduleListPage.assertModuleItemIsPublished(fileFolder.displayName.orEmpty())
    }

    @Test
    fun hideFileModuleItem() {
        val data = goToModulesPage()
        val module = data.courseModules.values.first().first()
        val course = data.courses.values.first()
        val fileId = data.addFileToCourse(course.id)
        val rootFolderId = data.courseRootFolders[course.id]!!.id
        val fileFolder = data.folderFiles[rootFolderId]?.find { it.id == fileId }
        data.addItemToModule(
            course = course,
            moduleId = module.id,
            item = fileFolder!!,
            contentId = fileId,
            published = true,
            moduleContentDetails = ModuleContentDetails(
                hidden = false,
                locked = false
            )
        )

        moduleListPage.refresh()

        moduleListPage.clickItemOverflow(fileFolder.displayName.orEmpty())

        updateFilePermissionsPage.swipeUpBottomSheet()
        updateFilePermissionsPage.clickHideRadioButton()
        updateFilePermissionsPage.clickUpdateButton()

        moduleListPage.assertModuleItemHidden(fileFolder.displayName.orEmpty())
    }

    @Test
    fun assertModuleItemDisabled() {
        val data = goToModulesPage()
        val module = data.courseModules.values.first().first()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(courseId = data.courses.values.first().id)
        data.addItemToModule(
            course = course,
            moduleId = module.id,
            item = assignment,
            published = true,
            moduleContentDetails = ModuleContentDetails(
                hidden = false,
                locked = true
            ),
            unpublishable = false
        )

        moduleListPage.refresh()

        moduleListPage.clickItemOverflow(assignment.name.orEmpty())
        moduleListPage.assertSnackbarContainsText(assignment.name.orEmpty())
    }


    private fun goToModulesPage(publishedModuleCount: Int = 1, unpublishedModuleCount: Int = 0): MockCanvas {
        val data = MockCanvas.init(teacherCount = 1, courseCount = 1, favoriteCourseCount = 1)
        val course = data.courses.values.first()

        data.addCoursePermissions(
            course.id,
            CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        val modulesTab = Tab(position = 2, label = "Modules", visibility = "public", tabId = Tab.MODULES_ID)
        data.courseTabs[course.id]!! += modulesTab

        repeat(publishedModuleCount) { data.addModuleToCourse(course, Randomizer.randomModuleName(), published = true) }
        repeat(unpublishedModuleCount) {
            data.addModuleToCourse(
                course,
                Randomizer.randomModuleName(),
                published = false
            )
        }

        val teacher = data.teachers.first()
        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)

        dashboardPage.openCourse(course)
        courseBrowserPage.openModulesTab()
        return data
    }

}