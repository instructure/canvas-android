package com.instructure.canvas.espresso.common.interaction

import com.instructure.canvas.espresso.CanvasComposeTest
import com.instructure.canvas.espresso.common.pages.compose.InboxComposePage
import com.instructure.canvas.espresso.common.pages.compose.RecipientPickerPage
import com.instructure.canvas.espresso.common.pages.compose.SelectContextPage
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.type.EnrollmentType
import org.junit.Test

abstract class InboxComposeInteractionTest: CanvasComposeTest() {

    private val inboxComposePage = InboxComposePage(composeTestRule)
    private val recipientPickerPage = RecipientPickerPage(composeTestRule)
    private val selectContextPage = SelectContextPage(composeTestRule)

    @Test
    fun assertNewTitle() {
        val data = initData()
        goToInboxCompose(data)
        composeTestRule.waitForIdle()

        inboxComposePage.assertTitle("New Message")
    }

    @Test
    fun assertInitialSendButtonState() {
        val data = initData()
        goToInboxCompose(data)
        composeTestRule.waitForIdle()

        inboxComposePage.assertIfSendButtonState(false)
    }

    @Test
    fun assertSendButtonStateAfterFill() {
        val data = initData()
        data.recipientGroups[getFirstCourse().id] = listOf(
            Recipient(
                stringId = getOtherUser().id.toString(),
                name = getOtherUser().name,
                commonCourses = hashMapOf(
                    getFirstCourse().id.toString() to arrayOf(EnrollmentType.TEACHERENROLLMENT.rawValue())
                )
            )
        )
        goToInboxCompose(data)
        composeTestRule.waitForIdle()

        inboxComposePage.pressCourseSelector()
        selectContextPage.selectContext(getFirstCourse().name)
        inboxComposePage.pressAddRecipient()
        recipientPickerPage.pressLabel("Teachers")
        recipientPickerPage.pressLabel(getOtherUser().name)
        recipientPickerPage.pressDone()
        inboxComposePage.typeSubject("Test Subject")
        inboxComposePage.typeBody("Test Body")

        inboxComposePage.assertIfSendButtonState(true)
    }

    @Test
    fun assertContextSelection() {
        val data = initData()
        goToInboxCompose(data)
        composeTestRule.waitForIdle()

        inboxComposePage.pressCourseSelector()

        selectContextPage.selectContext(getFirstCourse().name)

        inboxComposePage.assertContextSelected(getFirstCourse().name)
    }

    @Test
    fun assertRecipientSelection() {
        val data = initData()
        data.recipientGroups[getFirstCourse().id] = listOf(
            Recipient(
                stringId = getOtherUser().id.toString(),
                name = getOtherUser().name,
                commonCourses = hashMapOf(
                    getFirstCourse().id.toString() to arrayOf(EnrollmentType.TEACHERENROLLMENT.rawValue())
                )
            )
        )
        goToInboxCompose(data)
        composeTestRule.waitForIdle()

        inboxComposePage.pressCourseSelector()
        selectContextPage.selectContext(getFirstCourse().name)
        inboxComposePage.pressAddRecipient()
        recipientPickerPage.pressLabel("Teachers")
        recipientPickerPage.pressLabel(getOtherUser().name)
        recipientPickerPage.pressDone()

        inboxComposePage.assertRecipientSelected(getOtherUser().name)
        inboxComposePage.assertRecipientSearchDisplayed()
    }

    @Test
    fun assertSendIndividualButtonSwitched() {
        val data = initData()
        goToInboxCompose(data)
        composeTestRule.waitForIdle()

        inboxComposePage.assertIndividualSwitchState(false)
        inboxComposePage.pressIndividualSendSwitch()
        inboxComposePage.assertIndividualSwitchState(true)
    }

    @Test
    fun assertTypedSubjectIsDisplayed() {
        val data = initData()
        goToInboxCompose(data)
        composeTestRule.waitForIdle()

        inboxComposePage.typeSubject("Test Subject")
        inboxComposePage.assertSubjectText("Test Subject")
    }

    @Test
    fun assertTypedBodyIsDisplayed() {
        val data = initData()
        goToInboxCompose(data)
        composeTestRule.waitForIdle()

        inboxComposePage.typeBody("Test Body")
        inboxComposePage.assertBodyText("Test Body")
    }

    @Test
    fun assertAlertDialogPopsOnExit() {
        val data = initData()
        goToInboxCompose(data)
        composeTestRule.waitForIdle()

        inboxComposePage.pressBackButton()
        inboxComposePage.assertAlertDialog()
    }

    override fun displaysPageObjects() = Unit

    abstract fun initData(): MockCanvas

    abstract fun goToInboxCompose(data: MockCanvas)

    abstract fun getLoggedInUser(): User

    abstract fun getOtherUser(): User

    abstract fun getFirstCourse(): Course
}