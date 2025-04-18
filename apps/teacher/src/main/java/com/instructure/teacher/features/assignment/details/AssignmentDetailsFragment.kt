/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */
package com.instructure.teacher.features.assignment.details

import android.os.Bundle
import android.view.LayoutInflater
import android.webkit.WebView
import android.widget.Button
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Assignment.Companion.getSubmissionTypeFromAPIString
import com.instructure.canvasapi2.models.Assignment.Companion.submissionTypeToPrettyPrintString
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.interactions.Identity
import com.instructure.interactions.MasterDetailInteractions
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_ASSIGNMENT_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.pandautils.features.lti.LtiLaunchFragment
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.accessibilityClassName
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.isTablet
import com.instructure.pandautils.utils.loadHtmlWithIframes
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.withArgs
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.R
import com.instructure.teacher.activities.InternalWebViewActivity
import com.instructure.teacher.activities.SpeedGraderActivity
import com.instructure.teacher.databinding.FragmentAssignmentDetailsBinding
import com.instructure.teacher.dialog.NoInternetConnectionDialog
import com.instructure.teacher.events.AssignmentDeletedEvent
import com.instructure.teacher.events.AssignmentGradedEvent
import com.instructure.teacher.events.AssignmentUpdatedEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.factory.AssignmentDetailPresenterFactory
import com.instructure.teacher.features.assignment.submission.SubmissionListFragment
import com.instructure.teacher.features.assignment.submission.SubmissionListFilter
import com.instructure.teacher.fragments.DueDatesFragment
import com.instructure.teacher.fragments.EditAssignmentDetailsFragment
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.getColorCompat
import com.instructure.teacher.utils.setupBackButtonWithExpandCollapseAndBack
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.utils.updateToolbarExpandCollapseIcon
import com.instructure.teacher.viewinterface.AssignmentDetailsView
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Date

