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
package com.instructure.pandautils.discussions

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.net.Uri
import android.util.Base64
import android.view.View
import android.widget.ScrollView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.TextUtilsCompat
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toDate
import com.instructure.canvasapi2.utils.tryOrNull
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.DP
import com.instructure.pandautils.utils.Placeholder
import com.instructure.pandautils.utils.ProfileUtils
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.isCourse
import com.instructure.pandautils.utils.isGroup
import com.instructure.pandautils.views.CanvasWebView
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import java.util.UUID
import java.util.regex.Pattern

object DiscussionUtils {

    /* TODO: Support caching edited items. Comms - 868
    *  TODO: This has not yet been added due to a permissions issue.
    *  TODO: What needs to be done is to check the DiscussionUpdateFragment's response to ensure the last updated time stamp is correct.
    *  TODO: Then do a check here to not remove but replace discussion entries whose timestamps are behind the timestamps of cached items.
    * */

    private const val MAX_DISPLAY_DEPTH = 2

    private const val MAX_DISPLAY_DEPTH_TABLET = 4

    //region Cache and Discussion Unification

    private fun unifyDiscussionEntries(discussionEntries: MutableList<DiscussionEntry>): List<DiscussionEntry> {
        // Sort the discussion entries
        discussionEntries.sortBy { it.createdAt }

        // Loop through the highest level entries and add cached items where necessary for all children of parent
        discussionEntries.forEach { parentEntry ->
            parentEntry.replies = recursiveUnify(parentEntry.replies)
        }

        return discussionEntries
    }

    private fun recursiveUnify(discussionEntries: MutableList<DiscussionEntry>?): MutableList<DiscussionEntry>? {
        discussionEntries?.forEach { discussionEntry ->
            discussionEntry.replies = recursiveUnify(discussionEntry.replies)
        }
        return discussionEntries
    }
    //endregion

    //region Find Entry and Sub Entry

    private fun findSubEntry(startEntryId: Long, discussionEntries: List<DiscussionEntry>): List<DiscussionEntry> {
        discussionEntries.forEach {
            val foundEntries = recursiveFind(startEntryId, it.replies!!)
            if (foundEntries != null) {
                return foundEntries
            }
        }
        return discussionEntries
    }

    private fun recursiveFind(startEntryId: Long, replies: List<DiscussionEntry>?): List<DiscussionEntry>? {
        replies?.forEach {
            if (it.id == startEntryId) {
                // Creates a list of replies based on the entry the user clicked on. This will not show siblings of the parent.
                val formalReplies = ArrayList<DiscussionEntry>(1)
                formalReplies.add(it)
                return formalReplies
            } else {
                val items = recursiveFind(startEntryId, it.replies)
                if (items != null) {
                    return items
                }
            }
        }
        return null
    }

    private fun recursiveFindEntry(startEntryId: Long, replies: List<DiscussionEntry>?): DiscussionEntry? {
        replies?.forEach {
            if (it.id == startEntryId) {
                return it
            } else {
                val items = recursiveFindEntry(startEntryId, it.replies)
                if (items != null) return items
            }
        }
        return null
    }

    fun findEntry(entryId: Long, replies: List<DiscussionEntry>): DiscussionEntry? {
        replies.forEach { discussionEntry ->
            if (discussionEntry.id == entryId) {
                return discussionEntry
            }

            val entry = recursiveFindEntry(entryId, discussionEntry.replies)
            if (entry != null) return entry
        }
        return null
    }

    //endregion

    //region Discussion Topic Header
    fun createLTIPlaceHolders(context: Context, contentHtml: String, callback: (id: String, placeholder: Placeholder) -> Unit): String {
        val document = getAssetsFile(context, "lti_placeholder_template.html")
        val html = addLTIPlaceHolder(context, contentHtml, callback)
        return CanvasWebView.applyWorkAroundForDoubleSlashesAsUrlSource(document.replace("__HEADER_CONTENT__", html))
    }

