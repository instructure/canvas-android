package com.instructure.pandautils.compose.features.inbox.compose

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.pandautils.features.inbox.compose.RecipientPickerScreenOption
import com.instructure.pandautils.features.inbox.compose.RecipientPickerUiState
import com.instructure.pandautils.features.inbox.compose.ScreenState
import com.instructure.pandautils.features.inbox.compose.composables.RecipientPickerScreen
import com.instructure.pandautils.utils.ScreenState
import com.instructure.pandautils.utils.orDefault
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.EnumMap

@RunWith(AndroidJUnit4::class)
class RecipientPickerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val title = "Select Recipients"

    @Test
    fun testRecipientsRoleScreenTopBar() {
        setTestScreen(getUiState(screenOption = RecipientPickerScreenOption.Roles))

        val toolbar = composeTestRule.onNodeWithTag("toolbar")
        toolbar.assertExists()
        composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasText(title)))
            .assertIsDisplayed()

        val backButton =
            composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasContentDescription("Close recipient picker")))
        backButton
            .assertIsDisplayed()
            .assertHasClickAction()

        val doneButton =
            composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasText("Done")))
        doneButton
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testRecipientsRecipientScreenTopBar() {
        setTestScreen(getUiState(screenOption = RecipientPickerScreenOption.Recipients, selectedRole = EnrollmentType.StudentEnrollment))

        val toolbar = composeTestRule.onNodeWithTag("toolbar")
        toolbar.assertExists()
        composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasText(title)))
            .assertIsDisplayed()

        val backButton =
            composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasContentDescription("Back to roles")))
        backButton
            .assertIsDisplayed()
            .assertHasClickAction()

        val doneButton =
            composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasText("Done")))
        doneButton
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testRecipientsRolesScreen() {
        setTestScreen(getUiState(screenOption = RecipientPickerScreenOption.Roles))

        composeTestRule.onNode(hasText("Students").and(hasText("2 People")))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("Teachers").and(hasText("2 People")))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("TAs").and(hasText("1 Person")))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("Search in All Recipients"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("All"))
            .assertIsNotDisplayed()
    }

    @Test
    fun testRecipientsRecipientScreen() {
        setTestScreen(getUiState(
            screenOption = RecipientPickerScreenOption.Recipients,
            selectedRole = EnrollmentType.StudentEnrollment
        ))

        composeTestRule.onNode(hasText("Student 1"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("Student 2"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("Search in Students"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("All in Selected Role"))
            .assertIsNotDisplayed()
    }

    @Test
    fun testRecipientsRoleScreenAllOption() {
        setTestScreen(getUiState(
            screenOption = RecipientPickerScreenOption.Roles,
            canSendToAll = true
        ))

        composeTestRule.onNode(hasText("All"))
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testRecipientsRecipientScreenAllOption() {
        setTestScreen(getUiState(
            screenState = ScreenState.Content,
            screenOption = RecipientPickerScreenOption.Recipients,
            selectedRole = EnrollmentType.StudentEnrollment,
            canSendToAll = true
        ))

        composeTestRule.onNode(hasText("All in Selected Role"))
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testRecipientsRoleScreenSearch() {
        setTestScreen(getUiState(
            screenState = ScreenState.Content,
            screenOption = RecipientPickerScreenOption.Roles,
            searchValue = TextFieldValue("Student")
        ))

        composeTestRule.onNode(hasText("Student 1"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("Student 2"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("Students").and(hasText("2 People")))
            .assertIsNotDisplayed()

        composeTestRule.onNode(hasText("Teachers").and(hasText("2 People")))
            .assertIsNotDisplayed()

        composeTestRule.onNode(hasText("TAs").and(hasText("1 Person")))
            .assertIsNotDisplayed()

        composeTestRule.onNode(hasText("Search"))
            .assertIsNotDisplayed()

        composeTestRule.onNode(hasText("All"))
            .assertIsNotDisplayed()
    }

    @Test
    fun testRecipientsRecipientScreenSearch() {
        setTestScreen(getUiState(
            screenState = ScreenState.Content,
            screenOption = RecipientPickerScreenOption.Recipients,
            selectedRole = EnrollmentType.StudentEnrollment,
            searchValue = TextFieldValue("Teacher")
        ))

        composeTestRule.onNode(hasText("Teacher 1"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("Teacher 2"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("Student 1"))
            .assertIsNotDisplayed()

        composeTestRule.onNode(hasText("Student 2"))
            .assertIsNotDisplayed()

        composeTestRule.onNode(hasText("All in Selected Role"))
            .assertIsNotDisplayed()
    }

    @Test
    fun testSelectedRecipients() {
        setTestScreen(
            getUiState(
                screenState = ScreenState.Content,
                screenOption = RecipientPickerScreenOption.Recipients,
                selectedRole = EnrollmentType.StudentEnrollment,
                selectedRecipients = listOf(Recipient(stringId = "1", name = "Student 1"))
            )
        )

        composeTestRule.onNode(hasText("Student 1"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("Student 1").and(hasContentDescription("Selected")))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("Student 2"))
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNode(hasText("Student 2").and(hasContentDescription("Selected")))
            .assertIsNotDisplayed()

    }

    private fun setTestScreen(uiState: RecipientPickerUiState = getUiState()) {
        composeTestRule.setContent {
            RecipientPickerScreen(
                title = title,
                uiState = uiState,
                actionHandler = {},
            )
        }
    }

    private fun getUiState(
        screenOption: RecipientPickerScreenOption = RecipientPickerScreenOption.Roles,
        screenState: ScreenState = ScreenState.Content,
        canSendToAll: Boolean = false,
        selectedRole: EnrollmentType? = null,
        selectedRecipients: List<Recipient> = emptyList(),
        searchValue: TextFieldValue = TextFieldValue(""),
    ): RecipientPickerUiState {
        val recipientsByRoles: EnumMap<EnrollmentType, List<Recipient>> = EnumMap(EnrollmentType::class.java)
        recipientsByRoles.putAll(mapOf(
            EnrollmentType.StudentEnrollment to listOf(Recipient(stringId = "1", name = "Student 1"), Recipient(stringId = "2", name = "Student 2")),
            EnrollmentType.TeacherEnrollment to listOf(Recipient(stringId = "3", name = "Teacher 1"), Recipient(stringId = "4", name = "Teacher 2")),
            EnrollmentType.TaEnrollment to listOf(Recipient(stringId = "5", name = "Ta 1")),
        ))
        val allRecipientsToShow = if (canSendToAll) {
            if (selectedRole != null) {
                Recipient(stringId = "All in Selected Role", name = "All in Selected Role")
            } else {
                Recipient(stringId = "All", name = "All")
            }
        } else {
            null
        }
        val recipientsToShow: List<Recipient> = if (selectedRole == null) {
            if (searchValue.text.isEmpty()) {
                emptyList()
            } else {
                recipientsByRoles.values.flatten().filter { it.name?.contains(searchValue.text, ignoreCase = true).orDefault() }
            }
        } else {
            if (searchValue.text.isEmpty()) {
                recipientsByRoles[selectedRole] ?: emptyList()
            } else {
                recipientsByRoles.values.flatten().filter { it.name?.contains(searchValue.text, ignoreCase = true).orDefault() }
            }
        }
        return RecipientPickerUiState(
            recipientsByRole = recipientsByRoles,
            selectedRole = selectedRole,
            recipientsToShow = recipientsToShow,
            allRecipientsToShow = allRecipientsToShow,
            selectedRecipients = selectedRecipients,
            searchValue = searchValue,
            screenOption = screenOption,
            screenState = screenState,
        )
    }
}