package com.instructure.canvas.espresso.common.interaction

import com.instructure.canvas.espresso.CanvasComposeTest
import com.instructure.canvas.espresso.common.pages.InboxPage
import com.instructure.canvas.espresso.common.pages.compose.InboxComposePage
import com.instructure.canvas.espresso.common.pages.compose.RecipientPickerPage
import com.instructure.canvas.espresso.common.pages.compose.SelectContextPage
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addSentConversation
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.type.EnrollmentType
import org.junit.Test

abstract class InboxComposeInteractionTest : CanvasComposeTest() {

    private val inboxPage = InboxPage()
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
                stringId = getTeachers().first().id.toString(),
                name = getTeachers().first().name,
                commonCourses = hashMapOf(
                    getFirstCourse().id.toString() to arrayOf(EnrollmentType.TeacherEnrollment.rawValue)
                )
            )
        )
        goToInboxCompose(data)
        composeTestRule.waitForIdle()

        selectContext()
        inboxComposePage.pressAddRecipient()
        recipientPickerPage.pressLabel("Teachers")
        recipientPickerPage.pressLabel(getTeachers().first().name)
        recipientPickerPage.pressDone()
        inboxComposePage.typeSubject("Test Subject")
        inboxComposePage.typeBody("Test Body")

        inboxComposePage.assertIfSendButtonState(true)
    }

    @Test
    fun sendMessageToSingleUser() {
        val data = initData()
        data.addSentConversation("Test Subject", getLoggedInUser().id, messageBody = "Test Body")
        data.recipientGroups[getFirstCourse().id] = listOf(
            Recipient(
                stringId = getTeachers().first().id.toString(),
                name = getTeachers().first().name,
                commonCourses = hashMapOf(
                    getFirstCourse().id.toString() to arrayOf(EnrollmentType.TeacherEnrollment.rawValue)
                )
            )
        )
        goToInboxCompose(data)
        composeTestRule.waitForIdle()

        selectContext()
        inboxComposePage.pressAddRecipient()
        recipientPickerPage.pressLabel("Teachers")
        recipientPickerPage.pressLabel(getTeachers().first().name)
        recipientPickerPage.pressDone()
        inboxComposePage.typeSubject("Test Subject")
        inboxComposePage.typeBody("Test Body")
        inboxComposePage.pressSendButton()

        composeTestRule.waitForIdle()

        inboxPage.filterInbox("Sent")
        inboxPage.assertConversationDisplayed("Test Subject")
    }

    @Test
    fun sendMessageToMultipleUsers() {
        val data = initData()
        data.addSentConversation("Test Subject", getLoggedInUser().id, messageBody = "Test Body")
        data.recipientGroups[getFirstCourse().id] = listOf(
            Recipient(
                stringId = getTeachers().first().id.toString(),
                name = getTeachers().first().name,
                commonCourses = hashMapOf(
                    getFirstCourse().id.toString() to arrayOf(EnrollmentType.TeacherEnrollment.rawValue)
                )
            ),
            Recipient(
                stringId = getTeachers().last().id.toString(),
                name = getTeachers().last().name,
                commonCourses = hashMapOf(
                    getFirstCourse().id.toString() to arrayOf(EnrollmentType.TeacherEnrollment.rawValue)
                )
            )
        )
        goToInboxCompose(data)
        composeTestRule.waitForIdle()

        selectContext()
        inboxComposePage.pressAddRecipient()
        recipientPickerPage.pressLabel("Teachers")
        recipientPickerPage.pressLabel(getTeachers().first().name)
        recipientPickerPage.pressLabel(getTeachers().last().name)
        recipientPickerPage.pressDone()
        inboxComposePage.typeSubject("Test Subject")
        inboxComposePage.typeBody("Test Body")
        inboxComposePage.pressSendButton()

        composeTestRule.waitForIdle()

        inboxPage.filterInbox("Sent")
        inboxPage.assertConversationDisplayed("Test Subject")
    }

    @Test
    fun sendMessageToAllInCourse() {
        val data = initData(canSendToAll = true)
        data.addSentConversation("Test Subject", getLoggedInUser().id, messageBody = "Test Body")
        data.recipientGroups[getFirstCourse().id] = listOf(
            Recipient(
                stringId = getTeachers().first().id.toString(),
                name = getTeachers().first().name,
                commonCourses = hashMapOf(
                    getFirstCourse().id.toString() to arrayOf(EnrollmentType.TeacherEnrollment.rawValue)
                )
            ),
            Recipient(
                stringId = getTeachers().last().id.toString(),
                name = getTeachers().last().name,
                commonCourses = hashMapOf(
                    getFirstCourse().id.toString() to arrayOf(EnrollmentType.TeacherEnrollment.rawValue)
                )
            )
        )
        goToInboxCompose(data)
        composeTestRule.waitForIdle()

        selectContext()
        inboxComposePage.pressAddRecipient()
        recipientPickerPage.pressLabel("All in ${data.courses.values.first().name}")
        recipientPickerPage.pressDone()
        inboxComposePage.typeSubject("Test Subject")
        inboxComposePage.typeBody("Test Body")
        inboxComposePage.pressSendButton()

        composeTestRule.waitForIdle()

        inboxPage.filterInbox("Sent")
        inboxPage.assertConversationDisplayed("Test Subject")
    }

    @Test
    fun sendMessageToAllInRole() {
        val data = initData(canSendToAll = true)
        data.addSentConversation("Test Subject", getLoggedInUser().id, messageBody = "Test Body")
        data.recipientGroups[getFirstCourse().id] = listOf(
            Recipient(
                stringId = getTeachers().first().id.toString(),
                name = getTeachers().first().name,
                commonCourses = hashMapOf(
                    getFirstCourse().id.toString() to arrayOf(EnrollmentType.TeacherEnrollment.rawValue)
                )
            ),
            Recipient(
                stringId = getTeachers().last().id.toString(),
                name = getTeachers().last().name,
                commonCourses = hashMapOf(
                    getFirstCourse().id.toString() to arrayOf(EnrollmentType.TeacherEnrollment.rawValue)
                )
            )
        )
        goToInboxCompose(data)
        composeTestRule.waitForIdle()

        selectContext()
        inboxComposePage.pressAddRecipient()
        recipientPickerPage.pressLabel("Teachers")
        recipientPickerPage.pressLabel("All in Teachers")
        recipientPickerPage.pressDone()
        inboxComposePage.typeSubject("Test Subject")
        inboxComposePage.typeBody("Test Body")
        inboxComposePage.pressSendButton()

        composeTestRule.waitForIdle()

        inboxPage.filterInbox("Sent")
        inboxPage.assertConversationDisplayed("Test Subject")
    }

    @Test
    fun sendMessageWithAttachment() {
        val data = initData()
        data.addSentConversation("Test Subject", getLoggedInUser().id, messageBody = "Test Body")
        data.recipientGroups[getFirstCourse().id] = listOf(
            Recipient(
                stringId = getTeachers().first().id.toString(),
                name = getTeachers().first().name,
                commonCourses = hashMapOf(
                    getFirstCourse().id.toString() to arrayOf(EnrollmentType.TeacherEnrollment.rawValue)
                )
            )
        )
        goToInboxCompose(data)
        composeTestRule.waitForIdle()

        selectContext()
        inboxComposePage.pressAddRecipient()
        recipientPickerPage.pressLabel("Teachers")
        recipientPickerPage.pressLabel(getTeachers().first().name)
        recipientPickerPage.pressDone()
        inboxComposePage.typeSubject("Test Subject")
        inboxComposePage.typeBody("Test Body")
        inboxComposePage.pressSendButton()
        val attachmentName = "attachment.html"
        data.sentConversation?.let {
            addAttachmentToConversation(
                attachmentName,
                it,
                data
            )
        }
        composeTestRule.waitForIdle()

        inboxPage.filterInbox("Sent")
        inboxPage.assertConversationDisplayed("Test Subject")
    }

    @Test
    fun assertContextSelection() {
        val data = initData()
        goToInboxCompose(data)
        composeTestRule.waitForIdle()

        selectContext()

        inboxComposePage.assertContextSelected(getFirstCourse().name)
    }

    @Test
    fun assertRecipientSelection() {
        val data = initData()
        data.recipientGroups[getFirstCourse().id] = listOf(
            Recipient(
                stringId = getTeachers().first().id.toString(),
                name = getTeachers().first().name,
                commonCourses = hashMapOf(
                    getFirstCourse().id.toString() to arrayOf(EnrollmentType.TeacherEnrollment.rawValue)
                )
            )
        )
        goToInboxCompose(data)
        composeTestRule.waitForIdle()

        selectContext()
        inboxComposePage.pressAddRecipient()
        recipientPickerPage.pressLabel("Teachers")
        recipientPickerPage.pressLabel(getTeachers().first().name)
        recipientPickerPage.pressDone()

        inboxComposePage.assertRecipientSelected(getTeachers().first().name)
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
    fun assertAlertDialogNotPopsOnExitWithoutModification() {
        val data = initData()
        goToInboxCompose(data)
        composeTestRule.waitForIdle()

        inboxComposePage.clickOnCloseButton()
        inboxPage.assertInboxEmpty()
    }

    @Test
    fun assertAlertDialogPopsOnExitWithModification() {
        val data = initData()
        goToInboxCompose(data)
        composeTestRule.waitForIdle()

        inboxComposePage.typeBody("Test Body")

        inboxComposePage.clickOnCloseButton()
        inboxComposePage.assertAlertDialog()
    }

    @Test
    fun assertInboxSignatureIsPopulated() {
        val data = initData()
        data.inboxSignature = "Test Signature"
        goToInboxCompose(data)
        composeTestRule.waitForIdle()

        inboxComposePage.assertBodyText("\n\n---\nTest Signature")
    }

    private fun addAttachmentToConversation(attachmentName: String, conversation: Conversation, mockCanvas: MockCanvas) {
        val attachment = createHtmlAttachment(attachmentName, mockCanvas)
        val newMessageList = listOf(conversation.messages.first().copy(attachments = listOf(attachment)))
        mockCanvas.conversations[conversation.id] = conversation.copy(messages = newMessageList)
    }

    private fun createHtmlAttachment(displayName: String, mockCanvas: MockCanvas): Attachment {
        val attachmentHtml =
            """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        </head>

        <body>
        <h1 id="header1">Famous Quote</h1>
        <p id="p1">That's one small step for man, one giant leap for mankind -- Neil Armstrong</p>
        </body>
        </html> """

        return Attachment(
            id = mockCanvas.newItemId(),
            contentType = "html",
            filename = "mockhtmlfile.html",
            displayName = displayName,
            size = attachmentHtml.length.toLong()
        )
    }

    override fun displaysPageObjects() = Unit

    abstract fun initData(canSendToAll: Boolean = false, sendMessages: Boolean = true): MockCanvas

    abstract fun goToInboxCompose(data: MockCanvas)

    abstract fun getLoggedInUser(): User

    abstract fun getTeachers(): List<User>

    abstract fun getFirstCourse(): Course

    abstract fun getSentConversation(): Conversation?

    open fun selectContext() {
        inboxComposePage.pressCourseSelector()
        selectContextPage.selectContext(getFirstCourse().name)
    }
}