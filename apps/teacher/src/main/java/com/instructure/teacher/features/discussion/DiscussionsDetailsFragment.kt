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
package com.instructure.teacher.features.discussion

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.FullScreenInteractions
import com.instructure.interactions.Identity
import com.instructure.interactions.MasterDetailInteractions
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_DISCUSSION_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.dialogs.AttachmentPickerDialog
import com.instructure.pandautils.discussions.DiscussionCaching
import com.instructure.pandautils.discussions.DiscussionEntryHtmlConverter
import com.instructure.pandautils.discussions.DiscussionUtils
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.R
import com.instructure.teacher.activities.InternalWebViewActivity
import com.instructure.teacher.adapters.StudentContextFragment
import com.instructure.teacher.databinding.FragmentDiscussionsDetailsBinding
import com.instructure.teacher.dialog.NoInternetConnectionDialog
import com.instructure.teacher.events.*
import com.instructure.teacher.events.DiscussionEntryEvent
import com.instructure.teacher.factory.DiscussionsDetailsPresenterFactory
import com.instructure.teacher.features.assignment.submission.AssignmentSubmissionListFragment
import com.instructure.teacher.fragments.*
import com.instructure.teacher.features.assignment.submission.AssignmentSubmissionListPresenter
import com.instructure.teacher.features.assignment.submission.SubmissionListFilter
import com.instructure.teacher.presenters.DiscussionsDetailsPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.*
import com.instructure.teacher.viewinterface.DiscussionsDetailsView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

