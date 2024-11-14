/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.mobius.syllabus.ui

import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.student.databinding.FragmentSyllabusBinding
import com.instructure.student.mobius.common.ui.MobiusFragment
import com.instructure.student.mobius.syllabus.SyllabusEffect
import com.instructure.student.mobius.syllabus.SyllabusEffectHandler
import com.instructure.student.mobius.syllabus.SyllabusEvent
import com.instructure.student.mobius.syllabus.SyllabusModel
import com.instructure.student.mobius.syllabus.SyllabusPresenter
import com.instructure.student.mobius.syllabus.SyllabusRepository
import com.instructure.student.mobius.syllabus.SyllabusUpdate

abstract class SyllabusFragment : MobiusFragment<SyllabusModel, SyllabusEvent, SyllabusEffect, SyllabusView, SyllabusViewState, FragmentSyllabusBinding>() {

    val canvasContext by ParcelableArg<Course>(key = Const.CANVAS_CONTEXT)

    override fun makeEffectHandler() = SyllabusEffectHandler(getRepository())

    override fun makeUpdate() = SyllabusUpdate()

    override fun makePresenter() = SyllabusPresenter

    override fun makeInitModel() = SyllabusModel(canvasContext.id)

    abstract fun getRepository(): SyllabusRepository
}
