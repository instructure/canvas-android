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
package com.instructure.pandautils.discussions

import android.content.Context
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.localized
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.BuildConfig
import com.instructure.pandautils.R

/**
 * Used to convert DiscussionEntries into HTML. Typically this class only takes data and does little calculation.
 * Be careful doing heavy work within here since this will get used in some form of loop.
 */
class DiscussionEntryHtmlConverter {

    fun buildHtml(
            context: Context,
            isTablet: Boolean,
            brandColor: Int,
            likeColor: Int,
            discussionEntry: DiscussionEntry,
            template: String,
            avatarImage: String,
            canReply: Boolean,
            canEdit: Boolean,
            allowLiking: Boolean,
            showLikes: Boolean,
            canDelete: Boolean,
            reachedViewableEnd: Boolean,
            indent: Int,
            likeImage: String,
            replyButtonWidth: String,
            deletedText: String): String {

        val repliesButtonText = context.resources.getQuantityString(R.plurals.utils_discussionsReplies, discussionEntry.totalChildren, discussionEntry.totalChildren.localized)
        val likeCountLabel = getLikeCountText(context, discussionEntry)
        val likeEntryLabel = context.resources.getString(R.string.likeEntryLabel)

        val htmlListener = getItemClickListener(discussionEntry.id.toString())
        val replyListener = getReplyListener(discussionEntry.id.toString())
        val menuListener = getMenuListener(discussionEntry.id.toString())
        val likeListener = getLikeListener(discussionEntry.id.toString())
        val avatarListener = getAvatarListener(discussionEntry.id.toString())
        val attachmentListener = getAttachmentListener(discussionEntry.id.toString())
        val moreRepliesListener = getMoreRepliesListener(discussionEntry.id.toString())

        val ltiButtonWidth = if(isTablet) "320px" else "100%"
        val ltiButtonMargin = if(isTablet) "0px" else "auto"

        var authorName: String
        var date = ""
        var content = getContentHTML(discussionEntry.getMessage(""))
        val reply = formatReplyText(context, canReply)
        val menu = formatMenuText(context, canEdit, canDelete)

        val menuDivider = formatMenuDividerText(canEdit, canDelete)
        val userId: String
        val replyButtonWrapperStyle: String
        var detailsWrapperStyle = "display: block;"
        var indentDisplay1 = "display: none;"
        var indentDisplay2 = "display: none;"
        var indentDisplay3 = "display: none;"
        var indentDisplay4 = "display: none;"
        var indentDisplay5 = "display: none;"
        val attachments = getAttachments(discussionEntry)

        val showingLikes = if(showLikes) "display: flex;" else "display: none;"
        val liking = if(allowLiking) "display: flex;" else "display: none;"
        val likeCountShort = if(discussionEntry.ratingSum == 0) "" else "(" + discussionEntry.ratingSum.localized + ")"
        val likeSum = if(allowLiking) likeCountShort else likeCountLabel
        val likingIcon = if(discussionEntry._hasRated) likeImage else "file:///android_asset/discussion_unliked.png"
        val likingColor = if(discussionEntry._hasRated) brandColor else likeColor

        // Prevents the topic from appearing as deleted if the editorId was not set
        userId = if (discussionEntry.userId == 0L && discussionEntry.editorId != 0L) {
            discussionEntry.editorId.toString()
        } else {
            discussionEntry.userId.toString()
        }

        // Check if we can like, reply, and if there are comments
        if (discussionEntry.totalChildren == 0) {
            if(!canReply && !allowLiking) {
                //Hide detailsWrapper
                detailsWrapperStyle = "display: none;"
            }
        }

        replyButtonWrapperStyle = if(reachedViewableEnd && discussionEntry.totalChildren > 0) {
            "display: block;"
        } else {
            "display: none;"
        }

        //Sets up indentation based on the indent param
        // Hide details_wrapper
        // Hide the message content just in case
        when (indent) {
            0 -> {
                indentDisplay1 = "display: none;"
                indentDisplay2 = "display: none;"
                indentDisplay3 = "display: none;"
                indentDisplay4 = "display: none;"
                indentDisplay5 = "display: none;"
            }
            1 -> {
                indentDisplay1 = "display: inline;"
                indentDisplay2 = "display: none;"
                indentDisplay3 = "display: none;"
                indentDisplay4 = "display: none;"
                indentDisplay5 = "display: none;"
            }
            2 -> {
                indentDisplay1 = "display: inline;"
                indentDisplay2 = "display: inline;"
                indentDisplay3 = "display: none;"
                indentDisplay4 = "display: none;"
                indentDisplay5 = "display: none;"
            }
            3 -> {
                indentDisplay1 = "display: inline;"
                indentDisplay2 = "display: inline;"
                indentDisplay3 = "display: inline;"
                indentDisplay4 = "display: none;"
                indentDisplay5 = "display: none;"
            }
            4 -> {
                indentDisplay1 = "display: inline;"
                indentDisplay2 = "display: inline;"
                indentDisplay3 = "display: inline;"
                indentDisplay4 = "display: inline;"
                indentDisplay5 = "display: none;"
            }
            5 -> {
                indentDisplay1 = "display: inline;"
                indentDisplay2 = "display: inline;"
                indentDisplay3 = "display: inline;"
                indentDisplay4 = "display: inline;"
                indentDisplay5 = "display: inline;"
            }

            // Populate Author Information
        }

        // Populate Author Information
        if(discussionEntry.author != null) {
            authorName = Pronouns.html(discussionEntry.author?.displayName, discussionEntry.author?.pronouns)
            if(BuildConfig.DEBUG) authorName = "$authorName ${discussionEntry.id}"
        } else {
            authorName = context.getString(R.string.utils_discussionsUnknownAuthor)
        }

        if(discussionEntry.deleted) {
            // Hide details_wrapper
            detailsWrapperStyle = "display: none;"
            // Hide the message content just in case
            content = ""
            date = deletedText
        } else if(discussionEntry.updatedAt != null) {
            date = DateHelper.getDateTimeString(context, discussionEntry.updatedAt.toDate())!!
        }

        return template
                .replace("__GROUP__", "display: block;")
                .replace("__LIKE_ICON__", likingIcon)
                .replace("__LIKE_COUNT_SHORT__", likeCountShort)
                .replace("__LIKE_COUNT__", likeSum)
                .replace("__LIKE_ARIA__", likeEntryLabel)
                .replace("__LIKE_CHECKED_ARIA__", discussionEntry._hasRated.toString())
                .replace("__LIKE_COUNT_ARIA__", likeCountLabel)
                .replace("__LIKE_ALLOWED__", liking)
                .replace("__SHOW_LIKES__", showingLikes)
                .replace("__LIKE_COLOR__", colorToHex(likingColor))
                .replace("__BRAND_COLOR__", colorToHex(brandColor))
                .replace("__ATTACHMENTS_WRAPPER__", attachments)
                .replace("__INDENT_DISPLAY_1__", indentDisplay1)
                .replace("__INDENT_DISPLAY_2__", indentDisplay2)
                .replace("__INDENT_DISPLAY_3__", indentDisplay3)
                .replace("__INDENT_DISPLAY_4__", indentDisplay4)
                .replace("__INDENT_DISPLAY_5__", indentDisplay5)
                .replace("__REPLY_BUTTON_TEXT__", repliesButtonText)
                .replace("__REPLY_BUTTON_WIDTH__", replyButtonWidth)

                .replace("__HTML_LISTENER__", htmlListener)
                .replace("__AVATAR_LISTENER__", avatarListener)
                .replace("__ATTACHMENT_LISTENER__", attachmentListener)
                .replace("__REPLY_LISTENER__", replyListener)
                .replace("__MENU_LISTENER__", menuListener)
                .replace("__LIKE_LISTENER__", likeListener)
                .replace("__MORE_REPLIES_LISTENER__", moreRepliesListener)

                .replace("", "")
                .replace("__LTI_BUTTON_WIDTH__", ltiButtonWidth)
                .replace("__LTI_BUTTON_MARGIN__", ltiButtonMargin)

                .replace("__AVATAR_URL__", avatarImage)
                .replace("__AVATAR_ALT__", context.getString(R.string.userAvatar))
                .replace("__TITLE__", authorName)
                .replace("__DATE__", date)
                .replace("__CONTENT_HTML__", content)
                .replace("__HEADER_ID__", discussionEntry.id.toString())
                .replace("__ENTRY_ID__", discussionEntry.id.toString())
                .replace("__USER_ID__", userId)
                .replace("__REPLY_TEXT__", reply)
                .replace("__MENU_TEXT__", menu)
                .replace("__MENU_DIVIDER__", menuDivider)
                .replace("__DETAILS_WRAPPER__", detailsWrapperStyle)
                .replace("__REPLY_BUTTON_WRAPPER__", replyButtonWrapperStyle)
                .replace("__READ_STATE__", getReadState(discussionEntry))
    }

