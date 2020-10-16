package com.instructure.teacher.features.syllabus

import android.content.Context
import com.instructure.teacher.features.syllabus.ui.SyllabusViewState
import com.instructure.teacher.mobius.common.ui.Presenter

object SyllabusPresenter : Presenter<SyllabusModel, SyllabusViewState> {

    override fun present(model: SyllabusModel, context: Context): SyllabusViewState {
        return SyllabusViewState("Yeeee")
    }
}