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

import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.student.mobius.common.ui.MobiusView
import com.instructure.student.mobius.syllabus.SyllabusEvent
import com.spotify.mobius.functions.Consumer

class SyllabusView(inflater: LayoutInflater, parent: ViewGroup) : MobiusView<SyllabusViewState, SyllabusEvent>(0, inflater, parent) {
    override fun onConnect(output: Consumer<SyllabusEvent>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun render(state: SyllabusViewState) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDispose() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun applyTheme() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun showAssignmentView(assignmentId: Long, context: CanvasContext) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun showScheduleItemView(scheduleItem: ScheduleItem, context: CanvasContext) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