@PageView(url = "{canvasContext}/{type}/{topicId}")
@ScreenView(SCREEN_VIEW_DISCUSSION_DETAILS)
class DiscussionsDetailsFragment : BasePresenterFragment<
        DiscussionsDetailsPresenter,
        DiscussionsDetailsView,
        FragmentDiscussionsDetailsBinding>(), DiscussionsDetailsView, Identity {

    //region Member Variables
    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var discussionTopicHeader: DiscussionTopicHeader by ParcelableArg(DiscussionTopicHeader(), DISCUSSION_TOPIC_HEADER)
    private var discussionTopic: DiscussionTopic by ParcelableArg(DiscussionTopic(), DISCUSSION_TOPIC)
    private var discussionEntryId: Long by LongArg(0L, DISCUSSION_ENTRY_ID)
    private var discussionTopicHeaderId: Long by LongArg(0L, DISCUSSION_TOPIC_HEADER_ID)
    private var skipIdentityCheck: Boolean by BooleanArg(false, SKIP_IDENTITY_CHECK)
    private var skipId: String by StringArg("", SKIP_ID)

    private var isAnnouncements: Boolean by BooleanArg(false, IS_ANNOUNCEMENT)
    private var isNestedDetail: Boolean by BooleanArg(false, IS_NESTED_DETAIL)

    private var repliesLoadHtmlJob: Job? = null
    private var headerLoadHtmlJob: Job? = null
    private var loadDiscussionJob: WeaveJob? = null

    //endregion

    @Suppress("unused")
    @PageViewUrlParam("topicId")
    private fun getTopicId() = discussionTopicHeader.id

    override val bindingInflater: (layoutInflater: LayoutInflater) -> FragmentDiscussionsDetailsBinding = FragmentDiscussionsDetailsBinding::inflate

    override fun onRefreshFinished() {
        binding.discussionProgressBar.setGone()
    }

    override fun onRefreshStarted() {
        binding.discussionProgressBar.setVisible()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadDiscussionJob?.cancel()
        repliesLoadHtmlJob?.cancel()
        headerLoadHtmlJob?.cancel()
    }

    override val identity: Long? get() = if(discussionTopicHeaderId != 0L) discussionTopicHeaderId else discussionTopicHeader.id
    override val skipCheck: Boolean get() = skipIdentityCheck

    override fun getPresenterFactory() =
            DiscussionsDetailsPresenterFactory(canvasContext, discussionTopicHeader, discussionTopic,
                    if(skipId.isEmpty()) DiscussionsDetailsFragment::class.java.simpleName + UUID.randomUUID().toString() else skipId)

    override fun onPresenterPrepared(presenter: DiscussionsDetailsPresenter) {}

    override fun onReadySetGo(presenter: DiscussionsDetailsPresenter) {

        EventBus.getDefault().getStickyEvent(DiscussionEntryUpdatedEvent::class.java)?.once(javaClass.simpleName) { discussionEntry ->
            presenter.updateDiscussionEntryToDiscussionTopic(discussionEntry)
        }

        val discussionTopicEvent = EventBus.getDefault().getStickyEvent(DiscussionTopicEvent::class.java)

        if(discussionTopicEvent != null) {
             discussionTopicEvent.only(presenter.getSkipId()) { discussionTopic ->
                //A The Discussion Topic was changed in some way. Usually from a nested situation where something was added.
                presenter.updateDiscussionTopic(discussionTopic)
                if (!isNestedDetail) {
                    EventBus.getDefault().removeStickyEvent(discussionTopicEvent)
                }
            }
        } else {
            if (discussionTopicHeaderId == 0L && presenter.discussionTopicHeader.id != 0L) {
                //We were given a valid DiscussionTopicHeader, no need to fetch from the API
                populateDiscussionTopicHeader(presenter.discussionTopicHeader, false)
            } else if (discussionTopicHeaderId != 0L) {
                //results of this GET will call populateDiscussionTopicHeader()
                presenter.getDiscussionTopicHeader(discussionTopicHeaderId)
            }
        }

        EventBus.getDefault().getStickyEvent(DiscussionTopicHeaderDeletedEvent::class.java)?.once(javaClass.simpleName + ".onResume()") {
            if (it == presenter.discussionTopicHeader.id) {
                if (activity is MasterDetailInteractions) {
                    (activity as MasterDetailInteractions).popFragment(canvasContext)
                } else if(activity is FullScreenInteractions) {
                    requireActivity().finish()
                }
            }
        }

        EventBus.getDefault().getStickyEvent(DiscussionEntryEvent::class.java)?.once(javaClass.simpleName) { discussionEntry ->
            presenter.addDiscussionEntryToDiscussionTopic(discussionEntry)
        }
    }

    override fun populateAsForbidden() {
        //TODO: when we add support for students
    }

    override fun populateDiscussionTopicHeader(discussionTopicHeader: DiscussionTopicHeader, forceNetwork: Boolean) = with(binding) {
        if(discussionTopicHeader.assignment != null) {
            setupAssignmentDetails(discussionTopicHeader.assignment!!)
            presenter.getSubmissionData(forceNetwork)
            setupListeners()
        }

        // Publish status if discussion
        if(!isAnnouncements) {
            if (discussionTopicHeader.published) {
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
        } else {
            pointsPublishedLayout.setGone()
            pointsPublishedDivider.root.setGone()
            dueLayoutDivider.root.setGone()
            submissionDivider.root.setGone()
        }

        // If we're getting here by a deep link (like from an email) we don't know that it is an announcement
        // because that field is not included in the api (:frowny_face:) The sections field won't show up for a discussion or
        // an announcement that isn't section specific
        discussionTopicHeader.sections?.joinToString { it.name }?.validOrNull()?.let {
            announcementSection.setVisible().text = it
        }

        loadDiscussionTopicHeader(discussionTopicHeader)
        repliesBack.setVisible(isNestedDetail)
        repliesBack.onClick { requireActivity().onBackPressed() }
        attachmentIcon.setVisible(!discussionTopicHeader.attachments.isEmpty())
        attachmentIcon.onClick {
            val remoteFiles = presenter.discussionTopicHeader.attachments
            if(remoteFiles != null) {
                viewAttachments(remoteFiles)
            }
        }

        if(presenter.discussionTopic.views.isEmpty()) {
            // Loading data will eventually call, upon success, populateDiscussionTopic()
            presenter.loadData(true)
        } else {
            populateDiscussionTopic(discussionTopicHeader, presenter.discussionTopic)
        }
    }

    override fun populateDiscussionTopic(discussionTopicHeader: DiscussionTopicHeader, discussionTopic: DiscussionTopic, topLevelReplyPosted: Boolean) = with(binding) {
        // Check if we have permissions and if we have any discussions to display.

        loadDiscussionJob = tryWeave {

            swipeRefreshLayout.isRefreshing = false

            if(discussionTopic.views.isEmpty() && DiscussionCaching(discussionTopicHeader.id).isEmpty()) {
                // Nothing to display
                discussionRepliesHeaderWrapper.setGone()
                return@tryWeave
            }

            discussionRepliesHeaderWrapper.setVisible()

            val html = inBackground {
                DiscussionUtils.createDiscussionTopicHtml(
                        requireActivity(),
                        isTablet,
                        canvasContext,
                        discussionTopicHeader,
                        discussionTopic.views,
                        discussionEntryId)
            }

            discussionRepliesWebViewWrapper.setInvisible()

            repliesLoadHtmlJob = discussionRepliesWebViewWrapper.webView.loadHtmlWithIframes(requireContext(), html, {formattedHtml ->
                discussionRepliesWebViewWrapper.loadDataWithBaseUrl(CanvasWebView.getReferrer(true), formattedHtml, "text/html", "utf-8", null)
            }) {
                LtiLaunchFragment.routeLtiLaunchFragment(requireActivity(), canvasContext, it)
            }

            delay(300)
            discussionsScrollView.post {
                if (topLevelReplyPosted) {
                    discussionsScrollView.fullScroll(ScrollView.FOCUS_DOWN)
                } else {
                    discussionsScrollView.scrollTo(0, presenter.scrollPosition)
                }
                discussionRepliesWebViewWrapper.setVisible()
            }
        } catch { Logger.e("Error loading discussion " + it.message) }
    }

    private fun setupAssignmentDetails(assignment: Assignment) = with(binding) {
        pointsTextView.setVisible()
        // Points possible
        pointsTextView.text = resources.getQuantityString(
                R.plurals.quantityPointsAbbreviated,
                assignment.pointsPossible.toInt(),
                NumberHelper.formatDecimal(assignment.pointsPossible, 1, true)
        )
        pointsTextView.contentDescription = resources.getQuantityString(
                R.plurals.quantityPointsFull,
                assignment.pointsPossible.toInt(),
                NumberHelper.formatDecimal(assignment.pointsPossible, 1, true))

        dueLayout.setVisible()
        submissionsLayout.setVisible((canvasContext as? Course)?.isDesigner() == false)

        //set these as gone and make them visible if we have data for them
        availabilityLayout.setGone()
        availableFromLayout.setGone()
        availableToLayout.setGone()
        dueForLayout.setGone()
        dueDateLayout.setGone()
        otherDueDateTextView.setGone()

        // Lock status
        val atSeparator = getString(R.string.at)

        val allDates = assignment.allDates
        allDates.singleOrNull()?.apply {
            if (assignment.lockDate?.before(Date()) == true) {
                availabilityLayout.setVisible()
                availabilityTextView.setText(R.string.closed)
            } else {
                availableFromLayout.setVisible()
                availableToLayout.setVisible()
                availableFromTextView.text = if (unlockAt != null)
                    DateHelper.getMonthDayAtTime(requireContext(), unlockDate, atSeparator) else getString(R.string.no_date_filler)
                availableToTextView.text = if (lockAt != null)
                    DateHelper.getMonthDayAtTime(requireContext(), lockDate, atSeparator) else getString(R.string.no_date_filler)
            }
        }

        // Due date(s)
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

    override fun updateSubmissionDonuts(totalStudents: Int, gradedStudents: Int, needsGradingCount: Int, notSubmitted: Int) = with(binding.donutGroup) {
        // Submission section
        gradedChart.setSelected(gradedStudents)
        gradedChart.setTotal(totalStudents)
        gradedChart.setSelectedColor(ThemePrefs.brandColor)
        gradedChart.setCenterText(gradedStudents.toString())
        gradedWrapper.contentDescription = getString(R.string.content_description_submission_donut_graded).format(gradedStudents, totalStudents)
        gradedProgressBar.setGone()
        gradedChart.invalidate()

        ungradedChart.setSelected(needsGradingCount)
        ungradedChart.setTotal(totalStudents)
        ungradedChart.setSelectedColor(ThemePrefs.brandColor)
        ungradedChart.setCenterText(needsGradingCount.toString())
        ungradedLabel.text = requireContext().resources.getQuantityText(R.plurals.needsGradingNoQuantity, needsGradingCount)
        ungradedWrapper.contentDescription = getString(R.string.content_description_submission_donut_needs_grading).format(needsGradingCount, totalStudents)
        ungradedProgressBar.setGone()
        ungradedChart.invalidate()

        notSubmittedChart.setSelected(notSubmitted)
        notSubmittedChart.setTotal(totalStudents)
        notSubmittedChart.setSelectedColor(ThemePrefs.brandColor)
        notSubmittedChart.setCenterText(notSubmitted.toString())
        notSubmittedWrapper.contentDescription = getString(R.string.content_description_submission_donut_unsubmitted).format(notSubmitted, totalStudents)
        notSubmittedProgressBar.setGone()
        notSubmittedChart.invalidate()
    }

    private fun setupListeners() = with(binding) {
        dueLayout.setOnClickListener {
            val args = DueDatesFragment.makeBundle(presenter.discussionTopicHeader.assignment!!)
            RouteMatcher.route(requireActivity(), Route(null, DueDatesFragment::class.java, canvasContext, args))
        }
        submissionsLayout.setOnClickListener {
            navigateToSubmissions(canvasContext, presenter.discussionTopicHeader.assignment!!, SubmissionListFilter.ALL)
        }
        binding.donutGroup.viewAllSubmissions.onClick { submissionsLayout.performClick() } // Separate click listener for a11y
        binding.donutGroup.gradedWrapper.setOnClickListener {
            navigateToSubmissions(canvasContext, presenter.discussionTopicHeader.assignment!!, SubmissionListFilter.GRADED)
        }
        binding.donutGroup.ungradedWrapper.setOnClickListener {
            navigateToSubmissions(canvasContext, presenter.discussionTopicHeader.assignment!!, SubmissionListFilter.NOT_GRADED)
        }
        binding.donutGroup.notSubmittedWrapper.setOnClickListener {
            navigateToSubmissions(canvasContext, presenter.discussionTopicHeader.assignment!!, SubmissionListFilter.MISSING)
        }
    }

    private fun navigateToSubmissions(context: CanvasContext, assignment: Assignment, filter: SubmissionListFilter) {
        val args = AssignmentSubmissionListFragment.makeBundle(assignment, filter)
        RouteMatcher.route(requireActivity(), Route(null, AssignmentSubmissionListFragment::class.java, context, args))
    }

    private fun loadDiscussionTopicHeader(discussionTopicHeader: DiscussionTopicHeader) = with(binding) {
        val displayName = discussionTopicHeader.author?.displayName
        ProfileUtils.loadAvatarForUser(authorAvatar, displayName, discussionTopicHeader.author?.avatarImageUrl)
        authorAvatar.setupAvatarA11y(discussionTopicHeader.author?.displayName)
        authorAvatar.onClick {
            val bundle = StudentContextFragment.makeBundle(discussionTopicHeader.author?.id ?: 0, canvasContext.id)
            RouteMatcher.route(requireActivity(), Route(StudentContextFragment::class.java, null, bundle))
        }
        authorName?.text = discussionTopicHeader.author?.let { Pronouns.span(it.displayName, it.pronouns) }
        authoredDate?.text = DateHelper.getMonthDayAtTime(requireContext(), discussionTopicHeader.postedDate, getString(R.string.at))
        discussionTopicTitle?.text = discussionTopicHeader.title

        replyToDiscussionTopic.setTextColor(ThemePrefs.textButtonColor)
        replyToDiscussionTopic.setVisible(discussionTopicHeader.permissions!!.reply)
        replyToDiscussionTopic.onClick {
            showReplyView(presenter.discussionTopicHeader.id)
        }

        headerLoadHtmlJob = discussionTopicHeaderWebViewWrapper.webView.loadHtmlWithIframes(requireContext(), discussionTopicHeader.message, {
            discussionTopicHeaderWebViewWrapper.loadHtml(it, discussionTopicHeader.title, baseUrl = this@DiscussionsDetailsFragment.discussionTopicHeader.htmlUrl)
        }) {
            LtiLaunchFragment.routeLtiLaunchFragment(requireActivity(), canvasContext, it)
        }

        discussionRepliesWebViewWrapper.loadHtml("", "")
    }

    override fun onPause() = with(binding) {
        super.onPause()
        presenter.scrollPosition = discussionsScrollView.scrollY
        discussionTopicHeaderWebViewWrapper.webView.onPause()
        discussionRepliesWebViewWrapper.webView.onPause()
    }

    override fun onResume() = with(binding) {
        super.onResume()
        setupToolbar()

        if (isAccessibilityEnabled(requireContext()) && discussionTopicHeader.htmlUrl != null) {
            alternateViewButton.visibility = View.VISIBLE
            alternateViewButton.setOnClickListener {
                val bundle = InternalWebViewFragment.makeBundle(
                    discussionTopicHeader.htmlUrl!!,
                    discussionTopicHeader.title!!,
                    shouldAuthenticate = true
                )
                RouteMatcher.route(requireActivity(), Route(null, InternalWebViewFragment::class.java, canvasContext, bundle))
            }
        }

        swipeRefreshLayout.setOnRefreshListener {
            presenter.loadData(true)
            presenter.getSubmissionData(true)

            // Send out bus events to trigger a refresh for discussion list and submission list
            DiscussionUpdatedEvent(presenter.discussionTopicHeader, javaClass.simpleName).post()
            presenter.discussionTopicHeader.assignment?.let {
                AssignmentGradedEvent(it.id, javaClass.simpleName).post()
            }
        }

        discussionTopicHeaderWebViewWrapper.webView.onResume()
        discussionRepliesWebViewWrapper.webView.onResume()

        setupWebView(discussionTopicHeaderWebViewWrapper.webView, false)
        setupWebView(discussionRepliesWebViewWrapper.webView, true, addDarkTheme = !discussionRepliesWebViewWrapper.themeSwitched)
        discussionRepliesWebViewWrapper.onThemeChanged = { themeChanged, html ->
            setupWebView(discussionRepliesWebViewWrapper.webView, true, addDarkTheme = !themeChanged)
            discussionRepliesWebViewWrapper.loadDataWithBaseUrl(CanvasWebView.getReferrer(true), html, "text/html", "UTF-8", null)
        }
    }

    private fun setupToolbar() = with(binding) {
        toolbar.setupBackButtonWithExpandCollapseAndBack(this@DiscussionsDetailsFragment) {
            toolbar.updateToolbarExpandCollapseIcon(this@DiscussionsDetailsFragment)
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, canvasContext.backgroundColor, requireContext().getColor(R.color.white))
            (activity as MasterDetailInteractions).toggleExpandCollapse()
        }
        toolbar.setupMenu(R.menu.menu_edit_generic, menuItemCallback)
        toolbar.title = if(isAnnouncements) getString(R.string.announcementDetails) else getString(R.string.discussion_details)
        if(!isTablet) {
            toolbar.subtitle = canvasContext.name
        }
        ViewStyler.themeToolbarColored(requireActivity(), toolbar, canvasContext.backgroundColor, requireContext().getColor(R.color.white))
    }

    private val menuItemCallback: (MenuItem) -> Unit = { item ->
        when (item.itemId) {
            R.id.menu_edit -> {
                if(APIHelper.hasNetworkConnection()) {
                    if(isAnnouncements) {
                        val args = CreateOrEditAnnouncementFragment.newInstanceEdit(presenter.canvasContext, presenter.discussionTopicHeader).nonNullArgs
                        RouteMatcher.route(requireActivity(), Route(CreateOrEditAnnouncementFragment::class.java, null, args))
                    } else {
                        // If we have an assignment, set the topic header to null to prevent cyclic reference
                        presenter.discussionTopicHeader.assignment?.discussionTopicHeader = null
                        val args = CreateDiscussionFragment.makeBundle(presenter.canvasContext, presenter.discussionTopicHeader)
                        RouteMatcher.route(requireActivity(), Route(CreateDiscussionFragment::class.java, canvasContext, args))
                    }
                } else {
                    NoInternetConnectionDialog.show(requireFragmentManager())
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(webView: CanvasWebView, addJSSupport: Boolean, addDarkTheme: Boolean = false) {
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        val backgroundColorRes = if (addDarkTheme) R.color.backgroundLightest else R.color.white
        webView.setBackgroundColor(requireContext().getColor(backgroundColorRes))
        webView.settings.javaScriptEnabled = true
        if(addJSSupport) webView.addJavascriptInterface(JSDiscussionInterface(), "accessor")
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        CookieManager.getInstance().acceptThirdPartyCookies(webView)
        webView.canvasWebViewClientCallback = object: CanvasWebView.CanvasWebViewClientCallback {
            override fun routeInternallyCallback(url: String) {
                if (!RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, true)) {
                    val bundle = InternalWebViewFragment.makeBundle(url, url, false, "")
                    RouteMatcher.route(requireActivity(), Route(FullscreenInternalWebViewFragment::class.java,
                        presenter.canvasContext, bundle))
                }
            }
            override fun canRouteInternallyDelegate(url: String): Boolean {
                return true
            }
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                showToast(R.string.downloadingFile)
                RouteMatcher.openMedia(activity, url)
            }
            override fun onPageStartedCallback(webView: WebView, url: String) = Unit
            override fun onPageFinishedCallback(webView: WebView, url: String) = Unit
        }

        webView.addVideoClient(requireActivity())
    }

    @Suppress("unused")
    private inner class JSDiscussionInterface {

        @Suppress("UNUSED_PARAMETER")
        @JavascriptInterface
        fun onItemPressed(id: String) {
            //do nothing for now
        }

        @JavascriptInterface
        fun onAvatarPressed(id: String) {
            presenter.findEntry(id.toLong())?.let { entry ->
                val bundle = StudentContextFragment.makeBundle(entry.author!!.id, canvasContext.id)
                RouteMatcher.route(requireActivity(), Route(StudentContextFragment::class.java, null, bundle))
            }
        }

        @JavascriptInterface
        fun onAttachmentPressed(id: String) {
            val entry = presenter.findEntry(id.toLong())
            if(entry != null && !entry.attachments.isNullOrEmpty()) {
                viewAttachments(entry.attachments!!)
            }
        }

        @JavascriptInterface
        fun onReplyPressed(id: String) {
            showReplyView(id.toLong())
        }

        @JavascriptInterface
        fun onMenuPressed(id: String) {
            showOverflowMenu(id.toLong())
        }

        @JavascriptInterface
        fun onLikePressed(id: String) {
            presenter.likeDiscussionPressed(id.toLong())
        }

        @JavascriptInterface
        fun onMoreRepliesPressed(id: String) {
            val args = makeBundle(presenter.discussionTopicHeader, presenter.discussionTopic, id.toLong(), presenter.getSkipId())
            RouteMatcher.route(requireActivity(), Route(null, DiscussionsDetailsFragment::class.java, canvasContext, args))
        }

        @JavascriptInterface
        fun getInViewPort(): String {
            return presenter.discussionTopic.unreadEntries.joinToString()
        }

        @JavascriptInterface
        fun inViewPortAndUnread(idList: String) {
            if(idList.isNotEmpty()) {
                presenter.markAsRead(idList.split(",").map(String::toLong))
            }
        }

        @JavascriptInterface
        fun getLikedImage(): String {
            //Returns a string of a bitmap colored for the thumbs up (like) image.
            val likeImage = DiscussionUtils.getBitmapFromAssets(requireContext(), "discussion_liked.png")
            return DiscussionUtils.makeBitmapForWebView(ThemePrefs.brandColor, likeImage)
        }

        //A helper to log out messages from the JS code
        @JavascriptInterface
        fun logMessage(message: String) { Logger.d(message) }

        /**
         * Calculates the offset of the scrollview and it's content as compared to the elements position within the webview.
         * A scrollview's visible window can be between 0 and the size of the scrollview's height. This looks at the content on top
         * of the discussion replies webview and adds that to the elements position to come up with a relative position for the element
         * within the scrollview. In sort we are finding the elements position within a scrollview.
         */
        @Suppress("UNUSED_PARAMETER")
        @JavascriptInterface
        fun calculateActualOffset(elementId: String, elementHeight: String, elementTopOffset: String): Boolean {
            // Javascript passes us back a number, which could be either a float or an int, so we'll need to convert the string first to a float, then an int
            return isElementInViewPortWithinScrollView(elementHeight.toFloat().toInt(), elementTopOffset.toFloat().toInt())
        }
    }

    override fun updateDiscussionLiked(discussionEntry: DiscussionEntry) {
        updateDiscussionLikedState(discussionEntry, "setLiked"/*Constant found in the JS files*/)
    }

    override fun updateDiscussionUnliked(discussionEntry: DiscussionEntry) {
        updateDiscussionLikedState(discussionEntry, "setUnliked" /*Constant found in the JS files*/)
    }

    private fun updateDiscussionLikedState(discussionEntry: DiscussionEntry, methodName: String) = with(binding) {
        val likingSum = if(discussionEntry.ratingSum == 0) "" else "(" + discussionEntry.ratingSum + ")"
        val likingSumAllyText = DiscussionEntryHtmlConverter.getLikeCountText(requireContext(), discussionEntry)
        val likingColor = DiscussionUtils.getHexColorString(if (discussionEntry._hasRated) ThemePrefs.brandColor else ContextCompat.getColor(requireContext(), R.color.textDark))
        requireActivity().runOnUiThread {
            discussionRepliesWebViewWrapper.webView.loadUrl("javascript:$methodName('${discussionEntry.id}')")
            discussionRepliesWebViewWrapper.webView.loadUrl("javascript:updateLikedCount('${discussionEntry.id}','$likingSum','$likingColor','$likingSumAllyText')")
        }
    }

    override fun updateDiscussionEntry(discussionEntry: DiscussionEntry) = with(binding) {
        requireActivity().runOnUiThread {
            discussionRepliesWebViewWrapper.webView.loadUrl("javascript:updateEntry('${discussionEntry.id}', '${discussionEntry.message}')")
            if (discussionEntry.attachments == null)
                discussionRepliesWebViewWrapper.webView.loadUrl("javascript:hideAttachmentIcon('${discussionEntry.id}'")
        }
    }

    private fun showReplyView(id: Long) {
        if (APIHelper.hasNetworkConnection()) {
            val args = DiscussionsReplyFragment.makeBundle(presenter.discussionTopicHeader.id, id, isAnnouncements)
            RouteMatcher.route(requireActivity(), Route(DiscussionsReplyFragment::class.java, presenter.canvasContext, args))
        } else {
            NoInternetConnectionDialog.show(requireFragmentManager())
        }
    }

    private fun markAsUnread(id: Long) {
        if (APIHelper.hasNetworkConnection()) {
            presenter.markAsUnread(id)
        } else {
            NoInternetConnectionDialog.show(requireFragmentManager())
        }
    }

    private fun showOverflowMenu(id: Long) {
        parentFragmentManager.let {
            DiscussionBottomSheetMenuFragment.show(it, id)
        }
    }

    private fun showUpdateReplyView(id: Long) {
        if (APIHelper.hasNetworkConnection()) {
            val args = DiscussionsUpdateFragment.makeBundle(presenter.discussionTopicHeader.id, presenter.findEntry(id), isAnnouncements, presenter.discussionTopic)
            RouteMatcher.route(requireActivity(), Route(DiscussionsUpdateFragment::class.java, presenter.canvasContext, args))
        } else {
            NoInternetConnectionDialog.show(requireFragmentManager())
        }
    }

    private fun deleteDiscussionEntry(id: Long) {
        if (APIHelper.hasNetworkConnection()) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage(R.string.discussions_delete_warning)
            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                presenter.deleteDiscussionEntry(id)
            }
            builder.setNegativeButton(android.R.string.cancel) { _, _ -> }
            val dialog = builder.create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
            }
            dialog.show()
        } else {
            NoInternetConnectionDialog.show(requireFragmentManager())
        }
    }

    override fun updateDiscussionAsDeleted(discussionEntry: DiscussionEntry) {
        val deletedText = DiscussionUtils.formatDeletedInfoText(requireContext(), discussionEntry)
        binding.discussionRepliesWebViewWrapper.post { binding.discussionRepliesWebViewWrapper.webView.loadUrl(
                "javascript:markAsDeleted" + "('" + discussionEntry.id.toString() + "','" + deletedText + "')") }
    }

    override fun updateDiscussionsMarkedAsReadCompleted(markedAsReadIds: List<Long>) {
        markedAsReadIds.forEach {
            binding.discussionRepliesWebViewWrapper.post { binding.discussionRepliesWebViewWrapper.webView.loadUrl("javascript:markAsRead('$it')") }
        }
    }

    override fun updateDiscussionsMarkedAsUnreadCompleted(markedAsUnreadId: Long) {
        binding.discussionRepliesWebViewWrapper.post { binding.discussionRepliesWebViewWrapper.webView.loadUrl("javascript:markAsUnread('$markedAsUnreadId')") }
    }

    override fun showAnonymousDiscussionView() = with(binding) {
        anonymousDiscussionsNotSupported.setVisible()
        openInBrowser.setVisible(discussionTopicHeader.htmlUrl?.isNotEmpty() == true)
        replyToDiscussionTopic.setGone()
        swipeRefreshLayout.isEnabled = false
        openInBrowser.onClick {
            discussionTopicHeader.htmlUrl?.let { url ->
                requireContext().startActivity(InternalWebViewActivity.createIntent(requireContext(), url, "", true))
            }
        }
    }

    /**
     * Checks to see if the webview element is within the viewable bounds of the scrollview.
     */
    private fun isElementInViewPortWithinScrollView(elementHeight: Int, topOffset: Int): Boolean = with(binding) {
        val scrollBounds = Rect().apply { discussionsScrollView.getDrawingRect(this) }

        val discussionRepliesHeight = discussionRepliesWebViewWrapper.height
        val discussionScrollViewContentHeight = discussionsScrollViewContentWrapper.height
        val otherContentHeight = discussionScrollViewContentHeight - discussionRepliesHeight
        val top = requireContext().DP(topOffset) + otherContentHeight
        val bottom = top + requireContext().DP(elementHeight)

        return scrollBounds.top < top && scrollBounds.bottom > bottom
    }

    private fun viewAttachments(remoteFiles: List<RemoteFile>) {
        val attachments = ArrayList<Attachment>()
        remoteFiles.forEach { attachments.add(it.mapToAttachment()) }
        if (attachments.isNotEmpty()) {
            if (attachments.size > 1) {
                AttachmentPickerDialog.show(requireFragmentManager(), attachments) { attachment ->
                    AttachmentPickerDialog.hide(requireFragmentManager())
                    attachment.view(requireActivity())
                }
            } else {
                attachments[0].view(requireActivity())
            }
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDiscussionUpdated(event: DiscussionUpdatedEvent) {
        event.once(javaClass.simpleName) {
            presenter.discussionTopicHeader = it
            populateDiscussionTopicHeader(it, false)
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDiscussionTopicHeaderDeleted(event: DiscussionTopicHeaderDeletedEvent) {
        // Depending on the device and where we delete the discussion topic header from we handle this in two places.
        // This situation handles when we delete from discussions list, the other found in readySetGo handles the create discussion fragment.
        event.once(javaClass.simpleName + ".onPost()") {
            if (it == presenter.discussionTopicHeader.id) {
                if(activity is MasterDetailInteractions) {
                    (activity as MasterDetailInteractions).popFragment(canvasContext)
                }
            } else if(activity is FullScreenInteractions) {
                requireActivity().finish()
            }
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onOverFlowMenuClicked(event: DiscussionOverflowMenuClickedEvent) {
        val id = event.entryId
        when(event.type) {
            DiscussionBottomSheetChoice.MARK_AS_UNREAD -> markAsUnread(id)
            DiscussionBottomSheetChoice.EDIT -> showUpdateReplyView(id)
            DiscussionBottomSheetChoice.DELETE -> deleteDiscussionEntry(id)
        }
    }

    @PageViewUrlParam("type")
    fun pageViewType(): String = if (isAnnouncements) "announcements" else "discussion_topics"

    companion object {
        const val DISCUSSION_TOPIC_HEADER = "discussion_topic_header"
        const val DISCUSSION_TOPIC_HEADER_ID = "discussion_topic_header_id"
        const val DISCUSSION_TOPIC = "discussion_topic"
        const val DISCUSSION_ENTRY_ID = "discussion_entry_id"
        private const val SKIP_IDENTITY_CHECK = "skip_identity_check"
        private const val IS_NESTED_DETAIL = "is_nested_detail"
        private const val SKIP_ID = "skipId"
        private const val IS_ANNOUNCEMENT = "is_announcement"

        @JvmStatic fun makeBundle(discussionTopicHeader: DiscussionTopicHeader): Bundle = Bundle().apply {
            putParcelable(DISCUSSION_TOPIC_HEADER, discussionTopicHeader)
        }

        @JvmStatic fun makeBundle(discussionTopicHeader: DiscussionTopicHeader, isAnnouncement: Boolean): Bundle = Bundle().apply {
            putParcelable(DISCUSSION_TOPIC_HEADER, discussionTopicHeader)
            putBoolean(IS_ANNOUNCEMENT, isAnnouncement)
        }

        @JvmStatic fun makeBundle(discussionTopicHeaderId: Long): Bundle = Bundle().apply {
            putLong(DISCUSSION_TOPIC_HEADER_ID, discussionTopicHeaderId)
        }

        @JvmStatic fun makeBundle(discussionTopicHeaderId: Long, entryId: Long): Bundle = Bundle().apply {
            putLong(DISCUSSION_TOPIC_HEADER_ID, discussionTopicHeaderId)
            putLong(DISCUSSION_ENTRY_ID, entryId)
        }

        @JvmStatic fun makeBundle(
                discussionTopicHeader: DiscussionTopicHeader,
                discussionTopic: DiscussionTopic,
                discussionEntryId: Long,
                skipId: String): Bundle = Bundle().apply {

            // Used for viewing more entries, beyond the default nesting
            putParcelable(DISCUSSION_TOPIC_HEADER, discussionTopicHeader)
            putParcelable(DISCUSSION_TOPIC, discussionTopic)
            putLong(DISCUSSION_ENTRY_ID, discussionEntryId)
            putBoolean(SKIP_IDENTITY_CHECK, true)
            putBoolean(IS_NESTED_DETAIL, true)
            putString(SKIP_ID, skipId)
        }

        @JvmStatic fun newInstance(canvasContext: CanvasContext, args: Bundle) = DiscussionsDetailsFragment().withArgs(args).apply {
            this.canvasContext = canvasContext
        }
    }
}