    /**
     * Adds an LTI Tool placeholder in place of the iframe if we know the iframe is an External Tool
     */
    private fun addLTIPlaceHolder(context: Context, contentHtml: String, callback: (id: String, placeholder: Placeholder) -> Unit): String {
        var html = contentHtml
        // Append a open in browser button for the LTI tool
        val iframeMatcher = Pattern.compile("<iframe(.|\\n)*?iframe>").matcher(contentHtml)
        try {
            while (iframeMatcher.find()) {
                val iframe = iframeMatcher.group(0)
                if (iframe.contains("external_tools")) {
                    val buttonName = context.getString(R.string.utils_canvas) + UUID.randomUUID().toString() // Used to identify the button later on
                    val titleText = context.getString(R.string.utils_placeholderTitle)
                    val description = context.getString(R.string.utils_placeholderDescription)
                    val button = "</br><p><div name=\"$buttonName\" class=\"lti_placeholder\" contenteditable=\"false\">" +
                            "<div class=\"titleText\">$titleText</div>" +
                            "<div class=\"description\">$description</div>" +
                            "</div></p>"
                    callback(buttonName, Placeholder(iframe, button))
                    html = html.replace(iframe, button)
                }
            }
        } catch (e: Throwable) {
            // Pattern match not found.
        }
        return html
    }

    //endregion

    //region Loading the Discussion Topic

    /**
     * This function should only be called from a background thread as it can be very expensive to execute
     */
    suspend fun createDiscussionTopicHtml(
            context: Context,
            isTablet: Boolean,
            canvasContext: CanvasContext,
            discussionTopicHeader: DiscussionTopicHeader,
            discussionEntries: List<DiscussionEntry>,
            startEntryId: Long,
            isOnline: Boolean = true): String {

        val builder = StringBuilder()
        val brandColor = ThemePrefs.brandColor
        val likeColor = ContextCompat.getColor(context, R.color.textDark)
        val converter = DiscussionEntryHtmlConverter()
        val template = DiscussionHtmlTemplates.getItem(context)
        val likeImage = makeBitmapForWebView(brandColor, getBitmapFromAssets(context, "discussion_liked.png"))
        val replyButtonWidth = if (isTablet && context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) "260px" else "220px"

        builder.append(DiscussionHtmlTemplates.getHeader(context))


        val discussionEntryList: List<DiscussionEntry> = if (startEntryId == 0L) unifyDiscussionEntries(discussionEntries.toMutableList())
        else { // We are looking for a subentry of discussions to display. This finds a subentry, and uses that as the initial entry for display.
            findSubEntry(startEntryId, unifyDiscussionEntries(discussionEntries.toMutableList()))
        }

        // This loops through each of the direct replies and for each child up to 3 or 5 based on if tablet or phone.
        // General rule of thumb is to pass in any values that need calculation so we don't repeat those within the loop.
        // We also filter out any deleted discussions and by nature their children so they don't get displayed.

        val maxDepth = if (isTablet) MAX_DISPLAY_DEPTH_TABLET else MAX_DISPLAY_DEPTH

        fun buildEntry(discussionEntry: DiscussionEntry, depth: Int) {
            val isViewableEnd = (depth == maxDepth && discussionEntry.totalChildren > 0)
            builder.append(build(context, isTablet, canvasContext, discussionTopicHeader, discussionEntry, converter,
                    template, makeAvatarForWebView(context, discussionEntry, isOnline), depth, isViewableEnd, brandColor, likeColor,
                    likeImage, replyButtonWidth))
            if (depth < maxDepth) discussionEntry.replies?.forEach { buildEntry(it, depth + 1) }
        }

        // We only hide replies if the discussion is locked and has a future unlock date
        if(discussionTopicHeader.shouldShowReplies) discussionEntryList.forEach { buildEntry(it, 0) }

        // Append Footer - Don't do this in the loop to avoid String.replace() more than necessary
        builder.append(DiscussionHtmlTemplates.getFooter(context))
        return CanvasWebView.applyWorkAroundForDoubleSlashesAsUrlSource(builder.toString())
    }

    private fun build(
            context: Context,
            isTablet: Boolean,
            canvasContext: CanvasContext,
            discussionTopicHeader: DiscussionTopicHeader,
            discussionEntry: DiscussionEntry,
            converter: DiscussionEntryHtmlConverter,
            template: String,
            avatarImage: String,
            indent: Int,
            reachedViewableEnd: Boolean,
            brandColor: Int,
            likeColor: Int,
            likeImage: String,
            replyButtonWidth: String): String {

        return converter.buildHtml(
                context,
                isTablet,
                brandColor,
                likeColor,
                discussionEntry,
                template,
                avatarImage,
                allowReplies(canvasContext, discussionTopicHeader),
                allowEditing(canvasContext),
                allowLiking(canvasContext, discussionTopicHeader),
                discussionTopicHeader.allowRating,
                allowDeleting(canvasContext),
                reachedViewableEnd,
                indent,
                likeImage,
                replyButtonWidth,
                formatDeletedInfoText(context, discussionEntry))
    }

