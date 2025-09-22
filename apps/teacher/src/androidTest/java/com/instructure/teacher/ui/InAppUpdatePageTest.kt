/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.teacher.ui

import android.app.NotificationManager
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.di.UpdateModule
import com.instructure.pandautils.update.UpdateManager
import com.instructure.pandautils.update.UpdatePrefs
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import org.junit.Test
import org.threeten.bp.OffsetDateTime

@UninstallModules(UpdateModule::class)
@HiltAndroidTest
class InAppUpdatePageTest : TeacherTest() {

    override fun displaysPageObjects() = Unit

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @BindValue
    @JvmField
    val appUpdateManager: FakeAppUpdateManager = FakeAppUpdateManager(context)

    @BindValue
    @JvmField
    val updatePrefs: UpdatePrefs = UpdatePrefs

    @BindValue
    @JvmField
    val updateManager: UpdateManager = UpdateManager(appUpdateManager, notificationManager, updatePrefs)

    private val localUiDevice by lazy { UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()) }

    private fun goToDashboard() {
        val data = MockCanvas.init(
                teacherCount = 1,
                courseCount = 1,
                favoriteCourseCount = 1,
                createSections = true)
        val user = data.teachers[0]
        val token = data.tokenFor(user)!!
        tokenLogin(data.domain, token, user)
        dashboardPage.waitForRender()
    }

    @Test
    fun showFlexibleConfirmationDialogForFlexibleUpdate() {
        updatePrefs.clearPrefs()
        with(appUpdateManager) {
            setUpdateAvailable(400)
            setUpdatePriority(2)
            setClientVersionStalenessDays(10)
        }

        goToDashboard()

        assert(appUpdateManager.isConfirmationDialogVisible)
    }

    @Test
    fun hideFlexibleConfirmationDialogIfUserDeclines() {
        updatePrefs.clearPrefs()
        with(appUpdateManager) {
            setUpdateAvailable(400)
            setUpdatePriority(2)
            setClientVersionStalenessDays(10)
        }

        goToDashboard()

        assert(appUpdateManager.isConfirmationDialogVisible)

        appUpdateManager.userRejectsUpdate()

        assertFalse(appUpdateManager.isConfirmationDialogVisible)
    }

    @Test
    fun hideFlexibleConfirmationDialogIfUserAccepts() {
        updatePrefs.clearPrefs()
        with(appUpdateManager) {
            setUpdateAvailable(400)
            setUpdatePriority(2)
            setClientVersionStalenessDays(10)
        }

        goToDashboard()

        assert(appUpdateManager.isConfirmationDialogVisible)

        appUpdateManager.userAcceptsUpdate()

        assertFalse(appUpdateManager.isConfirmationDialogVisible)
    }

    @Test
    fun hideFlexibleConfirmationDialogIfItHasBeenShownToday() {
        with(updatePrefs) {
            lastUpdateNotificationCount = 1
            lastUpdateNotificationVersionCode = 400
            lastUpdateNotificationDate = OffsetDateTime.now().toApiString()!!
        }

        with(appUpdateManager) {
            setUpdateAvailable(400)
            setUpdatePriority(2)
            setClientVersionStalenessDays(10)
        }

        goToDashboard()

        assertFalse(appUpdateManager.isConfirmationDialogVisible)
    }

    @Test
    fun hideFlexibleConfirmationDialogIfItHasBeenShownTwice() {
        with(updatePrefs) {
            lastUpdateNotificationCount = 2
            lastUpdateNotificationVersionCode = 400
            lastUpdateNotificationDate = OffsetDateTime.now().toApiString()!!
        }

        with(appUpdateManager) {
            setUpdateAvailable(400)
            setUpdatePriority(2)
            setClientVersionStalenessDays(10)
        }

        goToDashboard()

        assertFalse(appUpdateManager.isConfirmationDialogVisible)
    }

    @Test
    fun showFlexibleConfirmationDialogForNewVersion() {
        with(updatePrefs) {
            lastUpdateNotificationCount = 2
            lastUpdateNotificationVersionCode = 399
            lastUpdateNotificationDate = OffsetDateTime.now().toApiString()!!
        }

        with(appUpdateManager) {
            setUpdateAvailable(400)
            setUpdatePriority(2)
            setClientVersionStalenessDays(10)
        }

        goToDashboard()

        assert(appUpdateManager.isConfirmationDialogVisible)
    }

    @Test
    fun hideFlexibleConfirmationIfItWasShownToday() {
        with(updatePrefs) {
            lastUpdateNotificationCount = 1
            lastUpdateNotificationVersionCode = 400
            lastUpdateNotificationDate = OffsetDateTime.now().toApiString()!!
            hasShownThisStart = true
        }

        with(appUpdateManager) {
            setUpdateAvailable(400)
            setUpdatePriority(2)
            setClientVersionStalenessDays(10)
        }

        goToDashboard()

        assertFalse(appUpdateManager.isConfirmationDialogVisible)
    }

    @Test
    fun showImmediateFlow() {
        with(appUpdateManager) {
            setUpdateAvailable(400)
            setUpdatePriority(4)
            setClientVersionStalenessDays(10)
        }

        goToDashboard()

        assert(appUpdateManager.isImmediateFlowVisible)
    }

    @Test
    fun hideImmediateFlowIfItWasShownThisStart() {
        with(updatePrefs) {
            lastUpdateNotificationCount = 1
            lastUpdateNotificationVersionCode = 400
            lastUpdateNotificationDate = OffsetDateTime.now().minusDays(2).toApiString()!!
            hasShownThisStart = true
        }

        with(appUpdateManager) {
            setUpdateAvailable(400)
            setUpdatePriority(4)
            setClientVersionStalenessDays(10)
        }

        goToDashboard()

        assertFalse(appUpdateManager.isImmediateFlowVisible)
    }

    @Test
    fun showNotificationOnFlexibleDownloadFinish() {
        updatePrefs.clearPrefs()
        val expectedTitle = context.getString(R.string.appUpdateReadyTitle)
        val expectedDescription = context.getString(R.string.appUpdateReadyDescription)

        with(appUpdateManager) {
            setUpdateAvailable(400)
            setUpdatePriority(2)
            setClientVersionStalenessDays(10)
        }

        goToDashboard()

        assert(appUpdateManager.isConfirmationDialogVisible)

        appUpdateManager.userAcceptsUpdate()
        appUpdateManager.downloadStarts()
        appUpdateManager.downloadCompletes()

        localUiDevice.openNotification()
        localUiDevice.wait(Until.hasObject(By.textStartsWith(context.getString(R.string.app_name))), 2)
        val title = localUiDevice.findObject(By.text(expectedTitle))
        val description = localUiDevice.findObject(By.text(expectedDescription))

        assertEquals(expectedTitle, title.text)
        assertEquals(expectedDescription, description.text)

        localUiDevice.pressBack()
    }

    @Test
    fun flexibleUpdateCompletesIfAppRestarts() {
        updatePrefs.clearPrefs()
        with(appUpdateManager) {
            setUpdateAvailable(400)
            setUpdatePriority(2)
            setClientVersionStalenessDays(10)
            userAcceptsUpdate()
            downloadStarts()
            downloadCompletes()
        }

        goToDashboard()

        assert(appUpdateManager.isInstallSplashScreenVisible)
    }

    @Test
    fun immediateUpdateCompletion() {
        with(appUpdateManager) {
            setUpdateAvailable(400)
            setUpdatePriority(4)
            setClientVersionStalenessDays(10)
        }

        goToDashboard()

        assert(appUpdateManager.isImmediateFlowVisible)

        appUpdateManager.userAcceptsUpdate()
        appUpdateManager.downloadStarts()
        appUpdateManager.downloadCompletes()

        assert(appUpdateManager.isInstallSplashScreenVisible)
    }

    @Test
    fun hideImmediateUpdateFlowIfUserCancels() {
        with(appUpdateManager) {
            setUpdateAvailable(400)
            setUpdatePriority(4)
            setClientVersionStalenessDays(10)
        }

        goToDashboard()

        assert(appUpdateManager.isImmediateFlowVisible)

        appUpdateManager.userRejectsUpdate()

        assertFalse(appUpdateManager.isImmediateFlowVisible)
    }
}