@PageView
@ScreenView(SCREEN_VIEW_ASSIGNMENT_DETAILS)
class AssignmentDetailsFragment : BasePresenterFragment<
        AssignmentDetailsPresenter,
        AssignmentDetailsView,
        FragmentAssignmentDetailsBinding>(),
    AssignmentDetailsView,
    Identity {

    private var assignment: Assignment by ParcelableArg(Assignment(), ASSIGNMENT)
    private var course: Course by ParcelableArg(Course())
    private var assignmentId: Long by LongArg(0L, ASSIGNMENT_ID)

    private var needToForceNetwork = false

    private var loadHtmlJob: Job? = null

    @Suppress("unused")
    @PageViewUrl
    fun makePageViewUrl() = "${ApiPrefs.fullDomain}/${course.contextId.replace("_", "s/")}/${assignment.id}"

    override val bindingInflater: (layoutInflater: LayoutInflater) -> FragmentAssignmentDetailsBinding = FragmentAssignmentDetailsBinding::inflate

    override fun onRefreshFinished() {}

    override fun onRefreshStarted() {
        binding.toolbar.menu.clear()
        clearListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadHtmlJob?.cancel()
    }

    override fun onReadySetGo(presenter: AssignmentDetailsPresenter) {
        // if we don't have an assignmentId that means we have an assignment, so we can load the data
        if(assignmentId == 0L) {
            presenter.loadData(needToForceNetwork)
        } else {
            presenter.getAssignment(assignmentId, course)
        }
    }

    override fun populateAssignmentDetails(assignment: Assignment) = with(binding) {
        this@AssignmentDetailsFragment.assignment = assignment
        toolbar.setupMenu(R.menu.menu_assignment_details) { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit -> {
                    openEditPage(assignment)
                }

                R.id.menu_speedGrader -> {
                    SpeedGraderActivity.createIntent(
                        requireContext(),
                        course.id,
                        assignment.id,
                        -1
                    ).let {
                        startActivity(it)
                    }
                }
            }
        }
        swipeRefreshLayout.isRefreshing = false
        setupViews(assignment)
        setupListeners(assignment)
        ViewStyler.themeToolbarColored(requireActivity(), toolbar, course.color, requireContext().getColor(R.color.textLightest))
    }

    override fun getPresenterFactory() = AssignmentDetailPresenterFactory(assignment)

    override fun onPresenterPrepared(presenter: AssignmentDetailsPresenter) {}

    private fun setupToolbar() = with(binding) {
        toolbar.setupBackButtonWithExpandCollapseAndBack(this@AssignmentDetailsFragment) {
            toolbar.updateToolbarExpandCollapseIcon(this@AssignmentDetailsFragment)
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, course.color, requireContext().getColor(R.color.textLightest))
            (activity as MasterDetailInteractions).toggleExpandCollapse()
        }

        toolbar.title = getString(R.string.assignment_details)
        if(!isTablet) {
            toolbar.subtitle = course.name
        }
        ViewStyler.themeToolbarColored(requireActivity(), toolbar, course.color, requireContext().getColor(R.color.textLightest))
    }

    private fun setupViews(assignment: Assignment) = with(binding) {
        swipeRefreshLayout.setOnRefreshListener {
            presenter.loadData(true)

            // Send out bus events to trigger a refresh for assignment list and submission list
            AssignmentGradedEvent(assignment.id, javaClass.simpleName).post()
            AssignmentUpdatedEvent(assignment.id, javaClass.simpleName).post()
        }

        availabilityLayout.setGone()
        availableFromLayout.setGone()
        availableToLayout.setGone()
        dueForLayout.setGone()
        dueDateLayout.setGone()
        otherDueDateTextView.setGone()

        // Assignment name
        assignmentNameTextView.text = assignment.name

        // See Configure Assignment Region
        configurePointsPossible(assignment)
        configurePublishStatus(assignment)
        configureLockStatus(assignment)
        configureDueDates(assignment)
        configureSubmissionTypes(assignment)
        configureDescription(assignment)
        configureSubmissionDonuts(assignment)
        configureViewDiscussionButton(assignment)

        dueLayout.accessibilityClassName(Button::class.java.name)
    }

    // region Configure Assignment
    private fun configurePointsPossible(assignment: Assignment) = with(assignment) {
        binding.pointsTextView.text = resources.getQuantityString(
                R.plurals.quantityPointsAbbreviated,
                pointsPossible.toInt(),
                NumberHelper.formatDecimal(pointsPossible, 1, true)
        )
        binding.pointsTextView.contentDescription = resources.getQuantityString(
                R.plurals.quantityPointsFull,
                pointsPossible.toInt(),
                NumberHelper.formatDecimal(pointsPossible, 1, true))
    }

    private fun configurePublishStatus(assignment: Assignment) = with(binding) {
        if (assignment.published) {
            publishStatusIconView.setImageResource(R.drawable.ic_complete_solid)
            publishStatusIconView.setColorFilter(requireContext().getColorCompat(R.color.textSuccess))
            publishStatusTextView.setText(R.string.published)
            publishStatusTextView.setTextColor(requireContext().getColorCompat(R.color.textSuccess))
        } else {
            publishStatusIconView.setImageResource(R.drawable.ic_complete)
            publishStatusIconView.setColorFilter(requireContext().getColorCompat(R.color.textDark))
            publishStatusTextView.setText(R.string.not_published)
            publishStatusTextView.setTextColor(requireContext().getColorCompat(R.color.textDark))
        }
    }

    private fun configureLockStatus(assignment: Assignment) = assignment.allDates.singleOrNull()?.apply {
        val atSeparator = getString(R.string.at)

        if (lockDate?.before(Date()) == true) {
            binding.availabilityLayout.setVisible()
            binding.availabilityTextView.setText(R.string.closed)
        } else {
            binding.availableFromLayout.setVisible()
            binding.availableToLayout.setVisible()
            binding.availableFromTextView.text = if (unlockAt != null)
                DateHelper.getMonthDayAtTime(requireContext(), unlockDate, atSeparator) else getString(R.string.no_date_filler)
            binding.availableToTextView.text = if (lockAt!= null)
                DateHelper.getMonthDayAtTime(requireContext(), lockDate, atSeparator) else getString(R.string.no_date_filler)
        }
    }

    private fun configureDueDates(assignment: Assignment) = with(binding) {
        val atSeparator = getString(R.string.at)

        val allDates = assignment.allDates
        if (allDates.size > 1) {
            otherDueDateTextView.setVisible()
            otherDueDateTextView.setText(R.string.multiple_due_dates)
        } else {
            if (allDates.isEmpty() || allDates[0].dueAt == null) {
                otherDueDateTextView.setVisible()
                otherDueDateTextView.setText(R.string.no_due_date)

                dueForLayout.setVisible()
                dueForTextView.text = if (allDates.isEmpty() || allDates[0].isBase) getString(R.string.everyone) else allDates[0].title ?: ""

            } else with(allDates[0]) {
                dueDateLayout.setVisible()
                dueDateTextView.text = DateHelper.getMonthDayAtTime(requireContext(), dueDate, atSeparator)

                dueForLayout.setVisible()
                dueForTextView.text = if (isBase) getString(R.string.everyone) else title ?: ""
            }
        }
    }

    private fun configureSubmissionTypes(assignment: Assignment) = with(binding) {
        submissionTypesTextView.text = assignment.submissionTypesRaw.map {
            submissionTypeToPrettyPrintString(getSubmissionTypeFromAPIString(it), requireContext(), assignment.ltiToolType()) }.joinToString("\n")

        if(assignment.submissionTypesRaw.contains(Assignment.SubmissionType.EXTERNAL_TOOL.apiString)) {
            // External tool
            submissionTypesArrowIcon.setVisible()
            submissionTypesLayout.onClickWithRequireNetwork {
                val ltiUrl = assignment.url.validOrNull() ?: assignment.htmlUrl
                if(!ltiUrl.isNullOrBlank()) {
                    val route = LtiLaunchFragment.makeRoute(course, ltiUrl, assignment.name!!, assignmentLti = true, openInternally = assignment.ltiToolType().openInternally)
                    RouteMatcher.route(requireActivity(), route)
                }
            }
        }

        submissionsLayout.setVisible(!course.isDesigner)
    }

    @Suppress("UsePropertyAccessSyntax")
    private fun configureDescription(assignment: Assignment): Unit = with(binding) {
        noDescriptionTextView.setVisible(assignment.description.isNullOrBlank())

        // Show progress bar while loading description
        descriptionProgressBar.announceForAccessibility(getString(R.string.loading))
        descriptionProgressBar.setVisible()
        descriptionWebViewWrapper.webView.addVideoClient(requireActivity())
        descriptionWebViewWrapper.webView.canvasWebChromeClientCallback = object : CanvasWebView.CanvasWebChromeClientCallback {
            override fun onProgressChangedCallback(view: WebView?, newProgress: Int) {
                if (newProgress >= 100) {
                    descriptionProgressBar.setGone()
                }
            }
        }

        descriptionWebViewWrapper.webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                RouteMatcher.openMedia(requireActivity(), url)
            }

            override fun onPageStartedCallback(webView: WebView, url: String) {}
            override fun onPageFinishedCallback(webView: WebView, url: String) {}
            override fun routeInternallyCallback(url: String) {
                RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, true)
            }

            override fun canRouteInternallyDelegate(url: String): Boolean = RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, false)
        }

        descriptionWebViewWrapper.webView.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
            override fun launchInternalWebViewFragment(url: String) = requireActivity().startActivity(InternalWebViewActivity.createIntent(requireActivity(), url, "", true))
            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
        }

        // Load description
        loadHtmlJob = descriptionWebViewWrapper.webView.loadHtmlWithIframes(requireContext(), assignment.description, {
            descriptionWebViewWrapper.loadHtml(it, assignment.name, baseUrl = assignment.htmlUrl)
        }) {
            RouteMatcher.route(requireActivity(), LtiLaunchFragment.makeSessionlessLtiUrlRoute(requireActivity(), course, it))
        }
    }

    private fun configureSubmissionDonuts(assignment: Assignment): Unit = with(binding) {
        if(Assignment.getGradingTypeFromString(assignment.gradingType!!, requireContext()) == Assignment.GradingType.NOT_GRADED) {
            // If the grading type is NOT_GRADED we don't want to show anything for the grading dials
            submissionsLayout.setGone()
            submissionsLayoutDivider.setGone()
        } else if(!assignment.isOnlineSubmissionType) {
            // Only show graded dial if the assignment submission type is not online
            donutGroup.notSubmittedWrapper.setGone()
            donutGroup.ungradedWrapper.setGone()
            donutGroup.assigneesWithoutGradesTextView.setVisible()
        }
    }

    private fun configureViewDiscussionButton(assignment: Assignment) = with(binding) {
        if (assignment.discussionTopicHeader != null) {
            viewDiscussionButton.setBackgroundColor(ThemePrefs.buttonColor)
            viewDiscussionButton.setTextColor(ThemePrefs.buttonTextColor)
            viewDiscussionButton.setVisible()
        } else {
            viewDiscussionButton.setGone()
        }
    }
    //endregion

    override fun updateSubmissionDonuts(totalStudents: Int, gradedStudents: Int, needsGradingCount: Int, notSubmitted: Int) = with(binding.donutGroup) {
        allTitle.setTextColor(course.color)
        allIcon.setColorFilter(course.color)
        // Submission section
        gradedChart.setSelected(gradedStudents)
        gradedChart.setTotal(totalStudents)
        gradedChart.setSelectedColor(course.color)
        gradedChart.setUnselectedColor(Color(course.color).copy(alpha = 0.2f).toArgb())
        gradedChart.setCenterText(gradedStudents.toString())
        gradedWrapper.contentDescription = getString(R.string.content_description_submission_donut_graded).format(gradedStudents, totalStudents)
        gradedWrapper.accessibilityClassName(Button::class.java.name)
        gradedProgressBar.setGone()
        gradedChart.invalidate()

        ungradedChart.setSelected(needsGradingCount)
        ungradedChart.setTotal(totalStudents)
        ungradedChart.setSelectedColor(course.color)
        ungradedChart.setUnselectedColor(Color(course.color).copy(alpha = 0.2f).toArgb())
        ungradedChart.setCenterText(needsGradingCount.toString())
        ungradedLabel.text = requireContext().resources.getQuantityText(R.plurals.needsGradingNoQuantity, needsGradingCount)
        ungradedWrapper.contentDescription = getString(R.string.content_description_submission_donut_needs_grading).format(needsGradingCount, totalStudents)
        ungradedWrapper.accessibilityClassName(Button::class.java.name)
        ungradedProgressBar.setGone()
        ungradedChart.invalidate()

        notSubmittedChart.setSelected(notSubmitted)
        notSubmittedChart.setTotal(totalStudents)
        notSubmittedChart.setSelectedColor(course.color)
        notSubmittedChart.setUnselectedColor(Color(course.color).copy(alpha = 0.2f).toArgb())
        notSubmittedChart.setCenterText(notSubmitted.toString())

        notSubmittedWrapper.contentDescription = getString(R.string.content_description_submission_donut_unsubmitted).format(notSubmitted, totalStudents)
        notSubmittedWrapper.accessibilityClassName(Button::class.java.name)
        notSubmittedProgressBar.setGone()
        notSubmittedChart.invalidate()

        // Only show graded dial if the assignment submission type is not online
        if (!presenter.mAssignment.isOnlineSubmissionType) {
            val totalCount = needsGradingCount + notSubmitted
            assigneesWithoutGradesTextView.text = requireContext().resources.getQuantityString(R.plurals.assignees_without_grades, totalCount, totalCount)
        }
    }

    private fun clearListeners() = with(binding) {
        dueLayout.setOnClickListener {}
        submissionsLayout.setOnClickListener {}
        donutGroup.gradedWrapper.setOnClickListener {}
        donutGroup.ungradedWrapper.setOnClickListener {}
        donutGroup.notSubmittedWrapper.setOnClickListener {}
        noDescriptionTextView.setOnClickListener {}
        donutGroup.assigneesWithoutGradesTextView.setOnClickListener {}
        viewDiscussionButton.setOnClickListener {}
    }

    private fun setupListeners(assignment: Assignment) = with(binding) {
        dueLayout.setOnClickListener {
            val args = DueDatesFragment.makeBundle(assignment)
            RouteMatcher.route(requireActivity(), Route(null, DueDatesFragment::class.java, course, args))
        }

        submissionsLayout.setOnClickListener {
            navigateToSubmissions(course, assignment, SubmissionListFilter.ALL)
        }
        donutGroup.viewAllSubmissions.onClick { submissionsLayout.performClick() } // Separate click listener for a11y
        donutGroup.gradedWrapper.setOnClickListener {
            navigateToSubmissions(course, assignment, SubmissionListFilter.GRADED)
        }
        donutGroup.ungradedWrapper.setOnClickListener {
            navigateToSubmissions(course, assignment, SubmissionListFilter.NOT_GRADED)
        }
        donutGroup.notSubmittedWrapper.setOnClickListener {
            navigateToSubmissions(course, assignment, SubmissionListFilter.MISSING)
        }
        noDescriptionTextView.setOnClickListener { openEditPage(assignment) }

        donutGroup.assigneesWithoutGradesTextView.setOnClickListener {
            submissionsLayout.performClick()
        }

        assignment.discussionTopicHeader?.let { discussionTopicHeader ->
            viewDiscussionButton.setOnClickListener {
                RouteMatcher.route(requireActivity(), DiscussionRouterFragment.makeRoute(course, discussionTopicHeader))
            }
        } ?: viewDiscussionButton.setGone()

    }

    private fun openEditPage(assignment: Assignment) {
        if(APIHelper.hasNetworkConnection()) {
            val args = EditAssignmentDetailsFragment.makeBundle(assignment, false)
            RouteMatcher.route(requireActivity(), Route(EditAssignmentDetailsFragment::class.java, course, args))
        } else {
            NoInternetConnectionDialog.show(requireFragmentManager())
        }
    }

    private fun navigateToSubmissions(course: Course, assignment: Assignment, filter: SubmissionListFilter) {
        val args = SubmissionListFragment.makeBundle(assignment, filter)
        RouteMatcher.route(requireActivity(), Route(null, SubmissionListFragment::class.java, course, args))
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onAssignmentEdited(event: AssignmentUpdatedEvent) {
        event.once(javaClass.simpleName) {
            if (it == presenter.mAssignment.id) needToForceNetwork = true
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onAssignmentDeleted(event: AssignmentDeletedEvent) {
        event.once(javaClass.simpleName) {
            if (it == presenter.mAssignment.id) activity?.onBackPressed()
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onAssignmentGraded(event: AssignmentGradedEvent) {
        event.once(javaClass.simpleName) {
            if(presenter.mAssignment.id == it) needToForceNetwork = true
        }
    }

    //Because of the presenter lifecycle using the assignment from there will result in random crashes.
    override val identity: Long? get() = if(assignmentId != 0L) assignmentId else assignment.id
    override val skipCheck: Boolean get() = false

    companion object {
        @JvmStatic val ASSIGNMENT = "assignment"
        @JvmStatic val ASSIGNMENT_ID = "assignmentId"

        fun newInstance(course: Course, args: Bundle) = AssignmentDetailsFragment().withArgs(args).apply {
            this.course = course
        }

        fun makeBundle(assignment: Assignment): Bundle {
            val args = Bundle()
            args.putParcelable(ASSIGNMENT, assignment)
            return args
        }

        fun makeBundle(assignmentId: Long): Bundle {
            val args = Bundle()
            args.putLong(ASSIGNMENT_ID, assignmentId)
            return args
        }

        fun makeRoute(course: CanvasContext, assignmentId: Long): Route {
            val bundle = course.makeBundle { putLong(ASSIGNMENT_ID, assignmentId) }
            return Route(null, AssignmentDetailsFragment::class.java, course, bundle)
        }
    }
}
