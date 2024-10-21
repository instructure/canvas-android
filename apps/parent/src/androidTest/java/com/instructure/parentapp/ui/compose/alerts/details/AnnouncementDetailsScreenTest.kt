package com.instructure.parentapp.ui.compose.alerts.details

import android.graphics.Color
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandares.R
import com.instructure.parentapp.features.alerts.details.AnnouncementDetailsScreen
import com.instructure.parentapp.features.alerts.details.AnnouncementDetailsUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.util.Date

@RunWith(AndroidJUnit4::class)
class AnnouncementDetailsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun assertContent() {
        val uiState = AnnouncementDetailsUiState(
            studentColor = 1,
            pageTitle = "Course Name",
            announcementTitle = "Alert Title",
            message = "Alert Message",
            postedDate = Date.from(
                Instant.parse("2024-01-03T00:00:00Z")
            ),
            attachment = Attachment(
                id = 1,
                filename = "attachment_file_name",
                size = 100,
                displayName = "File Name",
                thumbnailUrl = "thumbnail_url"
            )
        )
        composeTestRule.setContent {
            AnnouncementDetailsScreen(
                uiState = uiState,
                navigationActionClick = {},
                actionHandler = {}
            )
        }

        composeTestRule.onNodeWithText("Course Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alert Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("attachment_file_name").assertIsDisplayed()
        val dateString = DateHelper.getDateAtTimeString(
            InstrumentationRegistry.getInstrumentation().targetContext,
            R.string.alertDateTime,
            uiState.postedDate
        )
        dateString?.let {
            composeTestRule.onNodeWithText(it).assertIsDisplayed()
        }
    }

    @Test
    fun assertError() {
        composeTestRule.setContent {
            AnnouncementDetailsScreen(
                uiState = AnnouncementDetailsUiState(
                    isLoading = false,
                    isError = true,
                    isRefreshing = false,
                    studentColor = Color.BLUE,
                ),
                navigationActionClick = {},
                actionHandler = {}
            )
        }


        composeTestRule.onNodeWithText("There was an error loading this announcement")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertHasClickAction().assertIsDisplayed()
    }

    @Test
    fun assertLoading() {
        composeTestRule.setContent {
            AnnouncementDetailsScreen(
                uiState = AnnouncementDetailsUiState(
                    isLoading = true,
                    isError = false,
                    isRefreshing = false,
                    studentColor = Color.BLUE,
                ),
                navigationActionClick = {},
                actionHandler = {}
            )
        }

        composeTestRule.onNodeWithTag("loading").assertIsDisplayed()
    }

    @Test
    fun assertRefreshing() {
        composeTestRule.setContent {
            AnnouncementDetailsScreen(
                uiState = AnnouncementDetailsUiState(
                    isLoading = false,
                    isError = false,
                    isRefreshing = true,
                    studentColor = Color.BLUE,
                ),
                navigationActionClick = {},
                actionHandler = {}
            )
        }

        composeTestRule.onNodeWithTag("pullRefreshIndicator").assertIsDisplayed()
    }
}
