/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/* This is an auto-generated file. */

package com.instructure.teacher.ui.models;

import java.util.Date;

public class Discussion {
    public static final String SIDE_COMMENT = "side_comment";
    public static final String THREADED = "threaded";

    public int id;
    public String title;
    public Date lastReplyAt;
    public Date delayedPostAt;
    public Date postedAt;
    public int assignmentId;
    public int rootTopicId;
    public int position;
    public boolean podcastHasStudentPosts;
    public String discussionType;
    public Date lockAt;
    public boolean allowRating;
    public boolean onlyGradersCanRate;
    public boolean sortByRating;
    public String userName;
    public int discussionSubentryCount;
    // TODO: permissions
    public boolean requireInitialPost;
    public boolean userCanSeePosts;
    public String podcastUrl;
    public String readState;
    public int unreadCount;
    public boolean subscribed;
    // TODO: topicChildren, attachments
    public boolean published;
    public boolean canUnpublish;
    public boolean locked;
    public boolean canLock;
    public boolean commentsDisabled;
    public String authorId;
    public String authorDisplayName;
    public String htmlUrl;
    public boolean pinned;
    public int groupCategoryId;
    public boolean canGroup;
    public boolean lockedForUser;
    public String message;
    public boolean isAnnouncement;

    public Discussion(int id, String title, Date lastReplyAt, Date delayedPostAt, Date postedAt, int assignmentId, int rootTopicId,
                      int position, boolean podcastHasStudentPosts, String discussionType, Date lockAt, boolean allowRating,
                      boolean onlyGradersCanRate, boolean sortByRating, String userName, int discussionSubentryCount,
                      boolean requireInitialPost, boolean userCanSeePosts, String podcastUrl, String readState, int unreadCount,
                      boolean subscribed, boolean published, boolean canUnpublish, boolean locked, boolean canLock,
                      boolean commentsDisabled, String authorId, String authorDisplayName, String htmlUrl, boolean pinned,
                      int groupCategoryId, boolean canGroup, boolean lockedForUser, String message, boolean isAnnouncement) {
        this.id = id;
        this.title = title;
        this.lastReplyAt = lastReplyAt;
        this.delayedPostAt = delayedPostAt;
        this.postedAt = postedAt;
        this.assignmentId = assignmentId;
        this.rootTopicId = rootTopicId;
        this.position = position;
        this.podcastHasStudentPosts = podcastHasStudentPosts;
        this.discussionType = discussionType;
        this.lockAt = lockAt;
        this.allowRating = allowRating;
        this.onlyGradersCanRate = onlyGradersCanRate;
        this.sortByRating = sortByRating;
        this.userName = userName;
        this.discussionSubentryCount = discussionSubentryCount;
        this.requireInitialPost = requireInitialPost;
        this.userCanSeePosts = userCanSeePosts;
        this.podcastUrl = podcastUrl;
        this.readState = readState;
        this.unreadCount = unreadCount;
        this.subscribed = subscribed;
        this.published = published;
        this.canUnpublish = canUnpublish;
        this.locked = locked;
        this.canLock = canLock;
        this.commentsDisabled = commentsDisabled;
        this.authorId = authorId;
        this.authorDisplayName = authorDisplayName;
        this.htmlUrl = htmlUrl;
        this.pinned = pinned;
        this.groupCategoryId = groupCategoryId;
        this.canGroup = canGroup;
        this.lockedForUser = lockedForUser;
        this.message = message;
        this.isAnnouncement = isAnnouncement;
    }
}
