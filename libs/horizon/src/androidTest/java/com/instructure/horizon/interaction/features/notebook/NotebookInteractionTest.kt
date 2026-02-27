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
package com.instructure.horizon.interaction.features.notebook

import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeGetHorizonCourseManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeGetLearningLibraryManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeGetProgramsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeGetSkillsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeGetWidgetsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeRedwoodApiManager
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.di.graphql.GetCoursesModule
import com.instructure.canvasapi2.di.graphql.JourneyModule
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetLearningLibraryManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetProgramsManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetSkillsManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetWidgetsManager
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedData
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataRange
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataTextPosition
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.RedwoodApiManager
import com.instructure.horizon.espresso.HorizonTest
import com.instructure.horizon.pages.HorizonNotebookPage
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@HiltAndroidTest
@UninstallModules(GetCoursesModule::class, JourneyModule::class)
class NotebookInteractionTest : HorizonTest() {
    private val fakeGetHorizonCourseManager = FakeGetHorizonCourseManager()
    private val fakeGetProgramsManager = FakeGetProgramsManager()
    private val fakeGetWidgetsManager = FakeGetWidgetsManager()
    private val fakeGetSkillsManager = FakeGetSkillsManager()
    private val fakeRedwoodApiManager = FakeRedwoodApiManager()
    private val fakeLearningLibraryManager = FakeGetLearningLibraryManager()

    @BindValue
    @JvmField
    val getProgramsManager: GetProgramsManager = fakeGetProgramsManager

    @BindValue
    @JvmField
    val getWidgetsManager: GetWidgetsManager = fakeGetWidgetsManager

    @BindValue
    @JvmField
    val getSkillsManager: GetSkillsManager = fakeGetSkillsManager

    @BindValue
    @JvmField
    val getCoursesManager: HorizonGetCoursesManager = fakeGetHorizonCourseManager

    @BindValue
    @JvmField
    val redwoodApiManager: RedwoodApiManager = fakeRedwoodApiManager

    @BindValue
    @JvmField
    val learningLibraryManager: GetLearningLibraryManager = fakeLearningLibraryManager

    private val notebookPage: HorizonNotebookPage by lazy { HorizonNotebookPage(composeTestRule) }

    @Before
    fun setup() {
        fakeRedwoodApiManager.reset()
    }
    @Test
    fun testNoteIsDisplayed() = runTest {
        val data = setupMockCanvasData()
        val course = data.courses.values.first()

        fakeRedwoodApiManager.createNote(
            courseId = course.id.toString(),
            objectId = "test-object-id",
            objectType = "Page",
            userText = "User note",
            notebookType = "Important",
            highlightData = NoteHighlightedData(
                selectedText = "Original note",
                range = NoteHighlightedDataRange(
                    startOffset = 1,
                    endOffset = 4,
                    startContainer = "t",
                    endContainer = "t"
                ),
                textPosition = NoteHighlightedDataTextPosition(
                    start = 1,
                     end = 4
                )
            )
        )

        dashboardPage.clickNotebookButton()
        composeTestRule.waitForIdle()

        notebookPage.assertNoteDisplayed("Original note", "User note")
    }

    private fun setupMockCanvasData(): MockCanvas {
        val data = MockCanvas.init(
            studentCount = 1,
            teacherCount = 1,
            courseCount = 1
        )
        val student = data.students.first()
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        composeTestRule.waitForIdle()
        return data
    }
}
