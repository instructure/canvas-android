package com.instructure.pandautils.compose.features.inbox.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.compose.composables.SelectContextScreen
import com.instructure.pandautils.compose.composables.SelectContextUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContextPickerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val title = "Select a course or a group"

    @Test
    fun testContextPickerCourses() {
        setTestScreen(getUiState())

        composeTestRule.onNode(hasText("Courses")).assertIsDisplayed()

        composeTestRule.onNodeWithTag("calendar_Black Holes").assertIsDisplayed()
        composeTestRule.onNodeWithTag("calendar_Cosmology").assertIsDisplayed()
        composeTestRule.onNodeWithTag("calendar_Life in the Universe").assertIsDisplayed()

        composeTestRule.onNode(hasText("Favorite Courses"))
            .assertIsNotDisplayed()
        composeTestRule.onNode(hasText("More Courses"))
            .assertIsNotDisplayed()
    }

    @Test
    fun testContextPickerWithFavorites() {
        val uiState = getUiState()
        setTestScreen(
            uiState.copy(
                canvasContexts = uiState.canvasContexts + Course(
                    id = 4,
                    name = "Biology",
                    isFavorite = true
                )
            )
        )

        composeTestRule.onNode(hasText("Favorite Courses")).assertIsDisplayed()
        composeTestRule.onNode(hasText("More Courses")).assertIsDisplayed()

        composeTestRule.onNodeWithTag("calendar_Black Holes").assertIsDisplayed()
        composeTestRule.onNodeWithTag("calendar_Cosmology").assertIsDisplayed()
        composeTestRule.onNodeWithTag("calendar_Life in the Universe").assertIsDisplayed()
        composeTestRule.onNodeWithTag("calendar_Biology").assertIsDisplayed()
    }

    @Test
    fun testContextPickerWithOnlyFavorites() {
        setTestScreen(
            getUiState().copy(
                canvasContexts = listOf(
                    Course(
                        id = 4,
                        name = "Biology",
                        isFavorite = true
                    )
                )
            )
        )

        composeTestRule.onNode(hasText("Courses")).assertIsDisplayed()

        composeTestRule.onNodeWithTag("calendar_Biology").assertIsDisplayed()

        composeTestRule.onNode(hasText("Favorite Courses"))
            .assertIsNotDisplayed()
        composeTestRule.onNode(hasText("More Courses"))
            .assertIsNotDisplayed()
    }

    @Test
    fun testContextPickerWithGroups() {
        val uiState = getUiState()
        setTestScreen(
            uiState.copy(
                canvasContexts = uiState.canvasContexts + Group(
                    id = 4,
                    name = "Group1"
                )
            )
        )

        composeTestRule.onNode(hasText("Courses")).assertIsDisplayed()
        composeTestRule.onNodeWithTag("calendar_Black Holes").assertIsDisplayed()

        composeTestRule.onNode(hasText("Groups")).assertIsDisplayed()
        composeTestRule.onNodeWithTag("calendar_Group1").assertIsDisplayed()
    }

    private fun setTestScreen(uiState: SelectContextUiState = getUiState()) {
        composeTestRule.setContent {
            SelectContextScreen(
                title = title,
                uiState = uiState,
                onContextSelected = {},
                navigationActionClick = {}
            )
        }
    }

    private fun getUiState(
    ): SelectContextUiState {
        return SelectContextUiState(
            show = true,
            selectedCanvasContext = Course(id = 2),
            canvasContexts = listOf(
                Course(id = 1, name = "Black Holes"),
                Course(id = 2, name = "Cosmology"),
                Course(id = 3, name = "Life in the Universe"),
            )
        )
    }
}