/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.pandautils.discussions;

import android.content.Context;
import android.text.TextUtils;

import com.instructure.canvasapi2.models.DiscussionEntry;
import com.instructure.canvasapi2.models.RemoteFile;
import com.instructure.canvasapi2.utils.DateHelper;
import com.instructure.canvasapi2.utils.FileUtils;
import com.instructure.canvasapi2.utils.Pronouns;
import com.instructure.pandautils.R;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.ProfileUtils;

import java.util.Locale;

@Deprecated
public class DiscussionEntryHTMLHelper {

    private static final String NO_PIC = "images/dotted_pic.png";
    private static final String ASSETS = "file:///android_asset";
    public static final String ASSET_IMAGES = "file:///android_asset";
    private static final String DRAWABLE = "file:///android_res/drawable";
    private static final String ATTACHMENT_ICON = "https://du11hjcvx0uqb.cloudfront.net/dist/images/inst_tree/file_types/page_white_picture-94db8424e5.png";

    private static String getContentHTML(DiscussionEntry discussionEntry, String message) {
        if (message == null || message.equals("null")) {
            return "";
        } else {
            StringBuilder html = new StringBuilder();
            html.append("<div class=\"width\">");
            html.append(message);

            for(RemoteFile attachment : discussionEntry.getAttachments()){
                if (!attachment.getHiddenForUser() && !discussionEntry.getDeleted()) {
                    html.append("<div class=\"nowrap\">");
                    html.append(String.format("<img class=\"attachment_img\" src=\"" + ATTACHMENT_ICON + "\" /> <a class=\"attachment_link\" href=\"%s\">%s</a>", attachment.getUrl(), attachment.getDisplayName()));
                    html.append("</div>");
                }
            }
            html.append("</div>");
            return html.toString();
        }
    }

    private static String getClickListener(int index) {
        if (index == -1) {
            return "";
        }
        return " onClick=\"onPressed('" + index + "')\"";
    }

    private static String getReplyListener(long index) {
        return " onClick=\"onReplyPressed('" + index + "')\"";
    }


    private static String getLikeListener(long index) {
        if(index == -1) {
            return "";
        }
        return " onClick=\"onLikePressed('" + index + "')\"";
    }

    public static String getUnreadCountHtmlLabel(Context context, DiscussionEntry discussionEntry) {
        if (discussionEntry.getUnreadChildren() > 0) {
            return String.format("<div class=\"is_unread\" id=\"is_unread_%d\">%s</div>", discussionEntry.getId(), formatUnreadCount(context, discussionEntry.getUnreadChildren()));
        } else {
            if(discussionEntry.getUnread()) {
                return String.format("<div class=\"is_unread\" id=\"is_unread_%d\">%s</div>", discussionEntry.getId(), context.getString(R.string.discussion_unread));
            }
            return "";
        }
    }

    public static String getCommentsCountHtmlLabel(Context context, DiscussionEntry discussionEntry, int subEntryCount) {
        return String.format("<div class=\"comments\" id =\"comments_%d\">%s</div>",discussionEntry.getId(), formatCommentsCount(context, subEntryCount));
    }

    public static String getHTML(DiscussionEntry discussionEntry, Context context, int index, String deletedString, String colorString, boolean shouldAllowRating, boolean canReply, int currentRatingForUser, String likeString, int subEntryCount) {
        return getHTML(discussionEntry, context, index, deletedString, colorString, false, shouldAllowRating, canReply, currentRatingForUser, likeString, subEntryCount);
    }

