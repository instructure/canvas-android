/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.student.features.discussion.details

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.RemoteFile
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.mapToAttachment
import com.instructure.canvasapi2.utils.pageview.BeforePageView
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.canvasapi2.utils.pageview.PageViewUrlQuery
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.loginapi.login.dialog.NoInternetConnectionDialog
import com.instructure.pandautils.analytics.SCREEN_VIEW_DISCUSSION_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.discussions.DiscussionCaching
import com.instructure.pandautils.discussions.DiscussionEntryHtmlConverter
import com.instructure.pandautils.discussions.DiscussionUtils
import com.instructure.pandautils.features.lti.LtiLaunchFragment
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.DiscussionEntryEvent
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.NullableStringArg
import com.instructure.pandautils.utils.OnBackStackChangedEvent
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ProfileUtils
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.getModuleItemId
import com.instructure.pandautils.utils.isAccessibilityEnabled
import com.instructure.pandautils.utils.isGroup
import com.instructure.pandautils.utils.isTablet
import com.instructure.pandautils.utils.loadHtmlWithIframes
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setInvisible
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.setupAvatarA11y
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.BuildConfig
import com.instructure.student.R
import com.instructure.student.databinding.FragmentDiscussionsDetailsBinding
import com.instructure.student.events.DiscussionTopicHeaderEvent
import com.instructure.student.events.DiscussionUpdatedEvent
import com.instructure.student.events.ModuleUpdatedEvent
import com.instructure.student.events.post
import com.instructure.student.features.modules.progression.CourseModuleProgressionFragment
import com.instructure.student.fragment.DiscussionsReplyFragment
import com.instructure.student.fragment.DiscussionsUpdateFragment
import com.instructure.student.fragment.InternalWebviewFragment
import com.instructure.student.fragment.ParentFragment
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.Const
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.net.URLDecoder
import java.util.Date
import java.util.regex.Pattern
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_DISCUSSION_DETAILS)
@PageView(url = "{canvasContext}/discussion_topics/{topicId}")
@AndroidEntryPoint
class DiscussionDetailsFragment : ParentFragment(), Bookmarkable {

    private val binding by viewBinding(FragmentDiscussionsDetailsBinding::bind)

    @Inject
    lateinit var repository: DiscussionDetailsRepository

    // Bundle args
    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var discussionTopic: DiscussionTopic? by NullableParcelableArg(key = DISCUSSION_TOPIC)
    private var discussionTopicHeader: DiscussionTopicHeader by ParcelableArg(default = DiscussionTopicHeader(), key = DISCUSSION_TOPIC_HEADER)
    private var discussionTopicHeaderId: Long by LongArg(default = 0L, key = DISCUSSION_TOPIC_HEADER_ID)
    private var discussionTitle: String? by NullableStringArg(key = DISCUSSION_TITLE)
    private var discussionEntryId: Long by LongArg(default = 0L, key = DISCUSSION_ENTRY_ID)
    private var isNestedDetail: Boolean by BooleanArg(default = false, key = IS_NESTED_DETAIL)
    private val groupDiscussion: Boolean by BooleanArg(default = false, key = GROUP_DISCUSSION)

    private var courseSettings: CourseSettings? = null

    private var scrollPosition: Int = 0
    private var authenticatedSessionURL: String? = null

    private var markAsReadJob: Job? = null

    //region Analytics
    @Suppress("unused")
    @PageViewUrlParam("topicId")
    private fun getTopicId() = discussionTopicHeader.id

    @Suppress("unused")
    @PageViewUrlQuery("module_item_id")
    private fun pageViewModuleItemId() = getModuleItemId()
    //endregion

