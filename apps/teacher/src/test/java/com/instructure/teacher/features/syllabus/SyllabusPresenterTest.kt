package com.instructure.teacher.features.syllabus

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Assignment
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

    private lateinit var underTest: SyllabusPresenter

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
        underTest = SyllabusPresenter()
    }

    @Test
    fun `Should return loading state when the model is loading`() {
        // Given
        val model = baseModel.copy(isLoading = true)

        // When
        val syllabusViewState = underTest.present(model, context)

        // Then
        assertEquals(SyllabusViewState.Loading, syllabusViewState)
    }

    @Test
    fun `Should return loaded state with event error state when both events and course failed to load`() {
        // Given
        val model = baseModel.copy(course = DataResult.Fail(), events = DataResult.Fail())

        // When
        val syllabusViewState = underTest.present(model, context)

        // Then
        val expectedState = SyllabusViewState.Loaded(eventsState = EventsViewState.Error)
        assertEquals(expectedState, syllabusViewState)
    }

    @Test
    fun `Should return loaded state with event failure state when model has no syllabus and events failed to load`() {
        // Given
        val model = baseModel.copy(course = DataResult.Success(baseCourse), events = DataResult.Fail())

        // When
        val syllabusViewState = underTest.present(model, context)

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
        val syllabusViewState = underTest.present(model, context)

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
        val syllabusViewState = underTest.present(model, context)

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
                syllabus = ScheduleItem.createSyllabus("Title", syllabusBody)
        )

        // When
        val syllabusViewState = underTest.present(model, context)

        // Then
        val expectedState = SyllabusViewState.Loaded(syllabus = syllabusBody, eventsState = EventsViewState.Loaded(baseEventsViewState))
        assertEquals(expectedState, syllabusViewState)
    }
}