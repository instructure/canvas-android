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
package com.instructure.pandautils.features.speedgrader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.GradeableStudentSubmission
import com.instructure.canvasapi2.models.StudentAssignee
import com.instructure.canvasapi2.utils.coerceAtLeast
import com.instructure.canvasapi2.utils.rangeWithin
import com.instructure.interactions.router.Route
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.color
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class SpeedGraderFragment : BaseCanvasFragment() {

    private val viewModel: SpeedGraderViewModel by viewModels()

    private val sharedViewModel: SpeedGraderSharedViewModel by activityViewModels()

    private val courseId by LongArg(key = Const.COURSE_ID)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ThemePrefs.reapplyCanvasTheme(requireActivity())
        ViewStyler.setStatusBarDark(requireActivity(), CanvasContext.emptyCourseContext(courseId).color)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                CanvasTheme(courseColor = Color(CanvasContext.emptyCourseContext(courseId).color)) {
                    val uiState by viewModel.uiState.collectAsState()
                    SpeedGraderScreen(uiState, sharedViewModel) {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        }
    }

    companion object {
        const val FILTER = "filter"
        const val FILTER_VALUE = "filter_value"
        const val FILTERED_SUBMISSION_IDS = "filtered_submission_ids"
        const val DISCUSSION_ENTRY_ID = "discussion_entry_id"

        private const val MAX_CACHED_ADJACENT = 6

        private const val MAX_HISTORY_THRESHOLD = 8

        fun newInstance(route: Route): SpeedGraderFragment {
            return SpeedGraderFragment().apply {
                arguments = route.arguments
            }
        }

        fun newInstance(bundle: Bundle): SpeedGraderFragment {
            return SpeedGraderFragment().apply {
                arguments = bundle
            }
        }

        fun makeBundle(
            courseId: Long,
            assignmentId: Long,
            selectedIdx: Int,
            anonymousGrading: Boolean? = null,
            filteredSubmissionIds: LongArray = longArrayOf(),
            filter: SubmissionListFilter? = null,
            filterValue: Double = 0.0
        ): Bundle {
            return Bundle().apply {
                putLong(Const.COURSE_ID, courseId)
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                putInt(Const.SELECTED_ITEM, selectedIdx)
                putBoolean(Const.ANONYMOUS_GRADING, anonymousGrading ?: false)
                putSerializable(FILTER, filter)
                putDouble(FILTER_VALUE, filterValue)
                putLongArray(FILTERED_SUBMISSION_IDS, filteredSubmissionIds)
            }
        }

        fun makeBundle(courseId: Long, assignmentId: Long, submissions: List<GradeableStudentSubmission>, selectedIdx: Int, anonymousGrading: Boolean? = null): Bundle {
            return Bundle().apply {
                putLong(Const.COURSE_ID, courseId)
                putLong(Const.ASSIGNMENT_ID, assignmentId)

                // Avoid TransactionTooLargeException by only bundling submissions in the cached range with shallow submission histories
                val cachedRange = selectedIdx.rangeWithin(MAX_CACHED_ADJACENT).coerceAtLeast(0)
                val compactSubmissions = submissions.mapIndexed { index, submission ->
                    val inRange = index in cachedRange
                    val smallHistory = (submission.submission?.submissionHistory?.size
                        ?: 0) <= MAX_HISTORY_THRESHOLD
                    val smallBodies = submission.submission?.submissionHistory?.none {
                        (it?.body?.length ?: 0) > 2048
                    } ?: true
                    if (inRange && smallHistory && smallBodies && submission.submission != null) {
                        submission.copy(isCached = true)
                    } else {
                        submission.copy(submission = null, isCached = false)
                    }
                }

                // Only sort when anon grading is off
                val anonymousGradingOn = anonymousGrading ?: (submissions.firstOrNull()?.submission?.assignment?.anonymousGrading == true)

                if(!anonymousGradingOn) {
                    // We need to sort the submissions so they appear in the same order as the submissions list
                    putParcelableArrayList(Const.SUBMISSION, ArrayList(compactSubmissions.sortedBy {
                        (it.assignee as? StudentAssignee)?.student?.sortableName?.lowercase(Locale.getDefault())
                    }))
                } else {
                    putParcelableArrayList(Const.SUBMISSION, ArrayList(compactSubmissions))
                }

                putInt(Const.SELECTED_ITEM, selectedIdx)

                putBoolean(Const.ANONYMOUS_GRADING, anonymousGradingOn)
            }
        }
    }
}