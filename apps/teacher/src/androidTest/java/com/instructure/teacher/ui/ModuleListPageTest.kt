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

package com.instructure.teacher.ui

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addFileToCourse
import com.instructure.canvas.espresso.mockCanvas.addItemToModule
import com.instructure.canvas.espresso.mockCanvas.addModuleToCourse
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Tab
import com.instructure.dataseeding.util.Randomizer
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.openOverflowMenu
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import com.instructure.teacher.R

@HiltAndroidTest
class ModuleListPageTest : TeacherTest() {

    @Test
    override fun displaysPageObjects() {
        goToModulesPage()
        modulesPage.assertPageObjects()
    }

    @Test
    fun assertDisplaysMenuItems() {
        goToModulesPage()
        openOverflowMenu()
        modulesPage.assertToolbarMenuItems()
    }

    @Test
    fun assertDisplaysModuleMenuItems() {
        val data = goToModulesPage()
        val module = data.courseModules.values.first().first()

        modulesPage.clickItemOverflow(module.name.orEmpty())
        modulesPage.assertModuleMenuItems()
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

        modulesPage.refresh()

        modulesPage.clickItemOverflow(assignment.name.orEmpty())
        modulesPage.assertOverflowItem(R.string.unpublish)
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

        modulesPage.refresh()

        modulesPage.clickItemOverflow(assignment.name.orEmpty())
        modulesPage.assertOverflowItem(R.string.publish)
    }

    @Test
    fun assertFileEditOpens() {
        val data = goToModulesPage()
        val module = data.courseModules.values.first().first()
        val course = data.courses.values.first()
        val fileId = data.addFileToCourse(course.id, "Published file")
        val rootFolderId = data.courseRootFolders[course.id]!!.id
        val fileFolder = data.folderFiles[rootFolderId]?.find { it.id == fileId }
        data.addItemToModule(
            course = course,
            moduleId = module.id,
            item = fileFolder!!
        )

        modulesPage.refresh()

        modulesPage.clickItemOverflow(fileFolder.displayName.orEmpty())

        modulesPage.assertFileEditDialogVisible()
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

        modulesPage.refresh()

        modulesPage.clickItemOverflow(assignment.name.orEmpty())
        modulesPage.clickOnText(R.string.publish)
        modulesPage.clickOnText(R.string.publish)

        modulesPage.assertSnackbarText(R.string.moduleItemPublished)
        modulesPage.assertItemPublished(assignment.name.orEmpty())
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

        modulesPage.refresh()

        modulesPage.clickItemOverflow(assignment.name.orEmpty())
        modulesPage.clickOnText(R.string.unpublish)
        modulesPage.clickOnText(R.string.unpublish)

        modulesPage.assertSnackbarText(R.string.moduleItemUnpublished)
        modulesPage.assertItemUnublished(assignment.name.orEmpty())
    }

    @Test
    fun publishModuleOnly() {
        val data = goToModulesPage(publishedModuleCount = 0, unpublishedModuleCount = 1)
        val unpublishedModule = data.courseModules.values.first().first { it.published == false }
        val assignment = data.addAssignment(courseId = data.courses.values.first().id)

        data.addItemToModule(data.courses.values.first(), unpublishedModule.id, assignment, published = false)
        modulesPage.refresh()

        modulesPage.clickItemOverflow(unpublishedModule.name.orEmpty())
        modulesPage.clickOnText(R.string.publishModuleOnly)
        modulesPage.clickOnText(R.string.publish)

        modulesPage.assertSnackbarText(R.string.onlyModulePublished)
        modulesPage.assertModuleIsPublished()
        modulesPage.assertItemUnublished(assignment.name.orEmpty())
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