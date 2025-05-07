/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_SPEED_GRADER_QUIZ_SUBMISSION
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.speedgrader.content.QuizContent
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentSpeedGraderQuizSubmissionBinding
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.view.QuizSubmissionGradedEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@ScreenView(SCREEN_VIEW_SPEED_GRADER_QUIZ_SUBMISSION)
class SpeedGraderQuizSubmissionFragment : BaseCanvasFragment() {

    private val binding by viewBinding(FragmentSpeedGraderQuizSubmissionBinding::bind)

    private var courseId by LongArg()
    private var assignmentId by LongArg()
    private var studentId by LongArg()
    private var url by StringArg()
    private var pendingReview by BooleanArg()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_speed_grader_quiz_submission, container, false)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        setupViews()
    }

    private fun setupViews() = with(binding) {
        ViewStyler.themeButton(gradeQuizButton)
        ViewStyler.themeButton(viewQuizButton)
        gradeQuizButton.onClick { viewQuizSubmission() }
        viewQuizButton.onClick { viewQuizSubmission() }
        if (pendingReview) {
            viewQuizButton.setGone()
            pendingReviewLabel.setVisible()
            gradeQuizButton.setVisible()
        } else {
            pendingReviewLabel.setInvisible()
            gradeQuizButton.setGone()
            viewQuizButton.setVisible()
        }
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onQuizGraded(event: QuizSubmissionGradedEvent) {
        event.once("QuizSubFrag|$courseId|$assignmentId|$studentId") {
            if (it.userId != studentId) return@once
            pendingReview = false
            setupViews()
        }
    }

    private fun viewQuizSubmission() {
        val bundle = SpeedGraderQuizWebViewFragment.newInstance(courseId, assignmentId, studentId, url).nonNullArgs
        RouteMatcher.route(requireActivity(), Route(SpeedGraderQuizWebViewFragment::class.java, null, bundle))
    }

    companion object {
        fun newInstance(content: QuizContent) = SpeedGraderQuizSubmissionFragment().apply {
            courseId = content.courseId
            assignmentId = content.assignmentId
            studentId = content.studentId
            url = content.url
            pendingReview = content.pendingReview
        }

        fun createBundle(content: QuizContent) = Bundle().apply {
            putLong("courseId", content.courseId)
            putLong("assignmentId", content.assignmentId)
            putLong("studentId", content.studentId)
            putString("url", content.url)
            putBoolean("pendingReview", content.pendingReview)
        }
    }
}