    //region Fragment Lifecycle Overrides
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            layoutInflater.inflate(R.layout.fragment_discussions_details, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        populateDiscussionData()
        binding.swipeRefreshLayout.setOnRefreshListener {
            authenticatedSessionURL = null
            populateDiscussionData()
            // Send out bus events to trigger a refresh for discussion list
            DiscussionUpdatedEvent(discussionTopicHeader, javaClass.simpleName).post()
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onResume() {
        super.onResume()
        binding.discussionTopicHeaderWebViewWrapper.webView.onResume()
        binding.discussionRepliesWebViewWrapper.webView.onResume()
        addAccessibilityButton()

        /* TODO - Comms - 868
        EventBus.getDefault().getStickyEvent(DiscussionTopicHeaderDeletedEvent::class.java)?.once(javaClass.simpleName + ".onResume()") {
            if (it == presenter.discussionTopicHeader.id) {
                if (activity is MasterDetailInteractions) {
                    (activity as MasterDetailInteractions).popFragment(mCanvasContext)
                } else if(activity is FullScreenInteractions) {
                    requireActivity().finish()
                }
            }
        }
        */
    }

    override fun onPause() = with(binding) {
        super.onPause()
        scrollPosition = discussionsScrollView.scrollY
        discussionTopicHeaderWebViewWrapper.webView.onPause()
        discussionRepliesWebViewWrapper.webView.onPause()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    //endregion

    //region Fragment Interaction Overrides
    override fun title(): String = discussionTitle ?: if (discussionTopicHeaderId == 0L) discussionTopicHeader.title
            ?: getString(R.string.discussion) else getString(R.string.discussion)

    override fun applyTheme() {
        with (binding) {
            toolbar.title = title()
            setupToolbarMenu(toolbar)
            toolbar.setupAsBackButton(this@DiscussionDetailsFragment)
            /* TODO - Blocked by COMMS - 868
        if(!isAnnouncements && discussionTopicHeader.author.id == ApiPrefs.user?.id && hasEditPermissions) {
            toolbar.setMenu(R.menu.menu_edit_generic, menuItemCallback)
        }
        */
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, canvasContext)
        }
    }

    //endregion

    override val bookmark: Bookmarker
        get() = Bookmarker(!isNestedDetail, canvasContext).withParam(RouterParams.MESSAGE_ID, discussionTopicHeader.id.toString())

    //region Fragment Functionality
    //region Discussion Actions

    private fun viewAttachments(remoteFiles: List<RemoteFile>) {
        // Only one file can be attached to a discussion
        val remoteFile = remoteFiles.firstOrNull() ?: return

        // Show lock message if file is locked
        if (remoteFile.lockedForUser) {
            if (remoteFile.lockExplanation.isValid()) {
                Snackbar.make(
                    requireView(),
                    remoteFile.lockExplanation!!,
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.fileCurrentlyLocked,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        // Show attachment
        val attachment = remoteFile.mapToAttachment()
        openMedia(attachment.contentType, attachment.url, attachment.filename, attachment.id.toString(), canvasContext, localFile = attachment.isLocalFile)
    }

    private fun showReplyView(discussionEntryId: Long) {
        if (repository.isOnline()) {
            scrollPosition = binding.discussionsScrollView.scrollY
            val route = DiscussionsReplyFragment.makeRoute(
                canvasContext,
                discussionTopicHeader.id,
                discussionEntryId,
                discussionTopicHeader.permissions!!.attach
            )
            RouteMatcher.route(requireActivity(), route)
        } else {
            NoInternetConnectionDialog.show(requireFragmentManager())
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun markAsRead(discussionEntryIds: List<Long>) {
        if (markAsReadJob?.isActive == true) return
        markAsReadJob = lifecycleScope.tryLaunch {
            repository.markAsRead(canvasContext, discussionTopicHeader.id, discussionEntryIds).forEach { entryId ->
                discussionTopic?.let {
                    val entry = DiscussionUtils.findEntry(entryId, it.views)
                    entry?.unread = false
                    it.unreadEntriesMap.remove(entryId)
                    it.unreadEntries.remove(entryId)
                    if (discussionTopicHeader.unreadCount > 0) discussionTopicHeader.unreadCount -= 1
                }

                binding.discussionRepliesWebViewWrapper.post {
                    // Posting lets this escape Weave's lifecycle, so use a null-safe call on the webview here
                    if (view != null) binding.discussionRepliesWebViewWrapper.webView.loadUrl("javascript:markAsRead" + "('" + entryId.toString() + "')")
                }
            }

            if (!groupDiscussion) {
                DiscussionTopicHeaderEvent(discussionTopicHeader).post()
            }
        } catch {
            Logger.e("Error with DiscussionDetailsFragment:markAsRead() " + it.message)
        }
    }

    private fun askToDeleteDiscussionEntry(discussionEntryId: Long) {
        if (repository.isOnline()) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage(R.string.utils_discussionsDeleteWarning)
            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                deleteDiscussionEntry(discussionEntryId)
            }
            builder.setNegativeButton(android.R.string.cancel) { _, _ -> }
            val dialog = builder.create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
            }
            dialog.show()
        } else NoInternetConnectionDialog.show(requireFragmentManager())
    }

    private fun updateDiscussionAsDeleted(discussionEntry: DiscussionEntry) {
        val deletedText = DiscussionUtils.formatDeletedInfoText(requireContext(), discussionEntry)
        binding.discussionRepliesWebViewWrapper.post {
            binding.discussionRepliesWebViewWrapper.webView.loadUrl(
                    "javascript:markAsDeleted" + "('" + discussionEntry.id.toString() + "','" + deletedText + "')")
        }
    }

    private fun deleteDiscussionEntry(entryId: Long) {
        lifecycleScope.tryLaunch {
            val result = repository.deleteDiscussionEntry(canvasContext, discussionTopicHeader.id, entryId)

            if (result is DataResult.Success) {
                discussionTopic?.let {
                    DiscussionUtils.findEntry(entryId, it.views)?.let { entry ->
                        entry.deleted = true
                        updateDiscussionAsDeleted(entry)
                        discussionTopicHeader.decrementDiscussionSubentryCount()
                        if (!groupDiscussion) {
                            DiscussionTopicHeaderEvent(discussionTopicHeader).post()
                        }
                    }
                }
            }
        }
    }

    private fun showUpdateReplyView(discussionEntryId: Long) {
        if (repository.isOnline()) {
            discussionTopic?.let {
                val route = DiscussionsUpdateFragment.makeRoute(
                    canvasContext,
                    discussionTopicHeader.id,
                    DiscussionUtils.findEntry(discussionEntryId, it.views)
                )
                RouteMatcher.route(requireActivity(), route)
            }
        } else NoInternetConnectionDialog.show(requireFragmentManager())
    }

    //endregion

    //region Liking

    private fun likeDiscussionPressed(discussionEntryId: Long) {
        if (repository.isOnline()) {
            discussionTopic?.let { discussionTopic ->
                DiscussionUtils.findEntry(discussionEntryId, discussionTopic.views)?.let { entry ->
                    val rating = if (discussionTopic.entryRatings.containsKey(discussionEntryId)) discussionTopic.entryRatings[discussionEntryId] else 0
                    val newRating = if (rating == 1) 0 else 1

                    lifecycleScope.tryLaunch {
                        repository.rateDiscussionEntry(canvasContext, discussionTopicHeader.id, discussionEntryId, newRating)

                        discussionTopic.entryRatings[discussionEntryId] = newRating

                        if (newRating == 1) {
                            entry.ratingSum += 1
                            entry._hasRated = true
                            withContext(Dispatchers.Main) {
                                updateDiscussionLiked(entry)
                            }
                        } else if (entry.ratingSum > 0) {
                            entry.ratingSum -= 1
                            entry._hasRated = false
                            withContext(Dispatchers.Main) {
                                updateDiscussionUnliked(entry)
                            }
                        }
                    } catch {
                        // Maybe a permissions issue?
                        Logger.e("Error liking discussion entry: " + it.message)
                    }
                }
            }
        } else NoInternetConnectionDialog.show(requireFragmentManager())
    }

    private fun updateDiscussionLiked(discussionEntry: DiscussionEntry) = updateDiscussionLikedState(discussionEntry, JS_CONST_SET_LIKED /*Constant found in the JS files*/)
    private fun updateDiscussionUnliked(discussionEntry: DiscussionEntry) = updateDiscussionLikedState(discussionEntry, JS_CONST_SET_UNLIKED  /*Constant found in the JS files*/)

    private fun updateDiscussionLikedState(discussionEntry: DiscussionEntry, methodName: String) {
        val likingSum = if (discussionEntry.ratingSum == 0) "" else "(${discussionEntry.ratingSum})"
        val likingSumAllyText = DiscussionEntryHtmlConverter.getLikeCountText(requireContext(), discussionEntry)
        val likingColor = DiscussionUtils.getHexColorString(if (discussionEntry._hasRated) ThemePrefs.brandColor else ContextCompat.getColor(requireContext(), R.color.textDark))
        activity?.runOnUiThread {
            binding.discussionRepliesWebViewWrapper.webView.loadUrl("javascript:$methodName('${discussionEntry.id}')")
            binding.discussionRepliesWebViewWrapper.webView.loadUrl("javascript:updateLikedCount('${discussionEntry.id}','$likingSum','$likingColor','$likingSumAllyText')")
        }
    }

    //endregion

    //region WebView And Javascript

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(webView: CanvasWebView, addDarkTheme: Boolean = false) {
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        val backgroundColorRes = if (addDarkTheme) R.color.backgroundLightest else R.color.white
        webView.setBackgroundColor(requireContext().getColor(backgroundColorRes))
        webView.settings.javaScriptEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        CookieManager.getInstance().acceptThirdPartyCookies(webView)
        webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun routeInternallyCallback(url: String) {
                if (!RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, routeIfPossible = true, allowUnsupported = false)) {
                    RouteMatcher.route(requireActivity(), InternalWebviewFragment.makeRoute(url, url, false, ""))
                }
            }

            override fun canRouteInternallyDelegate(url: String): Boolean = true

            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                openMedia(canvasContext, url, filename, null)
            }

            override fun onPageStartedCallback(webView: WebView, url: String) = Unit
            override fun onPageFinishedCallback(webView: WebView, url: String) = Unit
        }

        webView.addVideoClient(requireActivity())
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupHeaderWebView() {
        setupWebView(binding.discussionTopicHeaderWebViewWrapper.webView)
        binding.discussionTopicHeaderWebViewWrapper.webView.addJavascriptInterface(JSDiscussionHeaderInterface(), "accessor")
        DiscussionManager.markDiscussionTopicRead(canvasContext, getTopicId(), object : StatusCallback<Void>() {})
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupRepliesWebView() = with(binding) {
        setupWebView(discussionRepliesWebViewWrapper.webView, addDarkTheme = !discussionRepliesWebViewWrapper.themeSwitched)
        discussionRepliesWebViewWrapper.onThemeChanged = { themeChanged, html ->
            setupWebView(discussionRepliesWebViewWrapper.webView, addDarkTheme = !themeChanged)
            discussionRepliesWebViewWrapper.loadDataWithBaseUrl(CanvasWebView.getReferrer(true), html, "text/html", "UTF-8", null)
        }
        discussionRepliesWebViewWrapper.webView.addJavascriptInterface(JSDiscussionInterface(), "accessor")
    }

    @Suppress("unused")
    private inner class JSDiscussionHeaderInterface {

        @JavascriptInterface
        fun onLtiToolButtonPressed(id: String) {
            val ltiUrl = URLDecoder.decode(id, "UTF-8")
            getAuthenticatedURL(ltiUrl) { authenticatedUrl, _ ->
                DiscussionUtils.launchIntent(requireContext(), authenticatedUrl)
            }
        }

        // A helper to log out messages from the JS code
        @JavascriptInterface
        fun logMessage(message: String) = Logger.d(message)
    }

    @Suppress("UNUSED_PARAMETER")
    private inner class JSDiscussionInterface {

        @JavascriptInterface
        fun onItemPressed(id: String) {
            // Do nothing for now
        }

        @JavascriptInterface
        fun onAvatarPressed(id: String) {
            // Not supporting avatar events at this time
        }

        @JavascriptInterface
        fun onAttachmentPressed(id: String) {
            discussionTopic?.let {
                DiscussionUtils.findEntry(id.toLong(), it.views)?.let { entry ->
                    if (entry.attachments!!.isNotEmpty()) viewAttachments(entry.attachments!!)
                }
            }
        }

        @JavascriptInterface
        fun onReplyPressed(id: String) = showReplyView(id.toLong())

        @JavascriptInterface
        fun onEditPressed(id: String) = showUpdateReplyView(id.toLong())

        @JavascriptInterface
        fun onDeletePressed(id: String) = askToDeleteDiscussionEntry(id.toLong())

        @JavascriptInterface
        fun onLikePressed(id: String) = likeDiscussionPressed(id.toLong())

        @JavascriptInterface
        fun onMoreRepliesPressed(id: String) {
            discussionTopic?.let {
                val route = makeRoute(canvasContext, discussionTopicHeader, it, id.toLong())
                RouteMatcher.route(requireActivity(), route)
            }
        }

        @JavascriptInterface
        fun getInViewPort(): String = discussionTopic?.unreadEntries?.joinToString() ?: ""

        @JavascriptInterface
        fun inViewPortAndUnread(idList: String) {
            if (idList.isNotEmpty()) {
                markAsRead(idList.split(",").map { it.toLong() })
            }
        }

        @JavascriptInterface
        fun getLikedImage(): String {
            //Returns a string of a bitmap colored for the thumbs up (like) image.
            val likeImage = DiscussionUtils.getBitmapFromAssets(requireContext(), "discussion_liked.png")
            return DiscussionUtils.makeBitmapForWebView(ThemePrefs.brandColor, likeImage)
        }

        // A helper to log out messages from the JS code
        @JavascriptInterface
        fun logMessage(message: String) = Logger.d(message)

        /**
         * Calculates the offset of the scrollview and it's content as compared to the elements position within the webview.
         * A scrollview's visible window can be between 0 and the size of the scrollview's height. This looks at the content on top
         * of the discussion replies webview and adds that to the elements position to come up with a relative position for the element
         * within the scrollview. In sort we are finding the elements position within a scrollview.
         */
        @Suppress("UNUSED_PARAMETER")
        @JavascriptInterface
        fun calculateActualOffset(elementId: String, elementHeight: String, elementTopOffset: String): Boolean {
            return DiscussionUtils.isElementInViewPortWithinScrollView(
                    requireContext(),
                    binding.discussionsScrollView,
                    binding.discussionRepliesWebViewWrapper.height,
                    binding.discussionsScrollViewContentWrapper.height,
                    // Javascript passes us back a number, which could be either a float or an int, so we'll need to convert the string first to a float, then an int to avoid errors in conversion
                    elementHeight.toFloat().toInt(), elementTopOffset.toFloat().toInt())
        }
    }
    //endregion

    //region Display Helpers

    /**
     * Method to put an authenticated URL in place of a non-authenticated URL (like when we try to load the Studio LTI in a WebView)
     */
    private fun getAuthenticatedURL(html: String, loadHtml: (newUrl: String, originalUrl: String?) -> Unit) {
        if (authenticatedSessionURL.isNullOrBlank()) {
            //get the url
            lifecycleScope.tryLaunch {
                //get the url from html
                val matcher = Pattern.compile("src=\"([^\"]+)\"").matcher(discussionTopicHeader.message)
                matcher.find()
                val url = matcher.group(1)

                // Get an authenticated session so the user doesn't have to log in
                authenticatedSessionURL = repository.getAuthenticatedSession(url)?.sessionUrl
                loadHtml(DiscussionUtils.getNewHTML(html, authenticatedSessionURL), url)
            } catch {
                //couldn't get the authenticated session, try to load it without it
                loadHtml(html, null)
            }
        } else {
            loadHtml(DiscussionUtils.getNewHTML(html, authenticatedSessionURL), null)
        }
    }

    //endregion

    //region Loading
    private fun populateDiscussionData(forceRefresh: Boolean = false, topLevelReplyPosted: Boolean = false) = with(binding) {
        lifecycleScope.tryLaunch {
            discussionProgressBar.setVisible()
            discussionRepliesWebViewWrapper.loadHtml("", "")
            discussionRepliesWebViewWrapper.setInvisible()
            discussionTopicRepliesTitle.setInvisible()
            postBeforeViewingRepliesTextView.setGone()

            // Do we have a discussion topic header? if not fetch it, or if forceRefresh is true force a fetch

            val courseId = when (canvasContext) {
                is Course -> canvasContext.id
                is Group -> (canvasContext as Group).courseId
                else -> null
            }

            if (courseId != null) {
                courseSettings = repository.getCourseSettings(courseId, forceRefresh)
            }
            if (forceRefresh) {
                val discussionTopicHeaderId = if (discussionTopicHeaderId == 0L && discussionTopicHeader.id != 0L) discussionTopicHeader.id else discussionTopicHeaderId
                if (!updateToGroupIfNecessary()) {
                    discussionTopicHeader = repository.getDetailedDiscussion(canvasContext, discussionTopicHeaderId,true)
                }
            } else {
                // If there is no discussion (ID not set), then we need to load one
                if (discussionTopicHeader.id == 0L) {
                    discussionTopicHeader = repository.getDetailedDiscussion(canvasContext, discussionTopicHeaderId, true)
                }
            }

            // Check if we were routed with a course context, instead of a group context.
            // Need to check here again in case we were only routed with a url instead of a whole discussionTopicHeader.
            updateToGroupIfNecessary()

            loadDiscussionTopicHeaderViews(discussionTopicHeader)
            addAccessibilityButton()
            // We only want to request the full discussion if it is not anonymous. Anonymous discussions are not supported by the API
            if (forceRefresh || discussionTopic == null && discussionTopicHeader.anonymousState == null) {
                // forceRefresh is true, fetch the discussion topic
                discussionTopic = getDiscussionTopic()

                withContext(Dispatchers.IO){ discussionTopic?.views?.forEach { it.init(discussionTopic!!, it, repository.isOnline()) } }
            }

            if (discussionTopic == null || discussionTopic?.views?.isEmpty() == true && DiscussionCaching(discussionTopicHeader.id).isEmpty()) {
                // Nothing to display
                discussionProgressBar.setGone()
                discussionTopicRepliesTitle.setGone()
                swipeRefreshLayout.isRefreshing = false

                if (discussionTopicHeader.anonymousState != null) {
                    showAnonymousDiscussionView()
                }
            } else {
                val html = DiscussionUtils.createDiscussionTopicHtml(
                            requireContext(),
                            isTablet,
                            canvasContext,
                            discussionTopicHeader,
                            discussionTopic!!.views,
                            discussionEntryId,
                            repository.isOnline()
                        )

                loadDiscussionTopicViews(html)

                delay(300)
                discussionsScrollView.post {
                    if (topLevelReplyPosted) {
                        discussionsScrollView.fullScroll(ScrollView.FOCUS_DOWN)
                    } else {
                        discussionsScrollView.scrollTo(0, scrollPosition)
                    }
                }
            }
        } catch {
            Logger.e("Error loading discussion topic " + it.message)
        }
    }

    private fun showAnonymousDiscussionView() = with(binding) {
        anonymousDiscussionsNotSupported.setVisible()
        openInBrowser.setVisible(discussionTopicHeader.htmlUrl?.isNotEmpty() == true)
        replyToDiscussionTopic.setGone()
        swipeRefreshLayout.isEnabled = false

        openInBrowser.onClickWithRequireNetwork {
            discussionTopicHeader.htmlUrl?.let { url ->
                InternalWebviewFragment.loadInternalWebView(
                    activity,
                    InternalWebviewFragment.makeRoute(canvasContext, url, true, true)
                )
            }
        }
    }

    private suspend fun updateToGroupIfNecessary(): Boolean {
        var changed = false
        if (!canvasContext.isGroup && discussionTopicHeader.groupCategoryId != null && discussionTopicHeader.groupTopicChildren.count() > 0) {
            val groupPair = getDiscussionGroup(discussionTopicHeader)
            if (groupPair != null) {
                changed = true
                canvasContext = groupPair.first
                discussionTopicHeaderId = groupPair.second
                discussionTopicHeader = repository.getDetailedDiscussion(canvasContext, discussionTopicHeaderId, true)
            }
        }

        return changed
    }

    suspend fun getDiscussionGroup(discussionTopicHeader: DiscussionTopicHeader): Pair<Group, Long>? {
        var groups = emptyList<Group>()
        ApiPrefs.user?.let { user ->
            groups = repository.getAllGroups(user.id, true)
        }
        for (group in groups) {
            val groupsMap = discussionTopicHeader.groupTopicChildren.associateBy({it.groupId}, {it.id})
            if (groupsMap.contains(group.id) && groupsMap[group.id] != null) {
                groupsMap[group.id]?.let { topicHeaderId ->
                    return Pair(group, topicHeaderId)
                }

                return null // There is a group, but not a matching topic header id
            }
        }
        // If we made it to here, there are no groups that match this
        return null
    }

    private suspend fun getDiscussionTopic(): DiscussionTopic {
        if (discussionTopicHeader.groupTopicChildren.isNotEmpty()) {
            // This is the base discussion for a group discussion

            // Grab the groups that the user belongs to
            var userGroups = emptyList<Long>()
            ApiPrefs.user?.let { user ->
                userGroups = repository.getAllGroups(user.id, true).map { it.id }
            }

            // Match group from discussion to a group that the user is a part of
            var context = canvasContext
            var discussionId = discussionTopicHeader.id
            userGroups.forEach { userGroup ->
                discussionTopicHeader.groupTopicChildren.forEach {
                    if (userGroup == it.groupId) {
                        context = CanvasContext.emptyGroupContext(it.groupId)
                        discussionId = it.id
                    }
                }
            }

            return repository.getFullDiscussionTopic(context, discussionId, true)
        } else {
            // Regular discussion, fetch normally
            return repository.getFullDiscussionTopic(canvasContext, discussionTopicHeader.id, true)
        }
    }

    @BeforePageView
    private fun loadDiscussionTopicHeaderViews(discussionTopicHeader: DiscussionTopicHeader) = with(binding) {
        if (discussionTopicHeader.assignment != null) {
            setupAssignmentDetails(discussionTopicHeader.assignment!!)
        }

        if (discussionTopicHeader.requireInitialPost && !discussionTopicHeader.userCanSeePosts) {
            // User must post before seeing replies
            discussionTopicRepliesTitle.setGone()
            postBeforeViewingRepliesTextView.setVisible()
            discussionProgressBar.setGone()
        } else {
            postBeforeViewingRepliesTextView.setGone()
        }

        val displayName = discussionTopicHeader.author?.displayName
        ProfileUtils.loadAvatarForUser(authorAvatar, displayName, discussionTopicHeader.author?.avatarImageUrl)
        authorAvatar.setupAvatarA11y(discussionTopicHeader.author?.displayName)
        authorName.text = Pronouns.span(displayName, discussionTopicHeader.author?.pronouns)
        authoredDate.text = DateHelper.getMonthDayAtTime(this@DiscussionDetailsFragment.context, discussionTopicHeader.postedDate, getString(R.string.at))
        discussionTopicTitle.text = discussionTopicHeader.title

        discussionSection.text = getString(R.string.announcementSections, discussionTopicHeader.sections?.joinToString { it.name })
        discussionSection.setVisible(discussionTopicHeader.sections?.isNotEmpty() == true)

        replyToDiscussionTopic.setTextColor(ThemePrefs.textButtonColor)
        replyToDiscussionTopic.setVisible(discussionTopicHeader.permissions?.reply ?: false)
        replyToDiscussionTopic.onClick { showReplyView(discussionTopicHeader.id) }

        discussionTopicHeaderWebViewWrapper.webView.loadHtmlWithIframes(requireContext(), discussionTopicHeader.message, {
            if (view != null) loadHTMLTopic(it, discussionTopicHeader.title)
        }, onLtiButtonPressed = {
            RouteMatcher.route(requireActivity(), LtiLaunchFragment.makeSessionlessLtiUrlRoute(requireActivity(), canvasContext, it))
        })

        attachmentIcon.setVisible(discussionTopicHeader.attachments.isNotEmpty())
        attachmentIcon.onClick {
            viewAttachments(discussionTopicHeader.attachments)
        }
    }

    private fun loadHTMLTopic(html: String, contentDescription: String?) {
        setupHeaderWebView()
        binding.discussionTopicHeaderWebViewWrapper.loadHtml(html, contentDescription, baseUrl = discussionTopicHeader.htmlUrl)
    }

    private fun loadDiscussionTopicViews(html: String) = with(binding) {
        discussionRepliesWebViewWrapper.setVisible()
        discussionProgressBar.setGone()

        setupRepliesWebView()

        discussionRepliesWebViewWrapper.webView.loadHtmlWithIframes(requireContext(), html, { formattedHtml ->
            discussionRepliesWebViewWrapper.loadDataWithBaseUrl(CanvasWebView.getReferrer(true), formattedHtml, "text/html", "UTF-8", null)
        }, onLtiButtonPressed = { RouteMatcher.route(requireActivity(), LtiLaunchFragment.makeSessionlessLtiUrlRoute(requireActivity(), canvasContext, it)) })

        swipeRefreshLayout.isRefreshing = false
        discussionTopicRepliesTitle.setVisible(discussionTopicHeader.shouldShowReplies)
        if (repository.isOnline()){ postBeforeViewingRepliesTextView.setGone() }
    }
    //endregion Loading

    private fun setupAssignmentDetails(assignment: Assignment) =
        with(binding) {
            with(assignment) {
                pointsTextView.setVisible(!courseSettings?.restrictQuantitativeData.orDefault())
                // Points possible
                pointsTextView.text = resources.getQuantityString(
                    R.plurals.quantityPointsAbbreviated,
                    pointsPossible.toInt(),
                    NumberHelper.formatDecimal(pointsPossible, 1, true)
                )
                pointsTextView.contentDescription = resources.getQuantityString(
                    R.plurals.quantityPointsFull,
                    pointsPossible.toInt(),
                    NumberHelper.formatDecimal(pointsPossible, 1, true)
                )

                // Set these as gone initially and make them visible if we have data for them
                availabilityLayout.setGone()
                availableFromLayout.setGone()
                availableToLayout.setGone()
                dueDateLayout.setGone()

                // Lock status
                val atSeparator = getString(R.string.at)

                allDates.singleOrNull()?.apply {
                    if (lockDate?.before(Date()) == true) {
                        availabilityLayout.setVisible()
                        availabilityTextView.setText(R.string.closed)
                    } else {
                        availableFromLayout.setVisible()
                        availableToLayout.setVisible()
                        availableFromTextView.text = if (unlockAt != null)
                            DateHelper.getMonthDayAtTime(
                                requireContext(),
                                unlockDate,
                                atSeparator
                            ) else getString(R.string.utils_noDateFiller)
                        availableToTextView.text = if (lockAt != null)
                            DateHelper.getMonthDayAtTime(
                                requireContext(),
                                lockDate,
                                atSeparator
                            ) else getString(R.string.utils_noDateFiller)
                    }
                }

                dueLayout.setVisible(allDates.singleOrNull() != null)

                dueDate?.let {
                    dueDateLayout.setVisible()
                    dueDateTextView.text = DateHelper.getMonthDayAtTime(requireContext(), it, atSeparator)
                }
            }
        }

    private fun addAccessibilityButton() {
        if (isAccessibilityEnabled(requireContext()) && discussionTopicHeader.htmlUrl != null) {
            binding.alternateViewButton.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    RouteMatcher.route(requireActivity(),
                        InternalWebviewFragment.makeRoute(
                            canvasContext,
                            discussionTopicHeader.htmlUrl!!,
                            authenticate = true,
                            shouldRouteInternally = false,
                            allowRoutingTheSameUrlInternally = false,
                            isUnsupportedFeature = false,
                            allowUnsupportedRouting = false
                        )
                    )
                }
            }
        }
    }

    //endregion Functionality

    // region Bus Events
    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDiscussionReplyCreated(event: DiscussionEntryEvent) {
        populateDiscussionData(true, event.topLevelReplyPosted)

        discussionTopicHeader.incrementDiscussionSubentryCount() // Update subentry count
        discussionTopicHeader.lastReplyDate?.time = Date().time // Update last post time
        if (!groupDiscussion) {
            DiscussionTopicHeaderEvent(discussionTopicHeader).post()
        }
        // needed for when discussions are in modules
        applyTheme()

        // We don't want to remove the event immediately because more screens might need to process it
        Handler(Looper.getMainLooper()).postDelayed({
            EventBus.getDefault().removeStickyEvent(event)
        }, 100)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBackStackChangedEvent(event: OnBackStackChangedEvent) {
        event.get { clazz ->
            if (clazz?.isAssignableFrom(DiscussionDetailsFragment::class.java) == true || clazz?.isAssignableFrom(CourseModuleProgressionFragment::class.java) == true) {
                binding.discussionRepliesWebViewWrapper.webView.onResume()
                binding.discussionTopicHeaderWebViewWrapper.webView.onResume()
            } else {
                binding.discussionRepliesWebViewWrapper.webView.onPause()
                binding.discussionTopicHeaderWebViewWrapper.webView.onPause()
            }
        }
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onModuleUpdatedEvent(event: ModuleUpdatedEvent) {
        populateDiscussionData(true)
    }

    //endregion

    companion object {
        const val DISCUSSION_TOPIC_HEADER = "discussion_topic_header"
        const val DISCUSSION_TOPIC_HEADER_ID = "discussion_topic_header_id"
        const val DISCUSSION_TITLE = "discussion_title"
        const val DISCUSSION_TOPIC = "discussion_topic"
        const val DISCUSSION_ENTRY_ID = "discussion_entry_id"
        const val IS_NESTED_DETAIL = "is_nested_detail"
        const val GROUP_DISCUSSION = "group_discussion"

        private const val JS_CONST_SET_LIKED = "setLiked"
        private const val JS_CONST_SET_UNLIKED = "setUnliked"

        fun makeRoute(canvasContext: CanvasContext, discussionTopicHeader: DiscussionTopicHeader): Route {
            val bundle = Bundle().apply {
                putParcelable(DISCUSSION_TOPIC_HEADER, discussionTopicHeader)
            }

            return Route(null, DiscussionDetailsFragment::class.java, canvasContext, bundle)
        }

        fun makeRoute(canvasContext: CanvasContext, discussionTopicHeaderId: Long, title: String? = null, groupDiscussion: Boolean = false): Route {
            val bundle = Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putLong(DISCUSSION_TOPIC_HEADER_ID, discussionTopicHeaderId)
                putString(DISCUSSION_TITLE, title)
                putBoolean(GROUP_DISCUSSION, groupDiscussion)
            }
            return Route(null, DiscussionDetailsFragment::class.java, canvasContext, bundle)
        }

        fun makeRoute(
                canvasContext: CanvasContext,
                discussionTopicHeader: DiscussionTopicHeader,
                discussionTopic: DiscussionTopic,
                discussionEntryId: Long): Route {
            val bundle = Bundle().apply {
                // Used for viewing more entries, beyond the default nesting
                putParcelable(DISCUSSION_TOPIC_HEADER, discussionTopicHeader)
                putParcelable(DISCUSSION_TOPIC, discussionTopic)
                putLong(DISCUSSION_ENTRY_ID, discussionEntryId)
                putBoolean(IS_NESTED_DETAIL, true)
            }

            return Route(null, DiscussionDetailsFragment::class.java, canvasContext, bundle)
        }

        fun newInstance(route: Route) = if (validRoute(route)) {
            DiscussionDetailsFragment().apply {
                arguments = route.canvasContext!!.makeBundle(route.arguments)

                // For routing
                if (route.paramsHash.containsKey(RouterParams.MESSAGE_ID))
                    discussionTopicHeaderId = route.paramsHash[RouterParams.MESSAGE_ID]?.toLong() ?: 0L
            }
        } else null

        fun validRoute(route: Route) = route.canvasContext != null &&
                (route.arguments.containsKey(DISCUSSION_TOPIC_HEADER) ||
                        route.arguments.containsKey(DISCUSSION_TOPIC_HEADER_ID) ||
                        route.paramsHash.containsKey(RouterParams.MESSAGE_ID))

    }
}
