package com.instructure.student.ui.interaction

import androidx.compose.ui.platform.ComposeView
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.rule.GrantPermissionRule
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addUserPermissions
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.student.R
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class ProfileSettingsInteractionTest : StudentComposeTest() {

    override fun displaysPageObjects() = Unit // Not used for interaction tests

    // This will give our test(s) permission to access external storage
    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SETTINGS, TestCategory.INTERACTION)
    fun testProfileSettings_changeUsername() {
        val data = MockCanvas.init(studentCount = 1, teacherCount = 1, courseCount = 1, favoriteCourseCount = 1)
        val student = data.students[0]
        val newUserName = "New User Name"

        data.addUserPermissions(userId = student.id, canUpdateName = true, canUpdateAvatar = true)

        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)

        leftSideNavigationDrawerPage.clickSettingsMenu()
        settingsPage.clickOnSettingsItem("Profile Settings")
        profileSettingsPage.changeUserNameTo(newUserName)

        Espresso.pressBack() // to settings page
        Espresso.pressBack() // to dashboard

        leftSideNavigationDrawerPage.assertUserLoggedIn(newUserName)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SETTINGS, TestCategory.INTERACTION)
    fun testProfileSettings_disabledIfNoPermissions() {
        val data = MockCanvas.init(studentCount = 1, teacherCount = 1, courseCount = 1, favoriteCourseCount = 1)
        val student = data.students[0]

        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)

        leftSideNavigationDrawerPage.clickSettingsMenu()
        settingsPage.clickOnSettingsItem("Profile Settings")
        profileSettingsPage.assertSettingsDisabled() // No permissions granted
    }


    // Creates a panda avatar, saves it, and verifies that a new panda avatar was saved.
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SETTINGS, TestCategory.INTERACTION)
    fun testProfileSettings_createPandaAvatar() {
        val data = MockCanvas.init(studentCount = 1, teacherCount = 1, courseCount = 1, favoriteCourseCount = 1)
        val student = data.students[0]

        // Add permission for student to update his/her avatar
        data.addUserPermissions(userId = student.id, canUpdateName = true, canUpdateAvatar = true)

        // Read the saved panda avatar count
        val originalSavedPandaAvatarCount = getSavedPandaAvatarCount()

        // Sign in
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)

        // Navigate to avatar creation page
        leftSideNavigationDrawerPage.clickSettingsMenu()
        settingsPage.clickOnSettingsItem("Profile Settings")
        profileSettingsPage.launchPandaAvatarCreator()

        // Select head
        pandaAvatarPage.selectChangeHead()
        pandaAvatarPage.choosePart(R.string.content_description_panda_head_4) // Fancy moustache
        pandaAvatarPage.clickBackButton()

        // Select body
        pandaAvatarPage.selectChangeBody()
        pandaAvatarPage.choosePart(R.string.content_description_panda_body_4) // Blue blazer, red bowtie
        pandaAvatarPage.clickBackButton()

        // Select legs
        pandaAvatarPage.selectChangeLegs()
        pandaAvatarPage.choosePart(R.string.content_description_panda_feet_5) // Red shoes
        pandaAvatarPage.clickBackButton()

        // Save as avatar
        pandaAvatarPage.save()

        // Verify that our saved panda avatar count has increased
        val finalSavedPandaAvatarCount = getSavedPandaAvatarCount()
        assertTrue(
                "Expected finalSavedPandaAvatarCount($finalSavedPandaAvatarCount) to be one more than originalSavedPandaAvatarCount($originalSavedPandaAvatarCount)",
                finalSavedPandaAvatarCount == originalSavedPandaAvatarCount + 1
        )

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