    private fun getContentHTML(message: String?): String = if (message == null || message == "null") "" else message

    private fun getAttachments(discussionEntry: DiscussionEntry): String {
        val display = "display: none;"
        if(discussionEntry.deleted) return display
        discussionEntry.attachments?.firstOrNull() ?: return display
        discussionEntry.attachments?.filter { !it.hiddenForUser }?.forEach {
            // Display the attachment icon if we have at least one not hidden for the user
            return "display: inline;"
        }
        return display
    }

    private fun getItemClickListener(id: String): String = " onClick=\"onItemPressed('$id')\""
    private fun getReplyListener(id: String): String = " onClick=\"onReplyPressed('$id')\""
    private fun getMenuListener(id: String): String = " onClick=\"onMenuPressed('$id')\""
    private fun getAvatarListener(id: String): String = " onClick=\"onAvatarPressed('$id')\""
    private fun getLikeListener(id: String): String = " onClick=\"onLikePressed('$id')\""
    private fun getAttachmentListener(id: String): String = " onClick=\"onAttachmentPressed('$id')\""
    private fun getMoreRepliesListener(id: String): String = " onClick=\"onMoreRepliesPressed('$id')\""

    fun getReadState(discussionEntry: DiscussionEntry): String {
        // Hidden strings to identify if a discussion entry was read or unread
        return if (discussionEntry.unread) "unread" else "read"
    }

