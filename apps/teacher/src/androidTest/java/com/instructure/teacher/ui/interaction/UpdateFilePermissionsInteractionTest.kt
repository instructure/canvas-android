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

import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockcanvas.addFileToCourse
import com.instructure.canvas.espresso.mockcanvas.addItemToModule
import com.instructure.canvas.espresso.mockcanvas.addModuleToCourse
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.dataseeding.util.Randomizer
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.util.Calendar
import java.util.Date

@HiltAndroidTest
class UpdateFilePermissionsInteractionTest : TeacherTest() {

    override fun displaysPageObjects() = Unit

    @Test
    fun assertFilePublished() {
        goToPage(fileAvailability = "published")
        updateFilePermissionsPage.assertFilePublished()
    }

    @Test
    fun assertFileUnpublished() {
        goToPage(fileAvailability = "unpublished")
        updateFilePermissionsPage.assertFileUnpublished()
    }

    @Test
    fun assertFileHidden() {
        goToPage(fileAvailability = "hidden")
        updateFilePermissionsPage.assertFileHidden()
    }

    @Test
    fun assertFileScheduled() {
        val calendar = Calendar.getInstance()
        val unlockDate = calendar.time
        val lockDate = calendar.apply { add(Calendar.MONTH, 1) }.time
        goToPage(fileAvailability = "scheduled", unlockDate = unlockDate, lockDate = lockDate)
        updateFilePermissionsPage.assertFileScheduled()
    }

    @Test
    fun assertFileVisibilityInherit() {
        goToPage(fileVisibility = "inherit", fileAvailability = "published")
        updateFilePermissionsPage.assertFileVisibilityInherit()
    }

    @Test
    fun assertFileVisibilityContext() {
        goToPage(fileVisibility = "context", fileAvailability = "published")
        updateFilePermissionsPage.assertFileVisibilityContext()
    }

    @Test
    fun assertFileVisibilityInstitution() {
        goToPage(fileVisibility = "institution", fileAvailability = "published")
        updateFilePermissionsPage.assertFileVisibilityInstitution()
    }

    @Test
    fun assertFileVisibilityPublic() {
        goToPage(fileVisibility = "public", fileAvailability = "published")
        updateFilePermissionsPage.assertFileVisibilityPublic()
    }

    @Test
    fun assertScheduleLayoutVisible() {
        val calendar = Calendar.getInstance()
        val unlockDate = calendar.time
        val lockDate = calendar.apply { add(Calendar.MONTH, 1) }.time
        goToPage(fileAvailability = "scheduled", unlockDate = unlockDate, lockDate = lockDate)
        updateFilePermissionsPage.assertScheduleLayoutDisplayed()
    }

    @Test
    fun assertScheduleLayoutNotVisible() {
        goToPage(fileAvailability = "published")
        updateFilePermissionsPage.assertScheduleLayoutNotDisplayed()
    }

    @Test
    fun assertUnlockDate() {
        val calendar = Calendar.getInstance()
        val unlockDate = calendar.time
        val lockDate = calendar.apply { add(Calendar.MONTH, 1) }.time
        goToPage(fileAvailability = "scheduled", unlockDate = unlockDate, lockDate = lockDate)
        updateFilePermissionsPage.assertUnlockDate(unlockDate)
    }

    @Test
    fun assertLockDate() {
        val calendar = Calendar.getInstance()
        val unlockDate = calendar.time
        val lockDate = calendar.apply { add(Calendar.MONTH, 1) }.time
        goToPage(fileAvailability = "scheduled", unlockDate = unlockDate, lockDate = lockDate)
        updateFilePermissionsPage.assertLockDate(lockDate)
    }

    @Test
    fun assertVisibilityDisabledIfUnpublished() {
        goToPage(fileVisibility = "public", fileAvailability = "unpublished")
        updateFilePermissionsPage.assertVisibilityDisabled()
    }

    @Test
    fun assertVisibilityEnabled() {
        goToPage(fileVisibility = "public", fileAvailability = "published")
        updateFilePermissionsPage.assertVisibilityEnabled()
    }

    private fun goToPage(fileVisibility: String = "inherit", fileAvailability: String = "published", unlockDate: Date? = null, lockDate: Date? = null) : MockCanvas {
        val data = MockCanvas.init(teacherCount = 1, courseCount = 1, favoriteCourseCount = 1)
        val course = data.courses.values.first()

        data.addCoursePermissions(
            course.id,
            CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        val modulesTab = Tab(position = 2, label = "Modules", visibility = "public", tabId = Tab.MODULES_ID)
        data.courseTabs[course.id]!! += modulesTab

        data.addModuleToCourse(course, Randomizer.randomModuleName(), published = true)

        val fileId = data.addFileToCourse(course.id, visibilityLevel = fileVisibility)
        val rootFolderId = data.courseRootFolders[course.id]!!.id
        val fileFolder = data.folderFiles[rootFolderId]?.find { it.id == fileId }

        val module = data.courseModules.values.first().first()

        data.addItemToModule(
            course = course,
            moduleId = module.id,
            item = fileFolder!!,
            contentId = fileId,
            published = fileAvailability == "published",
            moduleContentDetails = ModuleContentDetails(
                hidden = fileAvailability == "hidden",
                locked = fileAvailability == "unpublished",
                unlockAt = unlockDate?.toApiString(),
                lockAt = lockDate?.toApiString()
            )
        )

        val teacher = data.teachers.first()
        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)

        dashboardPage.openCourse(course)
        courseBrowserPage.openModulesTab()
        moduleListPage.clickItemOverflow(fileFolder.name.orEmpty())
        updateFilePermissionsPage.swipeUpBottomSheet()
        return data
    }
}