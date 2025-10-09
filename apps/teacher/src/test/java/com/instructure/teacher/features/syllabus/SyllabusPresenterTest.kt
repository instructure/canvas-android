/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.teacher.features.syllabus

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.utils.color
import com.instructure.teacher.R
import com.instructure.teacher.features.syllabus.ui.EventsViewState
import com.instructure.teacher.features.syllabus.ui.ScheduleItemViewState
import com.instructure.teacher.features.syllabus.ui.SyllabusViewState
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

@RunWith(AndroidJUnit4::class)
class SyllabusPresenterTest {

    private lateinit var context: Context

    private lateinit var syllabusPresenter: SyllabusPresenter

    private val baseCourse = Course(id = 123L)
    private val baseModel = SyllabusModel(courseId = baseCourse.id, isLoading = false)
    private val baseDate =
            OffsetDateTime.now().withMonth(4).withDayOfMonth(2).withHour(13).withMinute(59)

    private val baseEvents = listOf(
            ScheduleItem(
                    itemId = "0",
                    title = null,
                    assignment = null,
                    startAt = DateTimeUtils.toDate(baseDate.atZoneSimilarLocal(ZoneId.systemDefault()).toInstant()).toApiString(),
                    itemType = ScheduleItem.Type.TYPE_CALENDAR
            ),
            ScheduleItem(
                    itemId = "1",
                    title = "assignment",
                    assignment = Assignment(id = 123L),
                    startAt = baseDate.plusDays(1).format(DateTimeFormatter.ISO_DATE_TIME),
                    itemType = ScheduleItem.Type.TYPE_ASSIGNMENT
            ),
            ScheduleItem(
                    itemId = "2",
                    title = "quiz",
                    assignment = Assignment(id = 124L, submissionTypesRaw = listOf("online_quiz")),
                    startAt = baseDate.plusDays(2).withHour(2).withMinute(0).format(DateTimeFormatter.ISO_DATE_TIME),
                    itemType = ScheduleItem.Type.TYPE_ASSIGNMENT
            ),
            ScheduleItem(
                    itemId = "3",
                    title = "discussion",
                    assignment = Assignment(id = 125L, submissionTypesRaw = listOf("discussion_topic")),
                    startAt = null,
                    itemType = ScheduleItem.Type.TYPE_ASSIGNMENT
            ),
            ScheduleItem(
                    itemId = "4",
                    title = "discussion",
                    assignment = Assignment(id = 126L, submissionTypesRaw = listOf("discussion_topic")),
                    startAt = null,
                    itemType = ScheduleItem.Type.TYPE_ASSIGNMENT,
                    isHidden = true
            )
    )

