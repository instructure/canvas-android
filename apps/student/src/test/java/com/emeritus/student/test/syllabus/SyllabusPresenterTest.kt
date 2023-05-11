/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.emeritus.student.test.syllabus

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LockInfo
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.utils.backgroundColor
import com.emeritus.student.R
import com.emeritus.student.mobius.syllabus.SyllabusModel
import com.emeritus.student.mobius.syllabus.SyllabusPresenter
import com.emeritus.student.mobius.syllabus.ui.EventsViewState
import com.emeritus.student.mobius.syllabus.ui.ScheduleItemViewState
import com.emeritus.student.mobius.syllabus.ui.SyllabusViewState
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

@RunWith(AndroidJUnit4::class)
class SyllabusPresenterTest : Assert() {

    private lateinit var context: Context
    private lateinit var baseModel: SyllabusModel
    private lateinit var baseCourse: Course

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
    private lateinit var baseEventsViewState: List<ScheduleItemViewState>

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        baseCourse = Course(id = 123L)
        baseModel = SyllabusModel(baseCourse.id)
        baseEventsViewState = listOf(
            ScheduleItemViewState(
                id = "0",
                title = "",
                date = "Apr 2 at 1:59 PM",
                iconRes = R.drawable.ic_calendar,
                color = baseCourse.backgroundColor
            ),
            ScheduleItemViewState(
                id = "1",
                title = "assignment",
                date = "Due Apr 3 at 1:59 pm",
                iconRes = R.drawable.ic_assignment,
                color = baseCourse.backgroundColor
            ),
            ScheduleItemViewState(
                id = "2",
                title = "quiz",
                date = "Due Apr 4 at 2:00 am",
                iconRes = R.drawable.ic_quiz,
                color = baseCourse.backgroundColor
            ),
            ScheduleItemViewState(
                id = "3",
                title = "discussion",
                date = "No Due Date",
                iconRes = R.drawable.ic_discussion,
                color = baseCourse.backgroundColor
            )
        )
    }

    @Test
    fun `Returns Loading state when model is loading`() {
        val expectedState = SyllabusViewState.Loading
        val model = baseModel.copy(isLoading = true)
        val actualState = SyllabusPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns Loaded state with event failure when model result is failed`() {
        val expectedState = SyllabusViewState.Loaded(eventsState = EventsViewState.Error)
        val model = baseModel.copy(course = DataResult.Fail(), events = DataResult.Fail())
        val actualState = SyllabusPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns Loaded state with event failure when model has no syllabus and failed events`() {
        val expectedState = SyllabusViewState.Loaded(eventsState = EventsViewState.Error)
        val model =
            baseModel.copy(course = DataResult.Success(baseCourse), events = DataResult.Fail())
        val actualState = SyllabusPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns Loaded state with empty events when course model has no syllabus and no events`() {
        val expectedState = SyllabusViewState.Loaded(eventsState = EventsViewState.Empty)
        val model = baseModel.copy(
            course = DataResult.Success(baseCourse),
            events = DataResult.Success(emptyList())
        )
        val actualState = SyllabusPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns Loaded state with events when course model has no syllabus`() {
        val expectedState =
            SyllabusViewState.Loaded(eventsState = EventsViewState.Loaded(baseEventsViewState))
        val model = baseModel.copy(
            course = DataResult.Success(baseCourse),
            events = DataResult.Success(baseEvents)
        )
        val actualState = SyllabusPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns Loaded state with events when course failed`() {
        val expectedState =
            SyllabusViewState.Loaded(eventsState = EventsViewState.Loaded(baseEventsViewState.map {
                it.copy(color = 0)
            }))
        val model =
            baseModel.copy(course = DataResult.Fail(), events = DataResult.Success(baseEvents))
        val actualState = SyllabusPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns Loaded state with syllabus and empty events`() {
        val expectedState = SyllabusViewState.Loaded(syllabus = "syllabus")
        val model = baseModel.copy(
            course = DataResult.Success(baseCourse),
            events = DataResult.Success(emptyList()),
            syllabus = ScheduleItem.createSyllabus(null, "syllabus")
        )
        val actualState = SyllabusPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns Loaded state with events and syllabus`() {
        val expectedState =
            SyllabusViewState.Loaded(syllabus = "syllabus", eventsState = EventsViewState.Loaded(baseEventsViewState))
        val model = baseModel.copy(
            course = DataResult.Success(baseCourse),
            events = DataResult.Success(baseEvents),
            syllabus = ScheduleItem.createSyllabus(null, "syllabus")
        )
        val actualState = SyllabusPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Returns Loaded state with locked events`() {
        val time = Calendar.getInstance().timeInMillis
        val futureDate = Date(time + 100000)
        val pastDate = Date(time - 100000)

        val events = DataResult.Success(
            listOf(
                ScheduleItem(
                    assignment = Assignment(lockInfo = LockInfo(unlockAt = futureDate.toApiString()))
                ),
                ScheduleItem(
                    assignment = Assignment(
                        lockExplanation = "Locked",
                        lockAt = pastDate.toApiString()
                    )
                ),
                ScheduleItem(
                    assignment = Assignment(lockExplanation = "Not locked")
                )
            )
        )
        val eventsViewState = listOf(
            ScheduleItemViewState("", "", "No Due Date", R.drawable.ic_lock_lined, baseCourse.backgroundColor),
            ScheduleItemViewState("", "", "No Due Date", R.drawable.ic_lock_lined, baseCourse.backgroundColor),
            ScheduleItemViewState("", "", "No Due Date", R.drawable.ic_assignment, baseCourse.backgroundColor)
        )

        val expectedState =
            SyllabusViewState.Loaded(eventsState = EventsViewState.Loaded(eventsViewState))
        val model = baseModel.copy(course = DataResult.Success(baseCourse), events = events)
        val actualState = SyllabusPresenter.present(model, context)
        assertEquals(expectedState, actualState)
    }
}
