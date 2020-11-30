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
package com.instructure.teacher.features.syllabus.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.teacher.mobius.common.ui.EffectHandler
import com.instructure.teacher.mobius.common.ui.MobiusFragment
import com.instructure.teacher.mobius.common.ui.Presenter
import com.instructure.teacher.mobius.common.ui.UpdateInit

private const val SUMMARY_ALLOWED = "summaryAllowed"

class EditSyllabusFragment : MobiusFragment<EditSyllabusModel, EditSyllabusEvent, EditSyllabusEffect, EditSyllabusView, EditSyllabusViewState>() {

    private val course by ParcelableArg<Course>()
    private val summaryAllowed: Boolean by BooleanArg(key = SUMMARY_ALLOWED)

    override fun makeEffectHandler(): EffectHandler<EditSyllabusView, EditSyllabusEvent, EditSyllabusEffect> = EditSyllabusEffectHandler()

    override fun makeUpdate(): UpdateInit<EditSyllabusModel, EditSyllabusEvent, EditSyllabusEffect> = EditSyllabusUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup): EditSyllabusView = EditSyllabusView(inflater, parent)

    override fun makePresenter(): Presenter<EditSyllabusModel, EditSyllabusViewState> = EditSyllabusPresenter()

    override fun makeInitModel(): EditSyllabusModel = EditSyllabusModel(course, summaryAllowed)

    companion object {

        fun newInstance(bundle: Bundle): EditSyllabusFragment {
            return EditSyllabusFragment().apply {
                arguments = bundle
            }
        }

        fun createArgs(course: Course, summaryAllowed: Boolean): Bundle {
            val extras = Bundle()
            extras.putParcelable(Const.COURSE, course)
            extras.putBoolean(SUMMARY_ALLOWED, summaryAllowed)
            return extras
        }
    }
}