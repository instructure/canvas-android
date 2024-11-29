/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.pandautils.base.BaseCanvasFragment
import androidx.fragment.app.viewModels
import com.instructure.pandautils.utils.LongArg
import com.instructure.student.databinding.FragmentAnnotationSubmissionViewBinding
import com.instructure.student.mobius.assignmentDetails.submission.annnotation.AnnotationSubmissionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnnotationSubmissionViewFragment : BaseCanvasFragment() {

    private var submissionId by LongArg()
    private var submissionAttempt by LongArg()
    private var courseId by LongArg()

    private val viewModel: AnnotationSubmissionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAnnotationSubmissionViewBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.loadAnnotatedPdfUrl(submissionId, submissionAttempt.toString())

        viewModel.pdfUrl.observe(viewLifecycleOwner) {
            binding.annotationSubmissionViewContainer.addView(
                PdfStudentSubmissionView(
                    requireActivity(),
                    it,
                    courseId,
                    childFragmentManager,
                    studentAnnotationView = true,
                )
            )
        }

        return binding.root
    }

    companion object {
        fun newInstance(submissionId: Long, submissionAttempt: Long, courseId: Long): AnnotationSubmissionViewFragment {
            return AnnotationSubmissionViewFragment().apply {
                this.submissionId = submissionId
                this.submissionAttempt = submissionAttempt
                this.courseId = courseId
            }
        }
    }
}