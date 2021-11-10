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
package com.instructure.student.mobius.assignmentDetails.submission.annnotation

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.databinding.FragmentAnnotationSubmissionUploadBinding
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.PdfStudentSubmissionView
import com.instructure.student.mobius.common.ui.SubmissionService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnnotationSubmissionUploadFragment : Fragment() {

    private var submissionId by LongArg(key = SUBMISSION_ID)
    private var annotatableAttachmentId by LongArg(key = ANNOTATABLE_ATTACHMENT_ID)
    private var assignmentId by LongArg(key = Const.ASSIGNMENT_ID)
    private var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)
    private var assignmentName by StringArg(key = Const.ASSIGNMENT_NAME)

    private val viewModel: AnnotationSubmissionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAnnotationSubmissionUploadBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.loadAnnotatedPdfUrl(submissionId)

        viewModel.pdfUrl.observe(viewLifecycleOwner, {
            binding.annotationSubmissionViewContainer.addView(
                PdfStudentSubmissionView(requireActivity(), it, true)
            )
        })

        setUpToolbar(binding.annotationSubmissionToolbar)

        return binding.root
    }

    private fun setUpToolbar(toolbar: Toolbar) {
        toolbar.setupAsBackButton { requireActivity().onBackPressed() }
        toolbar.setMenu(R.menu.menu_submit_generic) {
            when (it.itemId) {
                R.id.menuSubmit -> {
                    SubmissionService.startStudentAnnotationSubmission(requireContext(), canvasContext, assignmentId, assignmentName, annotatableAttachmentId)
                    requireActivity().onBackPressed()
                }
            }
        }
        ViewStyler.themeToolbarBottomSheet(requireActivity(), false, toolbar, Color.BLACK, false)
    }

    companion object {

        private const val ANNOTATABLE_ATTACHMENT_ID = "annotatable_attachment_id"
        private const val SUBMISSION_ID = "submission_id"

        fun newInstance(route: Route): AnnotationSubmissionUploadFragment {
            return AnnotationSubmissionUploadFragment().withArgs(route.arguments)
        }

        fun makeRoute(
            canvasContext: CanvasContext,
            annotatableAttachmentId: Long,
            submissionId: Long,
            assignmentId: Long,
            assignmentName: String
        ): Route {
            val bundle = Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putLong(ANNOTATABLE_ATTACHMENT_ID, annotatableAttachmentId)
                putLong(SUBMISSION_ID, submissionId)
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                putString(Const.ASSIGNMENT_NAME, assignmentName)
            }
            return Route(AnnotationSubmissionUploadFragment::class.java, canvasContext, bundle)
        }
    }
}