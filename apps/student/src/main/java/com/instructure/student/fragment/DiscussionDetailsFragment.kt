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
package com.instructure.student.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.managers.DiscussionManager.deleteDiscussionEntry
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.pageview.BeforePageView
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.canvasapi2.utils.pageview.PageViewUrlQuery
import com.instructure.canvasapi2.utils.weave.*
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.loginapi.login.dialog.NoInternetConnectionDialog
import com.instructure.pandautils.discussions.DiscussionCaching
import com.instructure.pandautils.discussions.DiscussionEntryHtmlConverter
import com.instructure.pandautils.discussions.DiscussionUtils
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.BuildConfig
import com.instructure.student.R
import com.instructure.student.events.DiscussionTopicHeaderEvent
import com.instructure.student.events.DiscussionUpdatedEvent
import com.instructure.student.events.ModuleUpdatedEvent
import com.instructure.student.events.post
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.Const
import kotlinx.android.synthetic.main.fragment_discussions_details.*
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Response
import java.net.URLDecoder
import java.util.*
import java.util.regex.Pattern

@PageView(url = "{canvasContext}/discussion_topics/{topicId}")
class DiscussionDetailsFragment : ParentFragment(), Bookmarkable {
    // Weave jobs
    private var sessionAuthJob: Job? = null
    private var discussionMarkAsReadJob: Job? = null
    private var discussionLikeJob: Job? = null
    private var discussionsLoadingJob: WeaveJob? = null

    // Bundle args
    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var discussionTopic: DiscussionTopic? by NullableParcelableArg(key = DISCUSSION_TOPIC)
    private var discussionTopicHeader: DiscussionTopicHeader by ParcelableArg(default = DiscussionTopicHeader(), key = DISCUSSION_TOPIC_HEADER)
    private var discussionTopicHeaderId: Long by LongArg(default = 0L, key = DISCUSSION_TOPIC_HEADER_ID)
    private var discussionTitle: String? by NullableStringArg(key = DISCUSSION_TITLE)
    private var discussionEntryId: Long by LongArg(default = 0L, key = DISCUSSION_ENTRY_ID)
    private var isNestedDetail: Boolean by BooleanArg(default = false, key = IS_NESTED_DETAIL)

    private var scrollPosition: Int = 0
    private var authenticatedSessionURL: String? = null
    private var headerLoadHtmlJob: Job? = null

    //region Analytics
    @Suppress("unused")
    @PageViewUrlParam("topicId")
    private fun getTopicId() = discussionTopicHeader.id

    @Suppress("unused")
    @PageViewUrlQuery("module_item_id")
    private fun pageViewModuleItemId() = getModuleItemId()
    //endregion

    //region Fragment Lifecycle Overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        TelemetryUtils.setInteractionName(this::class.java.simpleName)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            layoutInflater.inflate(R.layout.fragment_discussions_details, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        populateDiscussionData()
        swipeRefreshLayout.setOnRefreshListener {
            authenticatedSessionURL = null
            populateDiscussionData(true)
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
        discussionTopicHeaderWebView.onResume()
        discussionRepliesWebView.onResume()

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

    override fun onPause() {
        super.onPause()
        scrollPosition = discussionsScrollView.scrollY
        discussionTopicHeaderWebView.onPause()
        discussionRepliesWebView.onPause()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sessionAuthJob?.cancel()
        discussionMarkAsReadJob?.cancel()
        discussionLikeJob?.cancel()
        discussionsLoadingJob?.cancel()
        headerLoadHtmlJob?.cancel()
        discussionTopicHeaderWebView?.destroy()
        discussionRepliesWebView?.destroy()
    }
    //endregion

    //region Fragment Interaction Overrides
    override fun title(): String = discussionTitle ?: if (discussionTopicHeaderId == 0L) discussionTopicHeader.title
            ?: getString(R.string.discussion) else getString(R.string.discussion)

    override fun applyTheme() {
        toolbar.title = title()
        setupToolbarMenu(toolbar)
        toolbar.setupAsBackButton(this)
        /* TODO - Blocked by COMMS - 868
        if(!isAnnouncements && discussionTopicHeader.author.id == ApiPrefs.user?.id && hasEditPermissions) {
            toolbar.setMenu(R.menu.menu_edit_generic, menuItemCallback)
        }
        */
        ViewStyler.themeToolbar(requireActivity(), toolbar, canvasContext)
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
                Snackbar.make(view!!, remoteFile.lockExplanation!!, Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(view!!, R.string.fileCurrentlyLocked, Snackbar.LENGTH_SHORT).show()
            }
        }

        // Show attachment
        val attachment = remoteFile.mapToAttachment()
        openMedia(attachment.contentType, attachment.url, attachment.filename, canvasContext)
    }

