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
package com.instructure.teacher.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.features.speedgrader.SpeedGraderSharedViewModel
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.teacher.features.speedgrader.PdfTeacherSubmissionView

class PdfSubmissionFragment : BaseCanvasFragment() {

    private val sharedViewModel: SpeedGraderSharedViewModel by activityViewModels()

    private var pdfUrl by StringArg()
    private var courseId by LongArg()
    private var assigneeId by LongArg()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return PdfTeacherSubmissionView(requireActivity(), pdfUrl, courseId, assigneeId, childFragmentManager)
    }

    companion object {

        const val PDF_URL = "pdfUrl"
        const val COURSE_ID = "courseId"
        const val ASSIGNEE_ID = "assigneeId"

        fun newInstance(url: String, courseId: Long) = PdfSubmissionFragment().apply {
            pdfUrl = url
            this.courseId = courseId
        }

        fun createBundle(url: String, courseId: Long, assigneeId: Long): Bundle {
            return Bundle().apply {
                putString(PDF_URL, url)
                putLong(COURSE_ID, courseId)
                putLong(ASSIGNEE_ID, assigneeId)
            }
        }
    }
}