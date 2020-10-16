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
package com.instructure.teacher.features.syllabus.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.pandautils.utils.withArgs
import com.instructure.teacher.features.syllabus.*
import com.instructure.teacher.mobius.common.ui.MobiusFragment

class SyllabusFragment : MobiusFragment<SyllabusModel, SyllabusEvent, SyllabusEffect, SyllabusView, SyllabusViewState>() {

    override fun makeEffectHandler() = SyllabusEffectHandler()

    override fun makeUpdate() = SyllabusUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) = SyllabusView(inflater, parent)

    override fun makePresenter() = SyllabusPresenter

    override fun makeInitModel() = SyllabusModel("Asd")

    companion object {
        fun newInstance(args: Bundle) = SyllabusFragment().withArgs(args)
    }
}