package com.instructure.teacher.features.syllabus

import android.content.Context
import com.instructure.canvasapi2.utils.isValid
import com.instructure.teacher.features.syllabus.ui.EventsViewState
import com.instructure.teacher.features.syllabus.ui.SyllabusViewState
import com.instructure.teacher.mobius.common.ui.Presenter

object SyllabusPresenter : Presenter<SyllabusModel, SyllabusViewState> {

    override fun present(model: SyllabusModel, context: Context): SyllabusViewState {
        if (model.isLoading) return SyllabusViewState.Loading

        if (model.course?.isFail == true && model.events?.isFail == true) {
            return SyllabusViewState.Loaded(eventsState = EventsViewState.Error)
        }

        val course = model.course?.dataOrNull
//        val events = mapEventsResultToViewState(course?.color ?: 0, model.events, context)
        val body = model.syllabus?.description?.takeIf { it.isValid() }

        return SyllabusViewState.Loaded(
                syllabus = body
        )
    }
}