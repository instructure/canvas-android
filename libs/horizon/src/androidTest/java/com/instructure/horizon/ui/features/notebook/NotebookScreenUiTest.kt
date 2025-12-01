/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.ui.features.notebook

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedData
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataRange
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataTextPosition
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteObjectType
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.notebook.NotebookScreen
import com.instructure.horizon.features.notebook.NotebookUiState
import com.instructure.horizon.features.notebook.common.model.Note
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.horizonui.platform.LoadingState
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NotebookScreenUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun testEmptyStateDisplaysWhenNoNotes() {
        val state = createEmptyState()

        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            NotebookScreen(navController, state)
        }

        composeTestRule.onNodeWithText(context.getString(R.string.notesEmptyContentTitle))
            .assertIsDisplayed()
    }

    @Test
    fun testNoteCardDisplaysHighlightedText() {
        val highlightedText = "This is important highlighted text from the course material"
        val state = createStateWithNotes(
            notes = listOf(createTestNote(highlightedText = highlightedText))
        )

        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            NotebookScreen(navController, state)
        }

        composeTestRule.onNodeWithText(highlightedText, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun testNoteCardDisplaysUserComment() {
        val userComment = "My personal note about this concept"
        val state = createStateWithNotes(
            notes = listOf(createTestNote(userText = userComment))
        )

        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            NotebookScreen(navController, state)
        }

        composeTestRule.onNodeWithText(userComment, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun testNoteCardDisplaysDate() {
        val state = createStateWithNotes(
            notes = listOf(createTestNote())
        )

        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            NotebookScreen(navController, state)
        }

        composeTestRule.onNode(hasText("Jan", substring = true))
            .assertIsDisplayed()
    }

    @Test
    fun testNoteCardDisplaysTypeImportant() {
        val state = createStateWithNotes(
            notes = listOf(createTestNote(type = NotebookType.Important))
        )

        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            NotebookScreen(navController, state)
        }

        composeTestRule.onNodeWithText("Important", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testNoteCardDisplaysTypeConfusing() {
        val state = createStateWithNotes(
            notes = listOf(createTestNote(type = NotebookType.Confusing))
        )

        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            NotebookScreen(navController, state)
        }

        composeTestRule.onNodeWithText("Unclear", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testCourseFilterDisplayedWhenEnabled() {
        val state = createStateWithNotes(
            showCourseFilter = true,
            courses = listOf(createTestCourse())
        )

        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            NotebookScreen(navController, state)
        }

        composeTestRule.onNodeWithText(context.getString(R.string.notebookFilterCoursePlaceholder), useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testNoteTypeFilterDisplayed() {
        val state = createStateWithNotes(
            showNoteTypeFilter = true,
            notes = listOf(createTestNote())
        )

        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            NotebookScreen(navController, state)
        }

        composeTestRule.onNodeWithText("All notes", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testCourseNameDisplayedWhenCourseFilterVisible() {
        val courseName = "Biology 101"
        val state = createStateWithNotes(
            showCourseFilter = true,
            courses = listOf(createTestCourse(name = courseName, id = 123L)),
            notes = listOf(createTestNote(courseId = 123L))
        )

        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            NotebookScreen(navController, state)
        }

        composeTestRule.onNodeWithText(courseName, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun testCourseNameNotDisplayedWhenCourseFilterHidden() {
        val courseName = "Biology 101"
        val state = createStateWithNotes(
            showCourseFilter = false,
            courses = listOf(createTestCourse(name = courseName, id = 123L)),
            notes = listOf(createTestNote(courseId = 123L))
        )

        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            NotebookScreen(navController, state)
        }

        composeTestRule.onNode(hasText(courseName))
            .assertDoesNotExist()
    }

    @Test
    fun testEmptyFilteredStateDisplayedWhenFilterApplied() {
        val state = createEmptyState(selectedFilter = NotebookType.Important)

        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            NotebookScreen(navController, state)
        }

        composeTestRule.onNodeWithText(context.getString(R.string.notesEmptyFilteredContentTitle))
            .assertIsDisplayed()
    }

    @Test
    fun testMultipleNotesDisplayed() {
        val note1 = createTestNote(
            id = "1",
            highlightedText = "First important concept"
        )
        val note2 = createTestNote(
            id = "2",
            highlightedText = "Second important concept"
        )
        val state = createStateWithNotes(notes = listOf(note1, note2))

        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            NotebookScreen(navController, state)
        }

        composeTestRule.onNodeWithText("First important concept", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Second important concept", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun testShowMoreButtonDisplayedWhenHasNextPage() {
        val state = createStateWithNotes(
            notes = listOf(createTestNote()),
            hasNextPage = true
        )

        composeTestRule.setContent {
            ContextKeeper.appContext = context
            val navController = rememberNavController()
            NotebookScreen(navController, state)
        }

        composeTestRule.onNodeWithText(context.getString(R.string.showMore))
            .assertIsDisplayed()
    }

    private fun createEmptyState(
        selectedFilter: NotebookType? = null
    ): NotebookUiState {
        return NotebookUiState(
            loadingState = LoadingState(isLoading = false),
            notes = emptyList(),
            selectedFilter = selectedFilter,
            showCourseFilter = true,
            showNoteTypeFilter = true
        )
    }

    private fun createStateWithNotes(
        notes: List<Note> = emptyList(),
        showCourseFilter: Boolean = false,
        showNoteTypeFilter: Boolean = false,
        courses: List<CourseWithProgress> = emptyList(),
        hasNextPage: Boolean = false,
        selectedFilter: NotebookType? = null
    ): NotebookUiState {
        return NotebookUiState(
            loadingState = LoadingState(isLoading = false),
            notes = notes,
            courses = courses,
            showCourseFilter = showCourseFilter,
            showNoteTypeFilter = showNoteTypeFilter,
            hasNextPage = hasNextPage,
            selectedFilter = selectedFilter
        )
    }

    private fun createTestNote(
        id: String = "note1",
        highlightedText: String = "Test highlighted text from course material",
        userText: String = "My personal annotation",
        type: NotebookType = NotebookType.Important,
        courseId: Long = 123L
    ): Note {
        return Note(
            id = id,
            highlightedText = NoteHighlightedData(
                selectedText = highlightedText,
                range = NoteHighlightedDataRange(0, highlightedText.length, "", ""),
                textPosition = NoteHighlightedDataTextPosition(0, highlightedText.length)
            ),
            type = type,
            userText = userText,
            updatedAt = Date(1706140800000L),
            courseId = courseId,
            objectType = NoteObjectType.Assignment,
            objectId = "assignment123"
        )
    }

    private fun createTestCourse(
        name: String = "Test Course",
        id: Long = 123L
    ): CourseWithProgress {
        return CourseWithProgress(
            courseId = id,
            courseName = name,
            progress = 0.0
        )
    }
}
