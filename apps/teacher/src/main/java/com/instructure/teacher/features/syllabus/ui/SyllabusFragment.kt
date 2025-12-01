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
package com.instructure.teacher.features.syllabus.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.pandautils.analytics.SCREEN_VIEW_SYLLABUS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.teacher.databinding.FragmentSyllabusBinding
import com.instructure.teacher.features.syllabus.SyllabusEffect
import com.instructure.teacher.features.syllabus.SyllabusEffectHandler
import com.instructure.teacher.features.syllabus.SyllabusEvent
import com.instructure.teacher.features.syllabus.SyllabusModel
import com.instructure.teacher.features.syllabus.SyllabusPresenter
import com.instructure.teacher.features.syllabus.SyllabusRepository
import com.instructure.teacher.features.syllabus.SyllabusUpdate
import com.instructure.teacher.mobius.common.ui.MobiusFragment

@PageView("{canvasContext}/assignments/syllabus")
@ScreenView(SCREEN_VIEW_SYLLABUS)
abstract class SyllabusFragment : MobiusFragment<SyllabusModel, SyllabusEvent, SyllabusEffect, SyllabusView, SyllabusViewState, FragmentSyllabusBinding>() {

    @get:PageViewUrlParam("canvasContext")
    val canvasContext by ParcelableArg<Course>(key = Const.CANVAS_CONTEXT)

    protected abstract fun getRepository(): SyllabusRepository

    override fun makeEffectHandler() = SyllabusEffectHandler(getRepository())

    override fun makeUpdate() = SyllabusUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) = SyllabusView(canvasContext, inflater, parent)

    override fun makePresenter() = SyllabusPresenter()

    override fun makeInitModel() = SyllabusModel(canvasContext.id)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
    }

    override fun onStart() {
        super.onStart()
        view.registerEventBus()
    }

    override fun onStop() {
        view.unregisterEventBus()
        super.onStop()
    }
}