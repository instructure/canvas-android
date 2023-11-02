/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

import android.text.format.Formatter
import androidx.test.espresso.Espresso
import com.google.android.material.checkbox.MaterialCheckBox
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addFileToCourse
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Tab
import com.instructure.dataseeding.util.Randomizer
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.StorageUtils
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class OfflineContentInteractionTest : StudentTest() {

    @Inject
    lateinit var storageUtils: StorageUtils

    override fun displaysPageObjects() = Unit

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.OFFLINE_CONTENT, TestCategory.INTERACTION, false)
    fun displaysNoCourses() {
        goToOfflineContent(createMockCanvas(courseCount = 0))
        manageOfflineContentPage.assertDisplaysNoCourses()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.OFFLINE_CONTENT, TestCategory.INTERACTION, false)
    fun displaysEmptyCourse() {
        val data = createMockCanvas(courseCount = 1, hasTabs = false)
        goToOfflineContent(data)
        manageOfflineContentPage.expandCollapseItem(data.courses.values.first().name)
        manageOfflineContentPage.assertDisplaysEmptyCourse()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.OFFLINE_CONTENT, TestCategory.INTERACTION, false)
    fun displaysCoursesCollapsedIfGlobalOfflineContent() {
        val data = createMockCanvas()
        goToOfflineContent(data)
        manageOfflineContentPage.assertDisplaysItemWithExpandedState(data.courses.values.first().name, false)
        manageOfflineContentPage.assertDisplaysItemWithExpandedState(data.courses.values.last().name, false)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.OFFLINE_CONTENT, TestCategory.INTERACTION, false)
    fun displaysCourseExpandedIfCourseOfflineContent() {
        val data = createMockCanvas()
        goToOfflineContentByCourse(data)
        manageOfflineContentPage.assertDisplaysItemWithExpandedState(data.courses.values.first().name, true)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.OFFLINE_CONTENT, TestCategory.INTERACTION, false)
    fun displaysCourseTabsAndFiles() {
        val data = createMockCanvas(courseCount = 1)
        goToOfflineContent(data)
        val course = data.courses.values.first()
        manageOfflineContentPage.expandCollapseItem(course.name)
        getCourseItemNames(data, course).forEach { manageOfflineContentPage.assertItemDisplayed(it) }
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.OFFLINE_CONTENT, TestCategory.INTERACTION, false)
    fun expandCourse() {
        val data = createMockCanvas()
        goToOfflineContent(data)
        val course = data.courses.values.first()
        manageOfflineContentPage.assertDisplaysItemWithExpandedState(course.name, false)
        manageOfflineContentPage.expandCollapseItem(course.name)
        manageOfflineContentPage.assertDisplaysItemWithExpandedState(course.name, true)
        manageOfflineContentPage.assertItemDisplayed(data.courseTabs[course.id]!!.first().label!!)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.OFFLINE_CONTENT, TestCategory.INTERACTION, false)
    fun selectCourse() {
        val data = createMockCanvas()
        goToOfflineContent(data)
        val course = data.courses.values.first()
        manageOfflineContentPage.expandCollapseItem(course.name)
        manageOfflineContentPage.assertCheckedStateOfItem(course.name, MaterialCheckBox.STATE_UNCHECKED)
        manageOfflineContentPage.changeItemSelectionState(course.name)
        getCourseItemNames(data, course).forEach { manageOfflineContentPage.assertCheckedStateOfItem(it, MaterialCheckBox.STATE_CHECKED) }
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.OFFLINE_CONTENT, TestCategory.INTERACTION, false)
    fun selectTab() {
        val data = createMockCanvas()
        goToOfflineContent(data)
        val course = data.courses.values.first()
        val firstTabName = data.courseTabs[course.id]!!.map { it.label!! }.first()
        manageOfflineContentPage.expandCollapseItem(course.name)
        manageOfflineContentPage.assertCheckedStateOfItem(course.name, MaterialCheckBox.STATE_UNCHECKED)
        manageOfflineContentPage.changeItemSelectionState(firstTabName)
        manageOfflineContentPage.assertCheckedStateOfItem(course.name, MaterialCheckBox.STATE_INDETERMINATE)
        manageOfflineContentPage.assertCheckedStateOfItem(firstTabName, MaterialCheckBox.STATE_CHECKED)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.OFFLINE_CONTENT, TestCategory.INTERACTION, false)
    fun expandFilesTab() {
        val data = createMockCanvas()
        goToOfflineContent(data)
        val course = data.courses.values.first()
        val filesTabName = data.courseTabs[course.id]!!.find { it.tabId == Tab.FILES_ID }!!.label!!
        val firstFileName = data.folderFiles[data.courseRootFolders[course.id]!!.id]!!.map { it.displayName!! }.first()
        manageOfflineContentPage.expandCollapseItem(course.name)
        manageOfflineContentPage.expandCollapseItem(filesTabName)
        manageOfflineContentPage.assertDisplaysItemWithExpandedState(filesTabName, false)
        manageOfflineContentPage.expandCollapseItem(filesTabName)
        manageOfflineContentPage.assertItemDisplayed(firstFileName)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.OFFLINE_CONTENT, TestCategory.INTERACTION, false)
    fun selectFilesTab() {
        val data = createMockCanvas()
        goToOfflineContent(data)
        val course = data.courses.values.first()
        val filesTabName = data.courseTabs[course.id]!!.find { it.tabId == Tab.FILES_ID }!!.label!!
        val fileNames = data.folderFiles[data.courseRootFolders[course.id]!!.id]!!.map { it.displayName!! }
        manageOfflineContentPage.expandCollapseItem(course.name)
        manageOfflineContentPage.assertCheckedStateOfItem(course.name, MaterialCheckBox.STATE_UNCHECKED)
        manageOfflineContentPage.assertCheckedStateOfItem(filesTabName, MaterialCheckBox.STATE_UNCHECKED)
        manageOfflineContentPage.changeItemSelectionState(filesTabName)
        manageOfflineContentPage.assertCheckedStateOfItem(course.name, MaterialCheckBox.STATE_INDETERMINATE)
        manageOfflineContentPage.assertCheckedStateOfItem(filesTabName, MaterialCheckBox.STATE_CHECKED)
        fileNames.forEach { manageOfflineContentPage.assertCheckedStateOfItem(it, MaterialCheckBox.STATE_CHECKED) }
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.OFFLINE_CONTENT, TestCategory.INTERACTION, false)
    fun selectFile() {
        val data = createMockCanvas()
        goToOfflineContent(data)
        val course = data.courses.values.first()
        val filesTabName = data.courseTabs[course.id]!!.find { it.tabId == Tab.FILES_ID }!!.label!!
        val firstFileName = data.folderFiles[data.courseRootFolders[course.id]!!.id]!!.map { it.displayName!! }.first()
        manageOfflineContentPage.expandCollapseItem(course.name)
        manageOfflineContentPage.assertCheckedStateOfItem(course.name, MaterialCheckBox.STATE_UNCHECKED)
        manageOfflineContentPage.assertCheckedStateOfItem(filesTabName, MaterialCheckBox.STATE_UNCHECKED)
        manageOfflineContentPage.changeItemSelectionState(firstFileName)
        manageOfflineContentPage.assertCheckedStateOfItem(course.name, MaterialCheckBox.STATE_INDETERMINATE)
        manageOfflineContentPage.assertCheckedStateOfItem(filesTabName, MaterialCheckBox.STATE_INDETERMINATE)
        manageOfflineContentPage.assertCheckedStateOfItem(firstFileName, MaterialCheckBox.STATE_CHECKED)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.OFFLINE_CONTENT, TestCategory.INTERACTION, false)
    fun selectAllFiles() {
        val data = createMockCanvas(courseCount = 1)
        goToOfflineContent(data)
        val course = data.courses.values.first()
        val filesTabName = data.courseTabs[course.id]!!.find { it.tabId == Tab.FILES_ID }!!.label!!
        val fileNames = data.folderFiles[data.courseRootFolders[course.id]!!.id]!!.map { it.displayName!! }
        manageOfflineContentPage.expandCollapseItem(course.name)
        fileNames.forEachIndexed { index, file ->
            manageOfflineContentPage.changeItemSelectionState(file)
            manageOfflineContentPage.assertCheckedStateOfItem(
                filesTabName,
                if (index == fileNames.size - 1) MaterialCheckBox.STATE_CHECKED else MaterialCheckBox.STATE_INDETERMINATE
            )
        }
        manageOfflineContentPage.assertCheckedStateOfItem(course.name, MaterialCheckBox.STATE_INDETERMINATE)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.OFFLINE_CONTENT, TestCategory.INTERACTION, false)
    fun selectAllTabs() {
        val data = createMockCanvas()
        goToOfflineContent(data)
        val course = data.courses.values.first()
        val tabNames = data.courseTabs[course.id]!!.map { it.label!! }
        manageOfflineContentPage.expandCollapseItem(course.name)
        manageOfflineContentPage.assertCheckedStateOfItem(course.name, MaterialCheckBox.STATE_UNCHECKED)
        tabNames.forEachIndexed { index, tab ->
            manageOfflineContentPage.changeItemSelectionState(tab)
            manageOfflineContentPage.assertCheckedStateOfItem(
                course.name,
                if (index == tabNames.size - 1) MaterialCheckBox.STATE_CHECKED else MaterialCheckBox.STATE_INDETERMINATE
            )
        }
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.OFFLINE_CONTENT, TestCategory.INTERACTION, false)
    fun selectAllToggle() {
        val data = createMockCanvas()
        goToOfflineContent(data)
        manageOfflineContentPage.clickOnSelectAllButton()
        data.courses.values.forEach {
            manageOfflineContentPage.assertCheckedStateOfItem(it.name, MaterialCheckBox.STATE_CHECKED)
        }
        manageOfflineContentPage.clickOnDeselectAllButton()
        data.courses.values.forEach {
            manageOfflineContentPage.assertCheckedStateOfItem(it.name, MaterialCheckBox.STATE_UNCHECKED)
        }
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.OFFLINE_CONTENT, TestCategory.INTERACTION, false)
    fun displaysDiscardDialogIfNeeded() {
        goToOfflineContent()
        Espresso.pressBack()
        dashboardPage.openGlobalManageOfflineContentPage()
        manageOfflineContentPage.clickOnSelectAllButton()
        Espresso.pressBack()
        manageOfflineContentPage.assertDiscardDialogDisplayed()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.OFFLINE_CONTENT, TestCategory.INTERACTION, false)
    fun displaysWifiOnlySyncDialog() {
        val data = createMockCanvas()
        val course = data.courses.values.first()
        goToOfflineContent(data)
        manageOfflineContentPage.changeItemSelectionState(course.name)
        manageOfflineContentPage.clickOnSyncButton()
        manageOfflineContentPage.assertSyncDialogDisplayed(
            activityRule.activity.getString(
                R.string.offline_content_sync_dialog_message_wifi_only,
                Formatter.formatShortFileSize(activityRule.activity, getCourseContentSize(data, course))
            )
        )
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.OFFLINE_CONTENT, TestCategory.INTERACTION, false)
    fun displaysSyncDialog() {
        val data = createMockCanvas()
        val course = data.courses.values.first()
        setupSyncAndGoToOfflineContent(data)
        manageOfflineContentPage.changeItemSelectionState(course.name)
        manageOfflineContentPage.clickOnSyncButton()
        manageOfflineContentPage.assertSyncDialogDisplayed(
            activityRule.activity.getString(
                R.string.offline_content_sync_dialog_message,
                Formatter.formatShortFileSize(activityRule.activity, getCourseContentSize(data, course))
            )
        )
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.OFFLINE_CONTENT, TestCategory.INTERACTION, false)
    fun savesChangesOnSync() {
        val data = createMockCanvas()
        goToOfflineContent(data)
        manageOfflineContentPage.clickOnSelectAllButton()
        manageOfflineContentPage.clickOnSyncButtonAndConfirm()
        dashboardPage.openGlobalManageOfflineContentPage()
        data.courses.values.forEach { manageOfflineContentPage.assertCheckedStateOfItem(it.name, MaterialCheckBox.STATE_CHECKED) }
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.OFFLINE_CONTENT, TestCategory.INTERACTION, false)
    fun calculatesStorageInfoCorrectly() {
        val data = createMockCanvas(fileCount = 10, largeFiles = true)
        val course = data.courses.values.first()
        goToOfflineContent(data)
        val total = storageUtils.getTotalSpace()
        val used = total - storageUtils.getFreeSpace()
        manageOfflineContentPage.assertStorageInfoDetails()
        manageOfflineContentPage.expandCollapseItem(course.name)
        manageOfflineContentPage.assertStorageInfoText(
            activityRule.activity.getString(
                R.string.offline_content_storage_info,
                Formatter.formatShortFileSize(activityRule.activity, used),
                Formatter.formatShortFileSize(activityRule.activity, total)
            )
        )
        manageOfflineContentPage.changeItemSelectionState(course.name)
        manageOfflineContentPage.assertStorageInfoText(
            activityRule.activity.getString(
                R.string.offline_content_storage_info,
                Formatter.formatShortFileSize(activityRule.activity, used + getCourseContentSize(data, course)),
                Formatter.formatShortFileSize(activityRule.activity, total)
            )
        )
    }

    private fun createMockCanvas(courseCount: Int = 2, hasTabs: Boolean = true, fileCount: Int = 3, largeFiles: Boolean = false): MockCanvas {
        val data = MockCanvas.init(studentCount = 1, teacherCount = 1, courseCount = courseCount)
        data.offlineModeEnabled = true

        if (hasTabs) {
            val filesTab = Tab(position = 2, label = "Files", visibility = "public", tabId = Tab.FILES_ID)

            data.courses.forEach { course ->
                val courseId = course.value.id

                data.courseTabs[courseId]?.add(filesTab)

                repeat(fileCount) {
                    data.addFileToCourse(
                        courseId = courseId,
                        displayName = "test-${courseId}-${it}.pdf",
                        contentType = "application/pdf",
                        fileContent = if (largeFiles) Randomizer.randomLargeTextFileContents() else Randomizer.randomTextFileContents()
                    )
                }
            }
        } else {
            data.courseTabs.clear()
        }

        return data
    }

    private fun goToOfflineContent(data: MockCanvas = createMockCanvas()) {
        val student = data.users.values.first()
        val token = data.tokenFor(student).orEmpty()
        tokenLogin(data.domain, token, student)
        dashboardPage.openGlobalManageOfflineContentPage()
    }

    private fun goToOfflineContentByCourse(data: MockCanvas = createMockCanvas()) {
        val student = data.students.first()
        val token = data.tokenFor(student).orEmpty()
        tokenLogin(data.domain, token, student)
        dashboardPage.clickCourseOverflowMenu(data.courses.values.first().name, "Manage Offline Content")
    }

    private fun getCourseItemNames(data: MockCanvas, course: Course): List<String> {
        return data.courseTabs[course.id]!!.map { it.label!! } + course.name +
                data.folderFiles[data.courseRootFolders[course.id]!!.id]!!.map { it.displayName!! }
    }

    private fun getCourseContentSize(data: MockCanvas, course: Course): Long {
        return data.folderFiles[data.courseRootFolders[course.id]!!.id]!!.sumOf { it.size } +
                data.courseTabs[course.id]!!.filter { it.tabId != Tab.FILES_ID }.size * 100000
    }

    private fun setupSyncAndGoToOfflineContent(data: MockCanvas = createMockCanvas()) {
        val student = data.users.values.first()
        val token = data.tokenFor(student).orEmpty()
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()
        leftSideNavigationDrawerPage.clickSettingsMenu()
        settingsPage.openOfflineContentPage()
        syncSettingsPage.clickWifiOnlySwitch()
        syncSettingsPage.assertDialogDisplayedWithTitle(R.string.syncSettings_wifiConfirmationTitle)
        syncSettingsPage.clickTurnOff()
        Espresso.pressBack()
        Espresso.pressBack()
        dashboardPage.openGlobalManageOfflineContentPage()
    }
}
