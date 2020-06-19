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

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.tabs.TabLayout
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.AnalyticsParamConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.pandautils.activities.BaseViewMediaActivity
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.RecordingMediaType
import com.instructure.student.R
import com.instructure.student.fragment.LTIWebViewFragment
import com.instructure.student.fragment.ViewUnsupportedFileFragment
import com.instructure.student.fragment.ViewImageFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsContentType
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.*
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionMessageFragment
import com.instructure.student.mobius.common.ui.MobiusView
import com.instructure.student.router.RouteMatcher
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_submission_details.*
import java.io.File

class SubmissionDetailsView(
    layoutInflater: LayoutInflater,
    parent: ViewGroup,
    private val canvasContext: CanvasContext,
    private val fragmentManager: FragmentManager
) : MobiusView<SubmissionDetailsViewState, SubmissionDetailsEvent>(
    R.layout.fragment_submission_details,
    layoutInflater,
    parent
) {

    private var drawerPagerAdapter = SubmissionDetailsDrawerPagerAdapter(fragmentManager)

    /* Tab selection listener for the drawer ViewPager */
    private val drawerTabLayoutListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
        override fun onTabReselected(tab: TabLayout.Tab?) = onTabSelected(tab)
        override fun onTabSelected(tab: TabLayout.Tab?) {
            if (slidingUpPanelLayout?.panelState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                slidingUpPanelLayout?.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
            }
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
        toolbar.setupAsBackButton { (context as? Activity)?.onBackPressed() }
        retryButton.onClick { consumer?.accept(SubmissionDetailsEvent.RefreshRequested) }
        drawerViewPager.offscreenPageLimit = 3
        drawerViewPager.adapter = drawerPagerAdapter
        configureDrawerTabLayout()
        configureSlidingPanelHeight()
    }

    private fun configureDrawerTabLayout() {
        drawerTabLayout.setupWithViewPager(drawerViewPager)

        // Tint the tab with the course color
        val tint = canvasContext.color
        drawerTabLayout.setSelectedTabIndicatorColor(tint)
        drawerTabLayout.setTabTextColors(ContextCompat.getColor(context, R.color.defaultTextDark), tint)

        // Use 90% luminance to ensure a 'light' ripple effect that doesn't overpower the tab text
        val rippleTint = tint.withLuminance(0.90f).asStateList()
        drawerTabLayout.tabRippleColor =  rippleTint
    }

    private fun configureSlidingPanelHeight() {
        /* Adjusts the panel content height based on the position of the sliding portion of the view, but only if
         * it is at (or has passed) the anchor point. */
        slidingUpPanelLayout.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelStateChanged(
                panel: View?,
                previousState: SlidingUpPanelLayout.PanelState?,
                newState: SlidingUpPanelLayout.PanelState?
            ) = Unit

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
        ViewStyler.themeToolbar(context as Activity, toolbar, canvasContext)
    }

    override fun onConnect(output: Consumer<SubmissionDetailsEvent>) {
        consumer = output
    }

    override fun onDispose() {
        consumer = null
    }

    override fun render(state: SubmissionDetailsViewState) {
        // Reset visibilities
        errorContainer.setGone()
        slidingUpPanelLayout.setGone()
        loadingView.setGone()

        when (state) {
            SubmissionDetailsViewState.Error -> errorContainer.setVisible()
            SubmissionDetailsViewState.Loading -> loadingView.setVisible()
            is SubmissionDetailsViewState.Loaded -> renderLoadedState(state)
        }
    }

    private fun renderLoadedState(state: SubmissionDetailsViewState.Loaded) {
        slidingUpPanelLayout.setVisible()
        submissionVersionsSpinner.setVisible(state.showVersionsSpinner)
        setupSubmissionVersionSpinner(state.submissionVersions, state.selectedVersionSpinnerIndex)
        updateDrawerPager(state.tabData)
    }

    private fun updateDrawerPager(tabData: List<SubmissionDetailsTabData>) {
        /* Updating the pager adapter's data can cause the current tab to be reselected, erroneously causing the drawer
        to open up to the anchor point if it was previously closed. As a workaround we remove the tab selection
        listener temporarily, and then restore it after updating the adapter */
        drawerTabLayout.removeOnTabSelectedListener(drawerTabLayoutListener)

        // Update adapter data
        drawerPagerAdapter.tabData = tabData
        drawerPagerAdapter.notifyDataSetChanged()

        // Restore tab selection listener
        drawerTabLayout.addOnTabSelectedListener(drawerTabLayoutListener)
    }


    private fun setupSubmissionVersionSpinner(submissions: List<Pair<Long, String>>, selectedIdx: Int) {
        submissionVersionsSpinner.adapter =
            ArrayAdapter(context, R.layout.spinner_submission_versions, submissions.map { it.second }).apply {
                setDropDownViewResource(R.layout.spinner_submission_versions_dropdown)
            }
        submissionVersionsSpinner.setSelection(selectedIdx, false)
        submissionVersionsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val submissionAttempt = submissions[position].first
                consumer?.accept(SubmissionDetailsEvent.SubmissionClicked(submissionAttempt))
            }
        }
    }

    fun showSubmissionContent(type: SubmissionDetailsContentType) {
        fragmentManager.beginTransaction().apply {
            replace(R.id.submissionContent, getFragmentForContent(type))
            commitAllowingStateLoss()
        }
    }

    fun showAudioRecordingView() {
        floatingRecordingView.setContentType(RecordingMediaType.Audio)
        floatingRecordingView.setVisible()
        floatingRecordingView.stoppedCallback = {
            consumer?.accept(SubmissionDetailsEvent.StopMediaRecordingClicked)
        }
        floatingRecordingView.recordingCallback = { file ->
            consumer?.accept(SubmissionDetailsEvent.SendMediaCommentClicked(file))
        }
    }

    fun showVideoRecordingView() {
        floatingRecordingView.setContentType(RecordingMediaType.Video)
        floatingRecordingView.startVideoView()
        floatingRecordingView.recordingCallback = { file ->
            consumer?.accept(SubmissionDetailsEvent.SendMediaCommentClicked(file))
        }
        floatingRecordingView.stoppedCallback = {
            consumer?.accept(SubmissionDetailsEvent.StopMediaRecordingClicked)
        }
        floatingRecordingView.replayCallback = { file ->
            consumer?.accept(SubmissionDetailsEvent.VideoRecordingReplayClicked(file))
        }
    }

    fun showVideoRecordingPlayback(file: File) {
        val bundle = BaseViewMediaActivity.makeBundle(file, "video", context.getString(R.string.videoCommentReplay), true)
        RouteMatcher.route(context, Route(bundle, RouteContext.MEDIA))
    }

    fun showVideoRecordingPlaybackError() {
        Toast.makeText(context, R.string.errorShowingVideoReplay, Toast.LENGTH_SHORT).show()
    }

    fun showMediaCommentError() {
        Toast.makeText(context, R.string.errorSubmittingMediaComment, Toast.LENGTH_SHORT).show()
    }

    private fun getFragmentForContent(type: SubmissionDetailsContentType): Fragment {
        return when (type) {
            is SubmissionDetailsContentType.NoSubmissionContent -> SubmissionDetailsEmptyContentFragment.newInstance(type.canvasContext as Course, type.assignment, type.isStudioEnabled, type.quiz, type.studioLTITool, type.isObserver)
            is SubmissionDetailsContentType.UrlContent -> UrlSubmissionViewFragment.newInstance(type.url, type.previewUrl)
            is SubmissionDetailsContentType.QuizContent -> QuizSubmissionViewFragment.newInstance(type.url)
            is SubmissionDetailsContentType.TextContent -> TextSubmissionViewFragment.newInstance(type.text)
            is SubmissionDetailsContentType.DiscussionContent -> DiscussionSubmissionViewFragment.newInstance(type.previewUrl ?: "")
            is SubmissionDetailsContentType.PdfContent -> PdfSubmissionViewFragment.newInstance(type.url)
            is SubmissionDetailsContentType.ExternalToolContent -> LTIWebViewFragment.newInstance(LTIWebViewFragment.makeRoute(type.canvasContext, type.url, hideToolbar = true))!!
            is SubmissionDetailsContentType.MediaContent -> MediaSubmissionViewFragment.newInstance(type)
            is SubmissionDetailsContentType.OtherAttachmentContent -> ViewUnsupportedFileFragment.newInstance(
                uri = Uri.parse(type.attachment.url),
                displayName = type.attachment.displayName ?: "",
                contentType = type.attachment.contentType ?: "",
                previewUri = type.attachment.previewUrl?.let { Uri.parse(it) },
                fallbackIcon = R.drawable.vd_attachment
            )
            is SubmissionDetailsContentType.ImageContent -> ViewImageFragment.newInstance(type.title, Uri.parse(type.url), type.contentType, false)
            SubmissionDetailsContentType.NoneContent -> SubmissionMessageFragment.newInstance(title = R.string.noOnlineSubmissions,  subtitle = R.string.noneContentMessage)
            SubmissionDetailsContentType.OnPaperContent -> SubmissionMessageFragment.newInstance(title = R.string.noOnlineSubmissions, subtitle = R.string.onPaperContentMessage)
            SubmissionDetailsContentType.LockedContent -> SubmissionMessageFragment.newInstance(title = R.string.submissionDetailsAssignmentLocked, subtitle = R.string.could_not_route_locked)
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

    companion object {
        private const val ANCHOR_POINT = 0.5f
    }
}