    /**
     * Here we replace html with data, fun...
     */
    public static String getHTML(DiscussionEntry discussionEntry, Context context, int index, String deletedString, String colorString, boolean isForbidden, boolean shouldAllowRating, boolean canReply, int currentRatingForUser, String likeString, int subEntryCount) {
        String unread = getUnreadCountHtmlLabel(context, discussionEntry);
        String comments = getCommentsCountHtmlLabel(context, discussionEntry, subEntryCount);
        String listener = getClickListener(index);
        String replyListener = getReplyListener(index);
        String likeListener = getLikeListener(index);
        String avatarUrl = "";
        String avatarVisibility = "display: block;";
        String title = "";
        String date = "";
        String message = discussionEntry.getMessage("<p>" + deletedString + "</p>");
        String content = getContentHTML(discussionEntry, message);
        String status = discussionEntry.getUnread() ? "unread_message" : "read_message";
        String classString = "parent";
        String displayInitials = "";
        String borderString = "";
        String reply = formatReplyText(context, canReply);
        String userId;
        String detailsWrapperStyle = "display: block;";

        //Prevents the topic from appearing as deleted if the editorId was not set
        if(index == -1 && discussionEntry.getUserId() == 0) {
            userId = Integer.toString(index);
        } else {
            if(discussionEntry.getUserId() == 0 && discussionEntry.getEditorId() != 0) {
                userId = Long.toString(discussionEntry.getEditorId());
            } else {
                userId = Long.toString(discussionEntry.getUserId());
            }
        }

        String forbiddenHtml = isForbidden ? getForbiddenHtml(context.getString(R.string.forbidden)) : "";

        if (index >= 0) {
            classString = "child";
        }

        if (discussionEntry.getDeleted()) {
            avatarUrl = "background: " + colorString + " url(\"file:///android_res/drawable/ic_default_avatar.png\");";
            if(discussionEntry.getAuthor() != null && !TextUtils.isEmpty(discussionEntry.getAuthor().getDisplayName())) {
                String name = Pronouns.html(
                        discussionEntry.getAuthor().getDisplayName(),
                        discussionEntry.getAuthor().getPronouns()
                );
                title = String.format(Locale.getDefault(), context.getString(R.string.deleted_by), name);
            } else {
                title = deletedString;
            }
            content = reply = comments = likeString = "";
            detailsWrapperStyle = "display: none;";
            avatarVisibility = "display: none;";
        } else {
            if (discussionEntry.getAuthor() != null) {
                if(!isEmptyImage(discussionEntry.getAuthor().getAvatarImageUrl())){
                    avatarUrl = "background-image:url(" + discussionEntry.getAuthor().getAvatarImageUrl() + ");";
                    borderString = "border: 0px solid " + colorString + ";";
                }else {
                    avatarUrl = "background: transparent;";
                    displayInitials = ProfileUtils.getUserInitials(discussionEntry.getAuthor().getDisplayName());
                }
            }else {
                avatarUrl = "background: " + colorString + " url(\"file:///android_res/drawable/ic_default_avatar.png\");";
                borderString = "border: 0px solid " + colorString + ";";
            }

            if (discussionEntry.getAuthor() != null && discussionEntry.getAuthor().getDisplayName() != null) {
                title = Pronouns.html(
                        discussionEntry.getAuthor().getDisplayName(),
                        discussionEntry.getAuthor().getPronouns()
                );
            } else if (discussionEntry.getDescription() != null) {    //Graded discussion.
                title = discussionEntry.getDescription();
            }
            //if title is still null set it to an empty string
            if(title == null) {
                title = "";
            }

            //Check if we can like, replay, and if there are comments
            if(discussionEntry.getTotalChildren() == 0 && !canReply && !shouldAllowRating) {
                //Hide detailsWrapper
                detailsWrapperStyle = "display: none;";
            }
        }

        if (discussionEntry.getUpdatedAt() != null) {
            date = DateHelper.getDateTimeString(context, discussionEntry.getUpdatedDate());
        }

        return (FileUtils.getAssetsFile(context, "discussion_html_template.html")
                .replace("__BORDER_STRING__", borderString)
                .replace("__COLOR__", "background: " + colorString + ";")
                .replace("__LISTENER_HTML__", listener)
                .replace("__LIKE_LISTENER__", likeListener)
                .replace("__REPLY_LISTENER__", replyListener)
                .replace("__AVATAR_URL__", avatarUrl)
                .replace("__AVATAR_VISIBILITY__", avatarVisibility)
                .replace("__TITLE__", title)
                .replace("__DATE__", date)
                .replace("__LIKES_TEXT__", likeString)
                .replace("__UNREAD_COUNT__", unread)
                .replace("__CONTENT_HTML__", content)
                .replace("__CLASS__", classString)
                .replace("__STATUS__", status)
                .replace("__FORBIDDEN__", forbiddenHtml)
                .replace("__HEADER_ID__", Long.toString(discussionEntry.getId()))
                .replace("__ENTRY_ID__", Long.toString(discussionEntry.getId()))
                .replace("__USER_ID__", userId)
                .replace("__USER_INITIALS__", displayInitials)
                .replace("__REPLY_TEXT__", reply)
                .replace("__COMMENTS_COUNT__", comments)
                .replace("__DETAILS_WRAPPER__", detailsWrapperStyle)
                .replace("__INDEX__", Integer.toString(index))
                .replace("__READ_STATE__", getReadState(discussionEntry)));
    }

    public static String getReadState(DiscussionEntry discussionEntry) {
        return discussionEntry.getUnread() ? "unread" : "read";
    }

    /*
     * Displays text to the user when they have to post before viewing replies
     */
    private static String getForbiddenHtml(String forbiddenText) {
        return String.format("<div class=\"forbidden\"><p>%s</p></div>", forbiddenText);
    }

    public static boolean isEmptyImage(String avatarURL){
        if(TextUtils.isEmpty(avatarURL)) return true;
        return avatarURL.contains(NO_PIC) || avatarURL.contains(Const.PROFILE_URL);
    }

    private static String formatCommentsCount(Context context, int count) {
        if(count == 0) {
            return "";
        } else if(count > 1) {
            return String.format(Locale.getDefault(), context.getString(R.string.word_space_word), String.valueOf(count), context.getString(R.string.discussion_responses));
        } else {
            return String.format(Locale.getDefault(), context.getString(R.string.word_space_word), String.valueOf(count), context.getString(R.string.discussion_response));
        }
    }

    private static String formatUnreadCount(Context context, int count) {
        if(count == 0) {
            return "";
        } else {
            return String.format(Locale.getDefault(), context.getString(R.string.word_space_word), String.valueOf(count), context.getString(R.string.discussion_unread));
        }
    }

    private static String formatReplyText(Context context, boolean canReply) {
        if(canReply) {
            return String.format("<div class=\"reply\">%s</div>", context.getString(R.string.discussion_reply));
        }
        return "";
    }
}
