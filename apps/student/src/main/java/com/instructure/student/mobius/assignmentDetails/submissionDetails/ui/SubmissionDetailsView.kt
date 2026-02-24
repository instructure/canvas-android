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

package com.instructure.student.mobius.assignmentDetails.submissionDetails.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.android.material.tabs.TabLayout
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.toBaseUrl
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.AnalyticsParamConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.pandautils.activities.BaseViewMediaActivity
import com.instructure.pandautils.binding.BindableSpinnerAdapter
import com.instructure.pandautils.features.assignmentdetails.AssignmentDetailsAttemptItemViewModel
import com.instructure.pandautils.features.assignmentdetails.AssignmentDetailsAttemptViewData
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyImeAndSystemBarInsets
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.asStateList
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.hideKeyboard
import com.instructure.pandautils.utils.isAccessibilityEnabled
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.withLuminance
import com.instructure.pandautils.views.RecordingMediaType
import com.instructure.student.R
import com.instructure.student.databinding.FragmentSubmissionDetailsBinding
import com.instructure.student.features.modules.progression.NotAvailableOfflineFragment
import com.instructure.student.fragment.ViewImageFragment
import com.instructure.student.fragment.ViewUnsupportedFileFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsContentType
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.AnnotationSubmissionViewFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.DiscussionSubmissionViewFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.LtiSubmissionViewFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.MediaSubmissionViewFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.PdfSubmissionViewFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.QuizSubmissionViewFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.TextSubmissionViewFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.UrlSubmissionViewFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionMessageFragment
import com.instructure.student.mobius.common.ui.MobiusView
import com.instructure.student.router.RouteMatcher
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.spotify.mobius.functions.Consumer
import java.io.File

