/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.withArgs
import com.instructure.teacher.features.syllabus.SyllabusRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SyllabusRepositoryFragment : SyllabusFragment() {

    @Inject
    lateinit var syllabusRepository: SyllabusRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
    }

    override fun getRepository() = syllabusRepository

    companion object {
        fun newInstance(canvasContext: CanvasContext?) = if (isValidRoute(canvasContext)) createFragmentWithCanvasContext(canvasContext) else null

        private fun isValidRoute(canvasContext: CanvasContext?) = canvasContext is Course

        private fun createFragmentWithCanvasContext(canvasContext: CanvasContext?): SyllabusRepositoryFragment {
            return SyllabusRepositoryFragment().withArgs {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
            }
        }
    }
}