    private fun showReplyView(discussionEntryId: Long) {
        if (APIHelper.hasNetworkConnection()) {
            scrollPosition = discussionsScrollView.scrollY
            val route = DiscussionsReplyFragment.makeRoute(canvasContext, discussionTopicHeader.id, discussionEntryId, discussionTopicHeader.permissions!!.attach)
            RouteMatcher.route(requireActivity(), route)
        } else {
            NoInternetConnectionDialog.show(requireFragmentManager())
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun markAsRead(discussionEntryIds: List<Long>) {
        if (discussionMarkAsReadJob?.isActive == true) return
        discussionMarkAsReadJob = tryWeave {
            val successfullyMarkedAsReadIds: MutableList<Long> = ArrayList(discussionEntryIds.size)
            discussionEntryIds.forEach { entryId ->
                val response = awaitApiResponse<Void> { DiscussionManager.markDiscussionTopicEntryRead(canvasContext, discussionTopicHeader.id, entryId, it) }
                if (response.isSuccessful) {
                    successfullyMarkedAsReadIds.add(entryId)
                    discussionTopic?.let {
                        val entry = DiscussionUtils.findEntry(entryId, it.views)
                        entry?.unread = false
                        it.unreadEntriesMap.remove(entryId)
                        it.unreadEntries.remove(entryId)
                        if (discussionTopicHeader.unreadCount > 0) discussionTopicHeader.unreadCount -= 1
                    }
                }
            }

            successfullyMarkedAsReadIds.forEach {
                discussionRepliesWebView.post {
                    // Posting lets this escape Weave's lifecycle, so use a null-safe call on the webview here
                    discussionRepliesWebView?.loadUrl("javascript:markAsRead" + "('" + it.toString() + "')")
                }
            }
            DiscussionTopicHeaderEvent(discussionTopicHeader).post()
        } catch {
            Logger.e("Error with DiscussionDetailsFragment:markAsRead() " + it.message)
        }
    }

    private fun askToDeleteDiscussionEntry(discussionEntryId: Long) {
        if (APIHelper.hasNetworkConnection()) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage(R.string.utils_discussionsDeleteWarning)
            builder.setPositiveButton(android.R.string.yes) { _, _ ->
                deleteDiscussionEntry(discussionEntryId)
            }
            builder.setNegativeButton(android.R.string.no) { _, _ -> }
            val dialog = builder.create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.buttonColor)
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.buttonColor)
            }
            dialog.show()
        } else NoInternetConnectionDialog.show(requireFragmentManager())
    }

    private fun updateDiscussionAsDeleted(discussionEntry: DiscussionEntry) {
        val deletedText = DiscussionUtils.formatDeletedInfoText(requireContext(), discussionEntry)
        discussionRepliesWebView.post {
            discussionRepliesWebView.loadUrl(
                    "javascript:markAsDeleted" + "('" + discussionEntry.id.toString() + "','" + deletedText + "')")
        }
    }

    private fun deleteDiscussionEntry(entryId: Long) {
        deleteDiscussionEntry(canvasContext, discussionTopicHeader.id, entryId, object : StatusCallback<Void>() {
            override fun onResponse(response: Response<Void>, linkHeaders: LinkHeaders, type: ApiType) {
                if (response.code() in 200..299) {
                    discussionTopic?.let {
                        DiscussionUtils.findEntry(entryId, it.views)?.let { entry ->
                            entry.deleted = true
                            updateDiscussionAsDeleted(entry)
                            discussionTopicHeader.decrementDiscussionSubentryCount()
                            DiscussionTopicHeaderEvent(discussionTopicHeader).post()
                        }
                    }
                }
            }
        })
    }

    private fun showUpdateReplyView(discussionEntryId: Long) {
        if (APIHelper.hasNetworkConnection()) {
            discussionTopic?.let {
                val route = DiscussionsUpdateFragment.makeRoute(canvasContext, discussionTopicHeader.id, DiscussionUtils.findEntry(discussionEntryId, it.views))
                RouteMatcher.route(requireActivity(), route)
            }
        } else NoInternetConnectionDialog.show(requireFragmentManager())
    }

    //endregion

    //region Liking

    private fun likeDiscussionPressed(discussionEntryId: Long) {
        discussionTopic?.let { discussionTopic ->
            if (discussionLikeJob?.isActive == true) return

            DiscussionUtils.findEntry(discussionEntryId, discussionTopic.views)?.let { entry ->
                discussionLikeJob = tryWeave {
                    val rating = if (discussionTopic.entryRatings.containsKey(discussionEntryId)) discussionTopic.entryRatings[discussionEntryId] else 0
                    val newRating = if (rating == 1) 0 else 1
                    val response = awaitApiResponse<Void> { DiscussionManager.rateDiscussionEntry(canvasContext, discussionTopicHeader.id, discussionEntryId, newRating, it) }

                    if (response.code() in 200..299) {
                        discussionTopic.entryRatings[discussionEntryId] = newRating

                        if (newRating == 1) {
                            entry.ratingSum += 1
                            entry._hasRated = true
                            updateDiscussionLiked(entry)
                        } else if (entry.ratingSum > 0) {
                            entry.ratingSum -= 1
                            entry._hasRated = false
                            updateDiscussionUnliked(entry)
                        }
                    }
                } catch {
                    // Maybe a permissions issue?
                    Logger.e("Error liking discussion entry: " + it.message)
                }
            }
        }
    }

    private fun updateDiscussionLiked(discussionEntry: DiscussionEntry) = updateDiscussionLikedState(discussionEntry, JS_CONST_SET_LIKED /*Constant found in the JS files*/)
    private fun updateDiscussionUnliked(discussionEntry: DiscussionEntry) = updateDiscussionLikedState(discussionEntry, JS_CONST_SET_UNLIKED  /*Constant found in the JS files*/)

    private fun updateDiscussionLikedState(discussionEntry: DiscussionEntry, methodName: String) {
        val likingSum = if (discussionEntry.ratingSum == 0) "" else "(${discussionEntry.ratingSum})"
        val likingSumAllyText = DiscussionEntryHtmlConverter.getLikeCountAllyText(requireContext(), discussionEntry)
        val likingColor = DiscussionUtils.getHexColorString(if (discussionEntry._hasRated) ThemePrefs.brandColor else ContextCompat.getColor(requireContext(), R.color.discussionLiking))
        activity?.runOnUiThread {
            discussionRepliesWebView.loadUrl("javascript:$methodName('${discussionEntry.id}')")
            discussionRepliesWebView.loadUrl("javascript:updateLikedCount('${discussionEntry.id}','$likingSum','$likingColor','$likingSumAllyText')")
        }
    }

    //endregion

    //region WebView And Javascript

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(webView: CanvasWebView) {
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        webView.setBackgroundColor(Color.WHITE)
        webView.settings.javaScriptEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.allowFileAccess = true
        webView.settings.loadWithOverviewMode = true
        CookieManager.getInstance().acceptThirdPartyCookies(webView)
        webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun routeInternallyCallback(url: String?) {
                if (url != null) {
                    if (!RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, true)) {
                        RouteMatcher.route(requireContext(), InternalWebviewFragment.makeRoute(url, url, false, ""))
                    }
                }
            }

            override fun canRouteInternallyDelegate(url: String?): Boolean = url != null

            override fun openMediaFromWebView(mime: String?, url: String?, filename: String?) {
                openMedia(canvasContext, url ?: "", filename)
            }

            override fun onPageStartedCallback(webView: WebView?, url: String?) {}
            override fun onPageFinishedCallback(webView: WebView?, url: String?) {}
        }

        webView.addVideoClient(requireActivity())
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupHeaderWebView() {
        setupWebView(discussionTopicHeaderWebView)
        discussionTopicHeaderWebView.addJavascriptInterface(JSDiscussionHeaderInterface(), "accessor")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupRepliesWebView() {
        setupWebView(discussionRepliesWebView)
        discussionRepliesWebView.addJavascriptInterface(JSDiscussionInterface(), "accessor")
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
                val route = DiscussionDetailsFragment.makeRoute(canvasContext, discussionTopicHeader, it, id.toLong())
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
                    discussionsScrollView,
                    discussionRepliesWebView.height,
                    discussionsScrollViewContentWrapper.height,
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
            sessionAuthJob = tryWeave {
                //get the url from html
                val matcher = Pattern.compile("src=\"([^\"]+)\"").matcher(discussionTopicHeader.message)
                matcher.find()
                val url = matcher.group(1)

                // Get an authenticated session so the user doesn't have to log in
                authenticatedSessionURL = awaitApi<AuthenticatedSession> { OAuthManager.getAuthenticatedSession(url, it) }.sessionUrl
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
    private fun loadHTMLTopic(html: String, ltiUrl: String? = null) {
        setupHeaderWebView()
        discussionTopicHeaderWebView.loadHtml(DiscussionUtils.createDiscussionTopicHeaderHtml(requireContext(), isTablet, html, ltiUrl), discussionTopicHeader.title)
    }

    private fun loadHTMLReplies(html: String) {
        discussionRepliesWebView.loadDataWithBaseURL(CanvasWebView.getReferrer(true), html, "text/html", "UTF-8", null)
    }

    private fun populateDiscussionData(forceRefresh: Boolean = false) {
        discussionsLoadingJob = tryWeave {
            discussionProgressBar.setVisible()
            discussionRepliesWebView.loadHtml("", "")
            discussionRepliesWebView.setInvisible()
            discussionTopicRepliesTitle.setInvisible()
            postBeforeViewingRepliesTextView.setGone()

            // Do we have a discussion topic header? if not fetch it, or if forceRefresh is true force a fetch

            if (forceRefresh) {
                val discussionTopicHeaderId = if (discussionTopicHeaderId == 0L && discussionTopicHeader.id != 0L) discussionTopicHeader.id else discussionTopicHeaderId
                if (!updateToGroupIfNecessary()) {
                    discussionTopicHeader = awaitApi { DiscussionManager.getDetailedDiscussion(canvasContext, discussionTopicHeaderId, it, true) }
                }
            } else {
                // If there is no discussion (ID not set), then we need to load one
                if (discussionTopicHeader.id == 0L) {
                    discussionTopicHeader = awaitApi { DiscussionManager.getDetailedDiscussion(canvasContext, discussionTopicHeaderId, it, true) }
                }
            }

            // Check if we were routed with a course context, instead of a group context.
            // Need to check here again in case we were only routed with a url instead of a whole discussionTopicHeader.
            updateToGroupIfNecessary()

            determinePermissions()

            loadDiscussionTopicHeaderViews(discussionTopicHeader)

            if (forceRefresh || discussionTopic == null) {
                // forceRefresh is true, fetch the discussion topic
                discussionTopic = getDiscussionTopic()

                inBackground { discussionTopic?.views?.forEach { it.init(discussionTopic!!, it) } }
            }

            if (discussionTopic == null || discussionTopic?.views?.isEmpty() == true && DiscussionCaching(discussionTopicHeader.id).isEmpty()) {
                // Nothing to display
                discussionProgressBar.setGone()
                discussionTopicRepliesTitle.setGone()
                swipeRefreshLayout.isRefreshing = false
            } else {
                val html = inBackground {
                    DiscussionUtils.createDiscussionTopicHtml(
                            requireContext(),
                            isTablet,
                            canvasContext,
                            discussionTopicHeader,
                            discussionTopic!!.views,
                            discussionEntryId)
                }

                loadDiscussionTopicViews(html)
                discussionsScrollView.post { discussionsScrollView?.scrollTo(0, scrollPosition) }
            }
        } catch {
            Logger.e("Error loading discussion topic " + it.message)
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
                discussionTopicHeader = awaitApi { DiscussionManager.getDetailedDiscussion(canvasContext, discussionTopicHeaderId, it, true) }
            }
        }

        return changed
    }

    private suspend fun getDiscussionTopic(): DiscussionTopic {
        if (discussionTopicHeader.groupTopicChildren.isNotEmpty()) {
            // This is the base discussion for a group discussion

            // Grab the groups that the user belongs to
            val userGroups = awaitApi<List<Group>> { GroupManager.getAllGroups(it, true) }.map { it.id }

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

            return awaitApi { DiscussionManager.getFullDiscussionTopic(context, discussionId, true, it) }
        } else {
            // Regular discussion, fetch normally
            return awaitApi { DiscussionManager.getFullDiscussionTopic(canvasContext, discussionTopicHeader.id, true, it) }
        }
    }

    @BeforePageView
    private fun loadDiscussionTopicHeaderViews(discussionTopicHeader: DiscussionTopicHeader) {
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
        authorName?.text = Pronouns.span(displayName, discussionTopicHeader.author?.pronouns)
        authoredDate?.text = DateHelper.getMonthDayAtTime(this@DiscussionDetailsFragment.context, discussionTopicHeader.postedDate, getString(R.string.at))
        discussionTopicTitle?.text = discussionTopicHeader.title

        discussionSection?.text = getString(R.string.announcementSections, discussionTopicHeader.sections?.joinToString { it.name })
        discussionSection?.setVisible(discussionTopicHeader.sections?.isNotEmpty() == true)

        replyToDiscussionTopic.setTextColor(ThemePrefs.buttonColor)
        replyToDiscussionTopic.setVisible(discussionTopicHeader.permissions!!.reply)
        replyToDiscussionTopic.onClick { showReplyView(discussionTopicHeader.id) }

        // If the html has a Studio LTI url, we want to authenticate so the user doesn't have to login again
        if (CanvasWebView.containsStudioLTI(discussionTopicHeader.message.orEmpty(), "UTF-8")) {
            // We are only handling Studio because there is not a predictable way for use to determine if a URL is and LTI launch
            getAuthenticatedURL(discussionTopicHeader.message.orEmpty()) { authenticatedHtml, originalUrl -> loadHTMLTopic(authenticatedHtml, originalUrl) }
        } else {
            loadHTMLTopic(discussionTopicHeader.message ?: "")
        }

        attachmentIcon.setVisible(!discussionTopicHeader.attachments.isEmpty())
        attachmentIcon.onClick { _ ->
            discussionTopicHeader.attachments?.let { viewAttachments(it) }
        }
    }

    private fun loadDiscussionTopicViews(html: String) {
        discussionRepliesWebView.setVisible()
        discussionProgressBar.setGone()
        // We are only handling Studio because there is not a predictable way for use to determine if a URL is an LTI launch
        if (CanvasWebView.containsStudioLTI(html, "UTF-8")) getAuthenticatedURL(html) { authenticatedHtml, _ -> loadHTMLReplies(authenticatedHtml) }
        else discussionRepliesWebView.loadDataWithBaseURL(CanvasWebView.getReferrer(), html, "text/html", "UTF-8", null)
        swipeRefreshLayout.isRefreshing = false
        discussionTopicRepliesTitle.setVisible(discussionTopicHeader.shouldShowReplies)
        postBeforeViewingRepliesTextView.setGone()

        setupRepliesWebView()
    }
    //endregion Loading

    private fun determinePermissions() {
        // Might still be needed once COMMS-868 is implemented, TBD
        //TODO: determine what permissions are available to student relative to course and discussion.
    }

    private fun setupAssignmentDetails(assignment: Assignment) = with(assignment) {
        pointsTextView.setVisible()
        // Points possible
        pointsTextView.text = resources.getQuantityString(
                R.plurals.quantityPointsAbbreviated,
                pointsPossible.toInt(),
                NumberHelper.formatDecimal(pointsPossible, 1, true)
        )
        pointsTextView.contentDescription = resources.getQuantityString(
                R.plurals.quantityPointsFull,
                pointsPossible.toInt(),
                NumberHelper.formatDecimal(pointsPossible, 1, true))

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
                    DateHelper.getMonthDayAtTime(requireContext(), unlockDate, atSeparator) else getString(R.string.utils_noDateFiller)
                availableToTextView.text = if (lockAt != null)
                    DateHelper.getMonthDayAtTime(requireContext(), lockDate, atSeparator) else getString(R.string.utils_noDateFiller)
            }
        }

        dueLayout.setVisible(allDates.singleOrNull() != null)

        dueDate?.let {
            dueDateLayout.setVisible()
            dueDateTextView.text = DateHelper.getMonthDayAtTime(requireContext(), it, atSeparator)
        }
    }
    //endregion Functionality

    // region Bus Events
    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onDiscussionReplyCreated(event: DiscussionEntryEvent) {
        event.once(discussionTopicHeader.id.toString()) {
            populateDiscussionData(true)

            discussionTopicHeader.incrementDiscussionSubentryCount() // Update subentry count
            discussionTopicHeader.lastReplyDate?.time = Date().time // Update last post time
            DiscussionTopicHeaderEvent(discussionTopicHeader).post()
            // needed for when discussions are in modules
            applyTheme()
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBackStackChangedEvent(event: OnBackStackChangedEvent) {
        event.get { clazz ->
            if (clazz?.isAssignableFrom(DiscussionDetailsFragment::class.java) == true || clazz?.isAssignableFrom(CourseModuleProgressionFragment::class.java) == true) {
                discussionRepliesWebView.onResume()
                discussionTopicHeaderWebView.onResume()
            } else {
                discussionRepliesWebView.onPause()
                discussionTopicHeaderWebView.onPause()
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

        private const val JS_CONST_SET_LIKED = "setLiked"
        private const val JS_CONST_SET_UNLIKED = "setUnliked"

        @JvmStatic
        fun makeRoute(canvasContext: CanvasContext, discussionTopicHeader: DiscussionTopicHeader): Route {
            val bundle = Bundle().apply {
                putParcelable(DISCUSSION_TOPIC_HEADER, discussionTopicHeader)
            }

            return Route(null, DiscussionDetailsFragment::class.java, canvasContext, bundle)
        }

        @JvmStatic
        fun makeRoute(canvasContext: CanvasContext, discussionTopicHeaderId: Long, title: String? = null): Route {
            val bundle = Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putLong(DISCUSSION_TOPIC_HEADER_ID, discussionTopicHeaderId)
                putString(DISCUSSION_TITLE, title)
            }
            return Route(null, DiscussionDetailsFragment::class.java, canvasContext, bundle)
        }

        @JvmStatic
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

        @JvmStatic
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

        suspend fun getDiscussionGroup(discussionTopicHeader: DiscussionTopicHeader): Pair<Group, Long>? {
            val groups = awaitApi<List<Group>> {
                GroupManager.getAllGroups(it, false)
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
    }
}