class SubmissionDetailsView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup,
    private val canvasContext: CanvasContext,
    private val fragmentManager: FragmentManager
) : MobiusView<SubmissionDetailsViewState, SubmissionDetailsEvent, FragmentSubmissionDetailsBinding>(
    layoutInflater,
    FragmentSubmissionDetailsBinding::inflate,
    parent
) {

    private var drawerPagerAdapter = SubmissionDetailsDrawerPagerAdapter(fragmentManager)

    /* Tab selection listener for the drawer ViewPager */
    private val drawerTabLayoutListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
        override fun onTabReselected(tab: TabLayout.Tab?) = onTabSelected(tab)
        override fun onTabSelected(tab: TabLayout.Tab?) {
            if (binding.slidingUpPanelLayout.panelState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                binding.slidingUpPanelLayout.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
            }
            binding.drawerViewPager.hideKeyboard()
            logTabSelected(tab?.position)
        }
    }

    private fun logTabSelected(position: Int?) {
        when (position) {
            0 -> logEvent(AnalyticsEventConstants.SUBMISSION_COMMENTS_SELECTED)
            1 -> logEvent(AnalyticsEventConstants.SUBMISSION_FILES_SELECTED)
            2 -> logEvent(AnalyticsEventConstants.SUBMISSION_RUBRIC_SELECTED)
        }
    }

    init {
        binding.toolbar.applyTopSystemBarInsets()
        binding.toolbar.setupAsBackButton { activity.onBackPressed() }
        binding.retryButton.onClick { consumer?.accept(SubmissionDetailsEvent.RefreshRequested) }
        binding.drawerViewPager.offscreenPageLimit = 3
        binding.drawerViewPager.adapter = drawerPagerAdapter
        configureDrawerTabLayout()
        configureSlidingPanelHeight()

        binding.slidingUpPanelLayout.applyImeAndSystemBarInsets()

        if (isAccessibilityEnabled(context)) {
            binding.slidingUpPanelLayout.anchorPoint = 1.0f
        }
    }

    private fun configureDrawerTabLayout() = with(binding) {
        drawerTabLayout.setupWithViewPager(drawerViewPager)

        // Tint the tab with the course color
        val tint = canvasContext.color
        drawerTabLayout.setSelectedTabIndicatorColor(tint)
        drawerTabLayout.setTabTextColors(ContextCompat.getColor(context, R.color.textDarkest), tint)

        // Use 90% luminance to ensure a 'light' ripple effect that doesn't overpower the tab text
        val rippleTint = tint.withLuminance(0.90f).asStateList()
        drawerTabLayout.tabRippleColor =  rippleTint
    }

    private fun configureSlidingPanelHeight() = with(binding) {
        /* Adjusts the panel content height based on the position of the sliding portion of the view, but only if
         * it is at (or has passed) the anchor point. */
        slidingUpPanelLayout.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelStateChanged(
                panel: View?,
                previousState: SlidingUpPanelLayout.PanelState?,
                newState: SlidingUpPanelLayout.PanelState?
            ) {
                when (newState) {
                    SlidingUpPanelLayout.PanelState.ANCHORED -> {
                        submissionContent.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                    }
                    SlidingUpPanelLayout.PanelState.EXPANDED -> {
                        submissionContent.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                    }
                    SlidingUpPanelLayout.PanelState.COLLAPSED -> {
                        submissionContent.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                    }
                    else -> {}
                }
            }

            override fun onPanelSlide(panel: View?, offset: Float) {
                val maxHeight = contentWrapper.height
                if (offset < ANCHOR_POINT || maxHeight == 0) return
                val adjustedHeight = Math.abs(maxHeight * offset)
                drawerViewPager.layoutParams?.height = adjustedHeight.toInt()
                drawerViewPager.requestLayout()
            }
        })

        /* Listens for layout changes on the content and adjusts the panel content height accordingly. This ensures we
         * use the correct height for initial layout and after orientation changes. */
        contentWrapper.addOnLayoutChangeListener { _, _, top, _, bottom, _, oldTop, _, oldBottom ->
            val oldHeight = oldBottom - oldTop
            val newHeight = bottom - top
            if (oldHeight != newHeight) {
                contentWrapper.post {
                    val slideOffset = when (slidingUpPanelLayout.panelState) {
                        SlidingUpPanelLayout.PanelState.EXPANDED -> 1f
                        else -> ANCHOR_POINT
                    }
                    drawerViewPager.layoutParams?.height = (newHeight * slideOffset).toInt()
                    drawerViewPager.requestLayout()
                }
            }
        }
    }

    override fun applyTheme() {
        ViewStyler.themeToolbarColored(activity, binding.toolbar, canvasContext)
    }

    override fun onConnect(output: Consumer<SubmissionDetailsEvent>) {
        consumer = output
    }

    override fun onDispose() {
        consumer = null
    }

    override fun render(state: SubmissionDetailsViewState) {
        // Reset visibilities
        binding.errorContainer.setGone()
        binding.slidingUpPanelLayout.setGone()
        binding.loadingView.setGone()

        when (state) {
            SubmissionDetailsViewState.Error -> binding.errorContainer.setVisible()
            SubmissionDetailsViewState.Loading -> binding.loadingView.setVisible()
            is SubmissionDetailsViewState.Loaded -> renderLoadedState(state)
        }
    }

    private fun renderLoadedState(state: SubmissionDetailsViewState.Loaded) {
        binding.slidingUpPanelLayout.setVisible()
        binding.submissionVersionsSpinner.setVisible(state.showVersionsSpinner)
        setupSubmissionVersionSpinner(state.submissionVersions, state.selectedVersionSpinnerIndex)
        updateDrawerPager(state.tabData)
    }

    private fun updateDrawerPager(tabData: List<SubmissionDetailsTabData>) {
        /* Updating the pager adapter's data can cause the current tab to be reselected, erroneously causing the drawer
        to open up to the anchor point if it was previously closed. As a workaround we remove the tab selection
        listener temporarily, and then restore it after updating the adapter */
        binding.drawerTabLayout.removeOnTabSelectedListener(drawerTabLayoutListener)

        // Update adapter data
        drawerPagerAdapter.tabData = tabData
        drawerPagerAdapter.notifyDataSetChanged()

        // Restore tab selection listener
        binding.drawerTabLayout.addOnTabSelectedListener(drawerTabLayoutListener)
    }

    private fun setupSubmissionVersionSpinner(submissions: List<Pair<Long, String>>, selectedIdx: Int) {
        val itemViewModels = submissions.map { submission ->
            AssignmentDetailsAttemptItemViewModel(
                AssignmentDetailsAttemptViewData(
                    context.getString(R.string.attempt, submission.first),
                    submission.second
                )
            )
        }
        binding.submissionVersionsSpinner.adapter = BindableSpinnerAdapter(context, R.layout.item_submission_attempt_spinner, itemViewModels)
        binding.submissionVersionsSpinner.setSelection(selectedIdx, false)
        binding.submissionVersionsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val submissionAttempt = submissions[position].first
                consumer?.accept(SubmissionDetailsEvent.SubmissionClicked(submissionAttempt))
            }
        }
    }

    fun showSubmissionContent(type: SubmissionDetailsContentType, isOnline: Boolean) {
        fragmentManager.beginTransaction().apply {
            replace(R.id.submissionContent, getFragmentForContent(type, isOnline))
            commitAllowingStateLoss()
        }
    }

    fun showAudioRecordingView() {
        binding.floatingRecordingView.apply {
            setContentType(RecordingMediaType.Audio)
            setVisible()
            stoppedCallback = {
                consumer?.accept(SubmissionDetailsEvent.StopMediaRecordingClicked)
            }
            recordingCallback = { file ->
                consumer?.accept(SubmissionDetailsEvent.SendMediaCommentClicked(file))
            }
        }
    }

    fun showVideoRecordingView() {
        binding.floatingRecordingView.apply {
            setContentType(RecordingMediaType.Video)
            startVideoView()
            recordingCallback = { file ->
                consumer?.accept(SubmissionDetailsEvent.SendMediaCommentClicked(file))
            }
            stoppedCallback = {
                consumer?.accept(SubmissionDetailsEvent.StopMediaRecordingClicked)
            }
            replayCallback = { file ->
                consumer?.accept(SubmissionDetailsEvent.VideoRecordingReplayClicked(file))
            }
        }
    }

    fun showVideoRecordingPlayback(file: File) {
        val bundle = BaseViewMediaActivity.makeBundle(file, "video", context.getString(R.string.videoCommentReplay), true)
        RouteMatcher.route(activity as FragmentActivity, Route(bundle, RouteContext.MEDIA))
    }

    fun showVideoRecordingPlaybackError() {
        Toast.makeText(context, R.string.errorShowingVideoReplay, Toast.LENGTH_SHORT).show()
    }

    fun showMediaCommentError() {
        Toast.makeText(context, R.string.errorSubmittingMediaComment, Toast.LENGTH_SHORT).show()
    }

    private fun getFragmentForContent(type: SubmissionDetailsContentType, isOnline: Boolean): Fragment {
        return when (type) {
            is SubmissionDetailsContentType.NoSubmissionContent -> SubmissionDetailsEmptyContentFragment.newInstance(
                type.canvasContext as Course,
                type.assignment,
                type.isStudioEnabled,
                type.quiz,
                type.studioLTITool,
                type.isObserver,
                type.ltiTool
            )
            is SubmissionDetailsContentType.UrlContent -> UrlSubmissionViewFragment.newInstance(type.url, type.previewUrl)
            is SubmissionDetailsContentType.QuizContent -> getFragmentWithOnlineCheck(QuizSubmissionViewFragment.newInstance(type.url), isOnline)
            is SubmissionDetailsContentType.TextContent -> TextSubmissionViewFragment.newInstance(type.text, canvasContext.toBaseUrl())
            is SubmissionDetailsContentType.DiscussionContent -> getFragmentWithOnlineCheck(
                DiscussionSubmissionViewFragment.newInstance(type.previewUrl.orEmpty()),
                isOnline
            )
            is SubmissionDetailsContentType.PdfContent -> getFragmentWithOnlineCheck(PdfSubmissionViewFragment.newInstance(type.url, canvasContext.id), isOnline)
            is SubmissionDetailsContentType.ExternalToolContent -> LtiSubmissionViewFragment.newInstance(type)
            is SubmissionDetailsContentType.MediaContent -> getFragmentWithOnlineCheck(MediaSubmissionViewFragment.newInstance(type), isOnline)
            is SubmissionDetailsContentType.OtherAttachmentContent -> ViewUnsupportedFileFragment.newInstance(
                uri = Uri.parse(type.attachment.url),
                displayName = type.attachment.displayName.orEmpty(),
                contentType = type.attachment.contentType.orEmpty(),
                previewUri = type.attachment.previewUrl?.let { Uri.parse(it) },
                fallbackIcon = R.drawable.ic_attachment
            )
            is SubmissionDetailsContentType.ImageContent -> getFragmentWithOnlineCheck(
                ViewImageFragment.newInstance(
                    type.title,
                    Uri.parse(type.url),
                    type.contentType,
                    false
                ), isOnline
            )
            SubmissionDetailsContentType.NoneContent -> SubmissionMessageFragment.newInstance(
                title = R.string.noOnlineSubmissions,
                subtitle = R.string.noneContentMessage
            )
            SubmissionDetailsContentType.OnPaperContent -> SubmissionMessageFragment.newInstance(
                title = R.string.noOnlineSubmissions,
                subtitle = R.string.onPaperContentMessage
            )
            SubmissionDetailsContentType.LockedContent -> SubmissionMessageFragment.newInstance(
                title = R.string.submissionDetailsAssignmentLocked,
                subtitle = R.string.could_not_route_locked
            )
            is SubmissionDetailsContentType.StudentAnnotationContent -> getFragmentWithOnlineCheck(
                AnnotationSubmissionViewFragment.newInstance(
                    type.subissionId,
                    type.submissionAttempt,
                    canvasContext.id
                ), isOnline
            )
            is SubmissionDetailsContentType.UnsupportedContent -> {
                // Users shouldn't get here, but we'll handle the case and send up some analytics if they do
                val bundle = Bundle().apply {
                    putString(AnalyticsParamConstants.DOMAIN_PARAM, ApiPrefs.fullDomain)
                    putString(AnalyticsParamConstants.USER_CONTEXT_ID, ApiPrefs.user?.contextId)
                    putString(AnalyticsParamConstants.CANVAS_CONTEXT_ID, canvasContext.contextId)
                    putLong(AnalyticsParamConstants.ASSIGNMENT_ID, type.assignmentId)
                }

                logEvent(AnalyticsEventConstants.UNSUPPORTED_SUBMISSION_CONTENT, bundle)

                SubmissionMessageFragment.newInstance(
                    title = R.string.noOnlineSubmissions,
                    subtitle = R.string.unsupportedContentMessage
                )
            }
        }
    }

    private fun getFragmentWithOnlineCheck(fragmentIfOnline: Fragment, isOnline: Boolean): Fragment {
        return if (isOnline) {
            fragmentIfOnline
        } else {
            NotAvailableOfflineFragment.newInstance(NotAvailableOfflineFragment.makeRoute(canvasContext, showToolbar = false))
        }
    }

    companion object {
        private const val ANCHOR_POINT = 0.5f
    }
}