    private fun colorToHex(color: Int): String {
        return "#" + Integer.toHexString(color).substring(2)
    }

    private fun formatReplyText(context: Context, canReply: Boolean): String {
        if (canReply) {
            return String.format("<div class=\"reply\">%s</div>", context.getString(R.string.utils_discussionsReply))
        }
        return ""
    }

    private fun formatMenuText(context: Context, canEdit: Boolean, canDelete: Boolean): String {
        if (canEdit && canDelete) {
            val menuLabel = context.getString(R.string.utils_contentDescriptionDiscussionsOverflow)
            return """
            <div class="meatball_wrapper menu">
                <div aria-label="$menuLabel" role="button">
                    <div class="meatball" aria-hidden="true"></div>
                    <div class="meatball" aria-hidden="true"></div>
                    <div class="meatball" aria-hidden="true"></div>
                </div>
            </div>
            """.trimIndent()
        }
        return ""
    }

    private fun formatMenuDividerText(canEdit: Boolean, canDelete: Boolean): String {
        if (canEdit && canDelete) {
            return String.format("<div class=\"delete_vertical_divider\">%s</div>", "|")
        }
        return ""
    }

    companion object {
        fun getLikeCountText(context: Context, discussionEntry: DiscussionEntry): String {
            return if (discussionEntry.ratingSum == 0) "" else context.resources.getQuantityString(
                R.plurals.likeCountLabel,
                discussionEntry.ratingSum,
                discussionEntry.ratingSum.localized
            )
        }
    }
}
