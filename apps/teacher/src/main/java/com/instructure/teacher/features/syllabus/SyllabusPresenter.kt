package com.instructure.teacher.features.syllabus

import android.content.Context
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.isValid
import com.instructure.pandares.R
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.getShortMonthAndDay
import com.instructure.pandautils.utils.getTime
import com.instructure.teacher.features.syllabus.ui.EventsViewState
import com.instructure.teacher.features.syllabus.ui.ScheduleItemViewState
import com.instructure.teacher.features.syllabus.ui.SyllabusViewState
import com.instructure.teacher.mobius.common.ui.Presenter
import org.threeten.bp.OffsetDateTime
import java.util.*

object SyllabusPresenter : Presenter<SyllabusModel, SyllabusViewState> {

    override fun present(model: SyllabusModel, context: Context): SyllabusViewState {
        return when {
            model.isLoading -> SyllabusViewState.Loading
            loadingFailed(model) -> SyllabusViewState.Loaded(eventsState = EventsViewState.Error)
            else -> createSuccessLoadedState(model, context)
        }
    }

    private fun loadingFailed(model: SyllabusModel) = model.course?.isFail == true && model.events?.isFail == true

    private fun createSuccessLoadedState(model: SyllabusModel, context: Context): SyllabusViewState.Loaded {
        val course = model.course?.dataOrNull
        val events = mapEventsResultToViewState(course?.color ?: 0, model.events, context)
        val body = model.syllabus?.description?.takeIf { it.isValid() }

        return SyllabusViewState.Loaded(
                syllabus = body,
                eventsState = events.takeUnless { it == EventsViewState.Empty && body != null })
    }

    private fun mapEventsResultToViewState(color: Int, eventsResult: DataResult<List<ScheduleItem>>?, context: Context): EventsViewState {
        return when {
            eventsResult == null -> EventsViewState.Error
            eventsResult.isFail -> EventsViewState.Error
            eventsResult.dataOrNull.isNullOrEmpty() -> EventsViewState.Empty
            else -> createLoadedEvents(eventsResult, context, color)
        }
    }

    private fun createLoadedEvents(eventsResult: DataResult<List<ScheduleItem>>, context: Context, color: Int): EventsViewState.Loaded {
        return EventsViewState.Loaded(eventsResult.dataOrThrow.map {
            ScheduleItemViewState(
                    it.itemId,
                    it.title ?: "",
                    getDateString(it, context),
                    getIcon(it),
                    color
            )
        })
    }

    private fun getDateString(event: ScheduleItem, context: Context): String {
        return when {
            event.startAt == null -> context.getString(R.string.toDoNoDueDate)
            event.itemType == ScheduleItem.Type.TYPE_ASSIGNMENT -> event.startAt?.toDueAtString(context) ?: ""
            else -> event.startDate?.let { DateHelper.getMonthDayAtTime(context, it, R.string.at) } ?: ""
        }
    }

    private fun getIcon(event: ScheduleItem): Int {
        return when {
            eventIsLocked(event) -> R.drawable.ic_lock_lined
            event.assignment != null -> getAssignmentIcon(event.assignment!!)
            else -> R.drawable.ic_calendar
        }
    }

    private fun eventIsLocked(event: ScheduleItem): Boolean {
        return event.assignment?.isLocked == true || event.assignment?.lockExplanation?.takeIf {
            it.isValid() && event.assignment?.lockDate?.before(Date()) == true
        } != null
    }

    private fun getAssignmentIcon(assignment: Assignment): Int {
        return when {
            assignment.getSubmissionTypes().contains(Assignment.SubmissionType.ONLINE_QUIZ) -> R.drawable.ic_quiz
            assignment.getSubmissionTypes().contains(Assignment.SubmissionType.DISCUSSION_TOPIC) -> R.drawable.ic_discussion
            else -> R.drawable.ic_assignment
        }
    }
}

private fun String.toDueAtString(context: Context): String {
    val dueDateTime = OffsetDateTime.parse(this).withOffsetSameInstant(OffsetDateTime.now().offset)
    return context.getString(R.string.submissionDetailsDueAt, dueDateTime.getShortMonthAndDay(), dueDateTime.getTime())
}