    //endregion

    //region Discussion Display Helpers

    fun launchIntent(context: Context, result: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result))
        // Make sure we can handle the intent
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, R.string.utils_unableToViewInBrowser, Toast.LENGTH_SHORT).show()
        }
    }

    fun getNewHTML(html: String, authenticatedSessionUrl: String?): String {
        if (authenticatedSessionUrl == null) return html
        // Now we need to swap out part of the old url for this new authenticated url
        val matcher = Pattern.compile("src=\"([^;]+)").matcher(html)
        var newHTML: String = html
        if (matcher.find()) {
            // We only want to change the urls that are part of an external tool, not everything (like avatars)
            for (index in 0..matcher.groupCount()) {
                val newUrl = matcher.group(index)
                if (newUrl.contains("external_tools")) {
                    newHTML = html.replace(newUrl, authenticatedSessionUrl)
                }
            }
        }
        return newHTML
    }

    /**
     * Checks to see if the webview element is within the viewable bounds of the scrollview.
     */
    fun isElementInViewPortWithinScrollView(context: Context, scrollView: ScrollView, webViewHeight: Int, scrollViewContentWrapperHeight: Int, elementHeight: Int, topOffset: Int): Boolean {
        val scrollBounds = Rect().apply { scrollView.getDrawingRect(this) }

        val otherContentHeight = scrollViewContentWrapperHeight - webViewHeight
        val top = context.DP(topOffset) + otherContentHeight
        val bottom = top + context.DP(elementHeight)

        return scrollBounds.top < top && scrollBounds.bottom > bottom
    }

    private fun allowReplies(canvasContext: CanvasContext?, header: DiscussionTopicHeader): Boolean {
        /*
            There are three related scenarios in which we don't want users to be able to reply,
            so we check that none of these conditions exist
            1) The discussion is Locked for an unknown reason.
            2) It's Locked due to a module/etc.
            3) User is an Observer in a course.
            4) IF it's a teacher we bag the entire rule book and let them reply.
        */

        val isCourse = canvasContext?.isCourse == true

        if (isCourse && (canvasContext as Course).isTeacher) return true

        val isLocked = header.locked
        val lockInfoEmpty = header.lockInfo == null || header.lockInfo!!.isEmpty
        val isObserver = isCourse && (canvasContext as Course).isObserver
        val hasPermission = header.permissions?.reply ?: false

        // If we are not Locked, do not have lock info, have permission, is a course, and not an observer...
        // - I suspect this can all be replaced with hasPermission, need to verify.
        return !isLocked && lockInfoEmpty && hasPermission && (isCourse || canvasContext?.isGroup == true) && !isObserver
    }

    private fun allowEditing(canvasContext: CanvasContext?): Boolean {
        // TODO - Update this when COMMS-868 is Completed
        return if (canvasContext?.type == CanvasContext.Type.COURSE) {
            (canvasContext as Course).isTeacher
        } else {
            false
        }
    }

    private fun allowLiking(canvasContext: CanvasContext?, header: DiscussionTopicHeader): Boolean {
        val isGrader = canvasContext is Course && (canvasContext.isTeacher || canvasContext.isTA)
        return header.allowRating && (
            !header.onlyGradersCanRate || isGrader
        )
    }

    private fun allowDeleting(canvasContext: CanvasContext?): Boolean {
        // TODO - Update this when COMMS-868 is Completed
        return if (canvasContext?.type == CanvasContext.Type.COURSE) {
            (canvasContext as Course).isTeacher
        } else {
            false
        }
    }

    fun getAssetsFile(context: Context, assetName: String): String {
        return tryOrNull { context.assets.open(assetName).bufferedReader().use { it.readText() } }
                ?: ""
    }

    fun getBitmapFromAssets(context: Context, filePath: String): Bitmap? {
        val assetManager = context.assets
        val inputStream: InputStream
        val bitmap: Bitmap?
        try {
            inputStream = assetManager.open(filePath)
            bitmap = BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            return null
        }
        return bitmap
    }

    fun makeBitmapForWebView(color: Int, bitmap: Bitmap?): String {
        if (bitmap == null) return ""
        val coloredBitmap = colorIt(color, bitmap)
        val outputStream = ByteArrayOutputStream()
        coloredBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        val imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
        coloredBitmap.recycle()
        return "data:image/png;base64," + imageBase64
    }

    fun colorIt(color: Int, map: Bitmap): Bitmap {
        val mutableBitmap = map.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint()
        paint.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        canvas.drawBitmap(mutableBitmap, 0f, 0f, paint)
        return mutableBitmap
    }

    fun getHexColorString(color: Int): String {
        return String.format("#%06X", (0xFFFFFF and color))
    }

    fun formatDeletedInfoText(context: Context, discussionEntry: DiscussionEntry): String {
        return if (discussionEntry.deleted) {
            val atSeparator = context.getString(R.string.at)
            val deletedText = String.format(context.getString(R.string.utils_discussionsDeleted),
                    DateHelper.getMonthDayAtTime(context, discussionEntry.updatedAt.toDate(), atSeparator))
            String.format("<div class=\"deleted_info\">%s</div>", deletedText)
        } else {
            ""
        }
    }

    /**
     * If the avatar is valid then returns an empty string. Otherwise...
     * Returns an avatar bitmap converted into a base64 string for webviews.
     */
    private fun makeAvatarForWebView(context: Context, discussionEntry: DiscussionEntry, isOnline: Boolean): String {
        if (!isOnline || discussionEntry.author != null && ProfileUtils.shouldLoadAltAvatarImage(discussionEntry.author!!.avatarImageUrl)) {
            val avatarBitmap = ProfileUtils.getInitialsAvatarBitMap(
                    context, discussionEntry.author!!.displayName!!,
                    Color.TRANSPARENT,
                    ContextCompat.getColor(context, R.color.licorice),
                    ContextCompat.getColor(context, R.color.borderMedium))
            val outputStream = ByteArrayOutputStream()
            avatarBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val byteArray = outputStream.toByteArray()
            val imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
            avatarBitmap.recycle()
            return "data:image/png;base64," + imageBase64
        } else {
            if (discussionEntry.author == null || discussionEntry.author!!.avatarImageUrl.isNullOrBlank()) {
                // Unknown author
                val avatarBitmap = ProfileUtils.getInitialsAvatarBitMap(
                        context, "?",
                        Color.TRANSPARENT,
                        ContextCompat.getColor(context, R.color.textDarkest),
                        ContextCompat.getColor(context, R.color.borderMedium))
                val outputStream = ByteArrayOutputStream()
                avatarBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                val byteArray = outputStream.toByteArray()
                val imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
                avatarBitmap.recycle()
                return "data:image/png;base64," + imageBase64
            }
            return discussionEntry.author?.avatarImageUrl ?: ""
        }
    }

    //endregion
}

