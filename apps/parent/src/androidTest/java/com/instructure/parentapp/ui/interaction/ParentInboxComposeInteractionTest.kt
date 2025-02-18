package com.instructure.parentapp.ui.interaction

import androidx.compose.ui.platform.ComposeView
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck
import com.instructure.canvas.espresso.common.interaction.InboxComposeInteractionTest
import com.instructure.canvas.espresso.common.pages.InboxPage
import com.instructure.canvas.espresso.common.pages.compose.InboxComposePage
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addRecipientsToCourse
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeInboxSettingsManager
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.di.GraphQlApiModule
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.parentapp.BuildConfig
import com.instructure.parentapp.features.login.LoginActivity
import com.instructure.parentapp.ui.pages.DashboardPage
import com.instructure.parentapp.ui.pages.ParentInboxCoursePickerPage
import com.instructure.parentapp.utils.ParentActivityTestRule
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers
import org.junit.Test

@HiltAndroidTest
@UninstallModules(GraphQlApiModule::class)
class ParentInboxComposeInteractionTest: InboxComposeInteractionTest() {
    override val isTesting = BuildConfig.IS_TESTING

    override val activityRule = ParentActivityTestRule(LoginActivity::class.java)

    private val dashboardPage = DashboardPage()
    private val inboxPage = InboxPage()
    private val inboxComposePage = InboxComposePage(composeTestRule)
    private val inboxCoursePickerPage = ParentInboxCoursePickerPage(composeTestRule)

    @BindValue
    @JvmField
    val inboxSettingsManager: InboxSettingsManager = FakeInboxSettingsManager()

    @Test
    fun testParentComposeDefaultValues() {
        val data = initData(canSendToAll = true)
        data.recipientGroups[getFirstCourse().id] = listOf(
            Recipient(
                stringId = getTeachers().first().id.toString(),
                name = getTeachers().first().name,
                commonCourses = hashMapOf(
                    getFirstCourse().id.toString() to arrayOf(EnrollmentType.TEACHERENROLLMENT.rawValue())
                )
            )
        )
        val parent = data.parents.first()
        val token = data.tokenFor(parent)!!
        tokenLogin(data.domain, token, parent)

        dashboardPage.openNavigationDrawer()
        dashboardPage.clickInbox()

        inboxPage.pressNewMessageButton()

        inboxCoursePickerPage.selectCourseWithUser(getFirstCourse().name, observedUserName = getObservedStudent().shortName ?: getObservedStudent().name)

        composeTestRule.waitUntil { !inboxComposePage.isRecipientsLoading() }

        inboxComposePage.assertContextSelected(getFirstCourse().name)
        inboxComposePage.assertSubjectText(getFirstCourse().name)
        inboxComposePage.assertRecipientSelected(getTeachers().first().name)
    }

    override fun goToInboxCompose(data: MockCanvas) {
        val parent = data.parents.first()
        val token = data.tokenFor(parent)!!
        tokenLogin(data.domain, token, parent)

        dashboardPage.openNavigationDrawer()
        dashboardPage.clickInbox()

        inboxPage.pressNewMessageButton()

        inboxCoursePickerPage.selectCourseWithUser(getFirstCourse().name, observedUserName = getObservedStudent().shortName ?: getObservedStudent().name)

        composeTestRule.waitUntil { !inboxComposePage.isRecipientsLoading() }
        inboxComposePage.removeAllRecipients()
    }

    override fun initData(canSendToAll: Boolean, sendMessages: Boolean): MockCanvas {
        val data =  MockCanvas.init(
            parentCount = 1,
            studentCount = 1,
            teacherCount = 2,
            courseCount = 1,
            favoriteCourseCount = 1
        )
        data.addRecipientsToCourse(
            course = data.courses.values.first(),
            students = data.students,
            teachers = data.teachers,
        )

        data.addCoursePermissions(
            data.courses.values.first().id,
            CanvasContextPermission(send_messages_all = canSendToAll, send_messages = sendMessages)
        )

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

    private fun getObservedStudent(): User = MockCanvas.data.students[0]

    override fun getLoggedInUser(): User = MockCanvas.data.parents[0]

    override fun getTeachers(): List<User> { return MockCanvas.data.teachers }

    override fun getFirstCourse(): Course { return MockCanvas.data.courses.values.first() }

    override fun getSentConversation(): Conversation? { return MockCanvas.data.sentConversation }

    override fun selectContext() = Unit
}