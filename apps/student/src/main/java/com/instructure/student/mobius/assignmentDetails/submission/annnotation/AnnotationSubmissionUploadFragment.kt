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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.instructure.pandautils.base.BaseCanvasFragment
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyBottomSystemBarInsets
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.setMenu
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.R
import com.instructure.student.databinding.FragmentAnnotationSubmissionUploadBinding
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.PdfStudentSubmissionView
import com.instructure.student.mobius.common.ui.SubmissionHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AnnotationSubmissionUploadFragment : BaseCanvasFragment() {

    @Inject
    lateinit var submissionHelper: SubmissionHelper

    private var submissionId by LongArg(key = SUBMISSION_ID)
    private var annotatableAttachmentId by LongArg(key = ANNOTATABLE_ATTACHMENT_ID)
    private var assignmentId by LongArg(key = Const.ASSIGNMENT_ID)
    private var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)
    private var assignmentName by StringArg(key = Const.ASSIGNMENT_NAME)
    private var attempt by LongArg(key = Const.SUBMISSION_ATTEMPT, default = 1L)

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

        viewModel.pdfUrl.observe(viewLifecycleOwner) {
            binding.annotationSubmissionViewContainer.addView(
                PdfStudentSubmissionView(
                    activity = requireActivity(),
                    pdfUrl = it,
                    fragmentManager = childFragmentManager,
                    studentAnnotationSubmit = true,
                    courseId = canvasContext.id
                )
            )
        }

        setUpToolbar(binding.annotationSubmissionToolbar)
        binding.annotationSubmissionToolbar.applyTopSystemBarInsets()
        binding.annotationSubmissionViewContainer.applyBottomSystemBarInsets()

        return binding.root
    }

    private fun setUpToolbar(toolbar: Toolbar) {
        toolbar.setupAsBackButton { requireActivity().onBackPressed() }
        toolbar.setMenu(R.menu.menu_submit_generic) {
            when (it.itemId) {
                R.id.menuSubmit -> {
                    submissionHelper.startStudentAnnotationSubmission(canvasContext, assignmentId, assignmentName, annotatableAttachmentId, attempt)
                    requireActivity().onBackPressed()
                }
            }
        }
        ViewStyler.themeToolbarLight(requireActivity(), toolbar)
    }

    companion object {

        private const val ANNOTATABLE_ATTACHMENT_ID = "annotatable_attachment_id"
        private const val SUBMISSION_ID = "submission_id"

        fun newInstance(route: Route): AnnotationSubmissionUploadFragment {
            return AnnotationSubmissionUploadFragment()
                .withArgs(route.arguments)
        }

        fun makeRoute(
            canvasContext: CanvasContext,
            annotatableAttachmentId: Long,
            submissionId: Long,
            assignmentId: Long,
            assignmentName: String,
            attempt: Long = 1L
        ): Route {
            val bundle = Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putLong(ANNOTATABLE_ATTACHMENT_ID, annotatableAttachmentId)
                putLong(SUBMISSION_ID, submissionId)
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                putString(Const.ASSIGNMENT_NAME, assignmentName)
                putLong(Const.SUBMISSION_ATTEMPT, attempt)
            }
            return Route(AnnotationSubmissionUploadFragment::class.java, canvasContext, bundle)
        }
    }
}