private class DiscussionHtmlAsset(private val ltr: String, private val rtl: String) {

    private val isRtl: Boolean
        get() = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL

    fun text(context: Context): String {
        val assetName = if (isRtl) rtl else ltr
        return readAssets.getOrPut(assetName) {
            tryOrNull { context.assets.open(assetName).bufferedReader().use { it.readText() } } ?: ""
        }
    }

    companion object {
        private val readAssets = mutableMapOf<String, String>()
    }

}

object DiscussionHtmlTemplates {

    private val footerAsset = DiscussionHtmlAsset(
            ltr = "discussion_html_footer_item.html",
            rtl = "discussion_html_footer_item_rtl.html"
    )

    private val headerAsset = DiscussionHtmlAsset(
            ltr = "discussion_html_header_item.html",
            rtl = "discussion_html_header_item_rtl.html"
    )

    private val itemAsset = DiscussionHtmlAsset(
            ltr = "discussion_html_template_item.html",
            rtl = "discussion_html_template_item_rtl.html"
    )

    private val topicHeaderAsset = DiscussionHtmlAsset(
            ltr = "discussion_topic_header_html_template.html",
            rtl = "discussion_topic_header_html_template_rtl.html"
    )

    fun getFooter(context: Context) = footerAsset.text(context)
    fun getHeader(context: Context) = headerAsset.text(context)
    fun getItem(context: Context) = itemAsset.text(context)
    fun getTopicHeader(context: Context) = topicHeaderAsset.text(context)

}