    private val baseEventsViewState = listOf(
            ScheduleItemViewState(
                    id = "0",
                    title = "",
                    date = "Apr 2 at 1:59 PM",
                    iconRes = R.drawable.ic_calendar,
                    color = baseCourse.color
            ),
            ScheduleItemViewState(
                    id = "1",
                    title = "assignment",
                    date = "Due Apr 3 at 1:59 pm",
                    iconRes = R.drawable.ic_assignment,
                    color = baseCourse.color
            ),
            ScheduleItemViewState(
                    id = "2",
                    title = "quiz",
                    date = "Due Apr 4 at 2:00 am",
                    iconRes = R.drawable.ic_quiz,
                    color = baseCourse.color
            ),
            ScheduleItemViewState(
                    id = "3",
                    title = "discussion",
                    date = "No Due Date",
                    iconRes = R.drawable.ic_discussion,
                    color = baseCourse.color
            )
    )

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        syllabusPresenter = SyllabusPresenter()
    }

    @Test
    fun `Should return loading state when the model is loading`() {
        // Given
        val model = baseModel.copy(isLoading = true)

        // When
        val syllabusViewState = syllabusPresenter.present(model, context)

        // Then
        assertEquals(SyllabusViewState.Loading, syllabusViewState)
    }

    @Test
    fun `Should return loaded state with event error state when both events and course failed to load`() {
        // Given
        val model = baseModel.copy(course = DataResult.Fail(), events = DataResult.Fail())

        // When
        val syllabusViewState = syllabusPresenter.present(model, context)

        // Then
        val expectedState = SyllabusViewState.Loaded(eventsState = EventsViewState.Error)
        assertEquals(expectedState, syllabusViewState)
    }

    @Test
    fun `Should return loaded state with event failure state when model has no syllabus and events failed to load`() {
        // Given
        val model = baseModel.copy(course = DataResult.Success(baseCourse), events = DataResult.Fail())

        // When
        val syllabusViewState = syllabusPresenter.present(model, context)

        // Then
        val expectedState = SyllabusViewState.Loaded(eventsState = EventsViewState.Error)
        assertEquals(expectedState, syllabusViewState)
    }

    @Test
    fun `Should return loaded state with only syllabus when model has syllabus and events are empty`() {
        // Given
        val syllabusBody = "Syllabus body"
        val model = baseModel.copy(
                course = DataResult.Success(baseCourse),
                events = DataResult.Success(emptyList()),
                syllabus = ScheduleItem.createSyllabus("Title", syllabusBody)
        )

        // When
        val syllabusViewState = syllabusPresenter.present(model, context)

        // Then
        val expectedState = SyllabusViewState.Loaded(syllabus = syllabusBody)
        assertEquals(expectedState, syllabusViewState)
    }

    @Test
    fun `Should return loaded state with only empty events when model has no syllabus and has no events`() {
        // Given
        val model = baseModel.copy(
                course = DataResult.Success(baseCourse),
                events = DataResult.Success(emptyList()),
        )

        // When
        val syllabusViewState = syllabusPresenter.present(model, context)

        // Then
        val expectedState = SyllabusViewState.Loaded(eventsState = EventsViewState.Empty)
        assertEquals(expectedState, syllabusViewState)
    }

    @Test
    fun `Should return loaded state with events and syllabus when model has syllabus and events`() {
        // Given
        val syllabusBody = "Syllabus body"
        val model = baseModel.copy(
            course = DataResult.Success(baseCourse),
            events = DataResult.Success(baseEvents),
            syllabus = ScheduleItem.createSyllabus("Title", syllabusBody),
            summaryAllowed = true
        )

        // When
        val syllabusViewState = syllabusPresenter.present(model, context)

        // Then
        val expectedState = SyllabusViewState.Loaded(syllabus = syllabusBody, eventsState = EventsViewState.Loaded(baseEventsViewState), showSummary = true)
        assertEquals(expectedState, syllabusViewState)
    }

    @Test
    fun `Should return loaded state with possibility to edit if user has the permission to edit`() {
        // Given
        val syllabusBody = "Syllabus body"
        val model = baseModel.copy(
            course = DataResult.Success(baseCourse),
            events = DataResult.Success(baseEvents),
            syllabus = ScheduleItem.createSyllabus("Title", syllabusBody),
            permissions = DataResult.Success(CanvasContextPermission(canManageContent = true))
        )

        // When
        val syllabusViewState = syllabusPresenter.present(model, context)

        // Then
        val expectedState = SyllabusViewState.Loaded(syllabus = syllabusBody, eventsState = EventsViewState.Loaded(baseEventsViewState), canEdit = true)
        assertEquals(expectedState, syllabusViewState)
    }

    @Test
    fun `Should return loaded state without possibility to edit if user does not have the permission to edit`() {
        // Given
        val syllabusBody = "Syllabus body"
        val model = baseModel.copy(
            course = DataResult.Success(baseCourse),
            events = DataResult.Success(baseEvents),
            syllabus = ScheduleItem.createSyllabus("Title", syllabusBody),
            permissions = DataResult.Success(CanvasContextPermission(canManageContent = false))
        )

        // When
        val syllabusViewState = syllabusPresenter.present(model, context)

        // Then
        val expectedState = SyllabusViewState.Loaded(syllabus = syllabusBody, eventsState = EventsViewState.Loaded(baseEventsViewState), canEdit = false)
        assertEquals(expectedState, syllabusViewState)
    }

    @Test
    fun `Should return loaded state without possibility to edit if permissions fail`() {
        // Given
        val syllabusBody = "Syllabus body"
        val model = baseModel.copy(
            course = DataResult.Success(baseCourse),
            events = DataResult.Success(baseEvents),
            syllabus = ScheduleItem.createSyllabus("Title", syllabusBody),
            permissions = DataResult.Fail()
        )

        // When
        val syllabusViewState = syllabusPresenter.present(model, context)

        // Then
        val expectedState = SyllabusViewState.Loaded(syllabus = syllabusBody, eventsState = EventsViewState.Loaded(baseEventsViewState), canEdit = false)
        assertEquals(expectedState, syllabusViewState)
    }

    @Test
    fun `Should return correct icons for planner item types`() {
        // Given
        val events = DataResult.Success(
            listOf(
                ScheduleItem(
                    itemId = "1",
                    title = "Quiz",
                    type = "quiz",
                    itemType = ScheduleItem.Type.TYPE_SYLLABUS
                ),
                ScheduleItem(
                    itemId = "2",
                    title = "Discussion",
                    type = "discussion_topic",
                    itemType = ScheduleItem.Type.TYPE_SYLLABUS
                ),
                ScheduleItem(
                    itemId = "3",
                    title = "Announcement",
                    type = "announcement",
                    itemType = ScheduleItem.Type.TYPE_SYLLABUS
                ),
                ScheduleItem(
                    itemId = "4",
                    title = "Wiki Page",
                    type = "wiki_page",
                    itemType = ScheduleItem.Type.TYPE_SYLLABUS
                ),
                ScheduleItem(
                    itemId = "5",
                    title = "Planner Note",
                    type = "planner_note",
                    itemType = ScheduleItem.Type.TYPE_SYLLABUS
                ),
                ScheduleItem(
                    itemId = "6",
                    title = "Calendar Event",
                    type = "calendar_event",
                    itemType = ScheduleItem.Type.TYPE_SYLLABUS
                )
            )
        )
        val eventsViewState = listOf(
            ScheduleItemViewState("1", "Quiz", "No Due Date", R.drawable.ic_quiz, baseCourse.color),
            ScheduleItemViewState("2", "Discussion", "No Due Date", R.drawable.ic_discussion, baseCourse.color),
            ScheduleItemViewState("3", "Announcement", "No Due Date", R.drawable.ic_announcement, baseCourse.color),
            ScheduleItemViewState("4", "Wiki Page", "No Due Date", com.instructure.pandares.R.drawable.ic_document, baseCourse.color),
            ScheduleItemViewState("5", "Planner Note", "No Due Date", com.instructure.pandares.R.drawable.ic_info, baseCourse.color),
            ScheduleItemViewState("6", "Calendar Event", "No Due Date", R.drawable.ic_calendar, baseCourse.color)
        )

        val model = baseModel.copy(
            course = DataResult.Success(baseCourse),
            events = events
        )

        // When
        val syllabusViewState = syllabusPresenter.present(model, context)

        // Then
        val expectedState = SyllabusViewState.Loaded(eventsState = EventsViewState.Loaded(eventsViewState))
        assertEquals(expectedState, syllabusViewState)
    }
}