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

package com.instructure.canvasapi.model;

import android.content.Context;
import android.os.Parcel;

import com.google.gson.annotations.SerializedName;
import com.instructure.canvasapi.R;
import com.instructure.canvasapi.utilities.APIHelpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class StreamItem extends CanvasModel<StreamItem> {

    public enum Type { DISCUSSION_TOPIC, SUBMISSION, ANNOUNCEMENT, CONVERSATION, MESSAGE, CONFERENCE, COLLABORATION, COLLECTION_ITEM, UNKNOWN, NOT_SET;
        public static boolean isDiscussionTopic(StreamItem streamItem) {return streamItem.getType() == DISCUSSION_TOPIC;}
        public static boolean isSubmission(StreamItem streamItem) {return streamItem.getType() == SUBMISSION;}
        public static boolean isAnnouncement(StreamItem streamItem) {return streamItem.getType() == ANNOUNCEMENT;}
        public static boolean isConversation(StreamItem streamItem) {return streamItem.getType() == CONVERSATION;}
        public static boolean isMessage(StreamItem streamItem) {return streamItem.getType() == MESSAGE;}
        public static boolean isConference(StreamItem streamItem) {return streamItem.getType() == CONFERENCE;}
        public static boolean isCollaboration(StreamItem streamItem) {return streamItem.getType() == COLLABORATION;}
        public static boolean isCollectionItem(StreamItem streamItem) {return streamItem.getType() == COLLECTION_ITEM;}
        public static boolean isUnknown(StreamItem streamItem) {return streamItem.getType() == UNKNOWN;}
        public static boolean isNotSet(StreamItem streamItem) {return streamItem.getType() == NOT_SET;}
    }

    // general info returned by the API
    private String updated_at;
    private long id;
    private String title;
    private String message;
    private String type;
    private String context_type;
    private boolean read_state;
    private String url;
    private String html_url;
    private long course_id = -1;
    private long group_id = -1;
    private long assignment_id = -1;

    // message type, which is not a conversation, but stream notifications
    private long message_id;
    private String notification_category;

    // conversation
    private long conversation_id;
    @SerializedName("private")
    private boolean isPrivate;
    private int participant_count;

    // discussionTopic or announcement
    private long discussion_topic_id = -1;
    private long announcement_id;
    private int total_root_discussion_entries;
    private boolean require_initial_post;
    private boolean user_has_posted;
    private List<DiscussionEntry> root_discussion_entries = new ArrayList<DiscussionEntry>();

    // submission
    private int attempt;
    private String body;
    private String grade;
    private boolean grade_matches_current_submission;
    private String graded_at;
    private long grader_id;
    private double score = -1.0;
    private String submission_type;
    private String submitted_at;
    private String workflow_state;
    private boolean late;
    private String preview_url;
    private List<SubmissionComment> submission_comments = new ArrayList<SubmissionComment>();
    private CanvasContext canvasContext;
    private Assignment assignment;
    private long user_id;
    private User user;

    // helper fields
    private Type enumType = Type.NOT_SET;
    private CanvasContext.Type canvasContextType = CanvasContext.Type.USER;
    private boolean hasSetContextType = false;
    private Date updatedAtDate;
    private Date gradedAtDate;
    private Date submittedAtDate;
    private Conversation conversation;
    private boolean isChecked;

    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides
    ///////////////////////////////////////////////////////////////////////////

    // We want opposite of natural sorting order of date since we want the newest one to come first
    @Override
    public Date getComparisonDate() {
        return  getUpdatedAtDate();
    }

    @Override
    public String getComparisonString() {
        return title;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Getters
    ///////////////////////////////////////////////////////////////////////////

    public Date getUpdatedAtDate() {
        if (updatedAtDate == null) {
            updatedAtDate = APIHelpers.stringToDate(updated_at);
        }
        return updatedAtDate;
    }
    public long getId() {
        return id;
    }
    public String getTitle(Context context) {
        if (title == null && getType() == Type.CONVERSATION) {
            title = context.getString(R.string.Message);
        }
        return title;
    }
    public String getMessage(Context context) {
        if (message == null) {
            message = createMessage(context);
        }
        return message;
    }
    public Type getType() {
        if (enumType == Type.NOT_SET) {
            enumType = typeFromString(type);
        }
        return enumType;
    }
    public CanvasContext.Type getContextType() {
        if (!hasSetContextType) {
            if (context_type != null && (context_type.toLowerCase().equals("course") || course_id > 0)) {
                canvasContextType = CanvasContext.Type.COURSE;
            } else if (context_type != null && (context_type.toLowerCase().equals("group") || group_id > 0)){
                canvasContextType = CanvasContext.Type.GROUP;
            }
            hasSetContextType = true;
        }

        return canvasContextType;
    }
    public boolean isReadState() {
        return read_state;
    }

    //helper method to show that the stream item has been read without having to reload all the data.
    //this method does not get the data from the server, so make sure item is actually read.
    public void setReadState(boolean readState) {
        read_state = readState;
    }
    public String getUrl() {
        return url;
    }
    public String getHtmlUrl() {
        return html_url;
    }
    public long getCourseId() {
        if (getContextType() == CanvasContext.Type.COURSE && course_id == -1) {
            course_id = parseCourseId();
        }
        return course_id;
    }

    public long getGroupId() {
        if (getContextType() == CanvasContext.Type.GROUP && group_id == -1) {
            group_id = parseGroupId();
        }
        return group_id;
    }
    public long getAssignmentId() {
        if (getContextType() == CanvasContext.Type.COURSE && assignment_id == -1) {
            assignment_id = parseAssignmentId();
        }
        return assignment_id;
    }
    public long getMessageId() {
        return message_id;
    }
    public String getNotificationCategory() {
        return notification_category;
    }
    public long getConversationId() {
        return conversation_id;
    }
    public boolean isPrivate() {
        return isPrivate;
    }
    public int getParticipantCount() {
        return participant_count;
    }
    public long getDiscussionTopicId() {
        if (discussion_topic_id == -1) {
            return announcement_id;
        }
        return discussion_topic_id;
    }
    public int getTotalRootDiscussionEntries() {
        return total_root_discussion_entries;
    }
    public boolean requiresInitialPost() {
        return require_initial_post;
    }
    public boolean userHasPosted() {
        return user_has_posted;
    }
    public List<DiscussionEntry> getRootDiscussionEntries() {
        return root_discussion_entries;
    }
    public int getAttempt() {
        return attempt;
    }
    public String getBody() {
        return body;
    }

    public String getGrade() {
        return grade;
    }

    public boolean gradeMatchesCurrentSubmission() {
        return grade_matches_current_submission;
    }
    public Date getGradedAt() {
        if (gradedAtDate == null) {
            gradedAtDate = APIHelpers.stringToDate(graded_at);
        }
        return gradedAtDate;
    }
    public long getGraderId() {
        return grader_id;
    }
    public double getScore() {
        return score;
    }
    public String getSubmissionType() {
        return submission_type;
    }
    public Date getSubmittedAt() {
        if (submittedAtDate == null) {
            submittedAtDate = APIHelpers.stringToDate(submitted_at);
        }
        return submittedAtDate;
    }

    public String getWorkflowState() {
        return workflow_state;
    }

    public boolean isLate() {
        return late;
    }

    public String getPreviewUrl() {
        return preview_url;
    }

    public List<SubmissionComment> getSubmissionComments() {
        return submission_comments;
    }

    public CanvasContext getCanvasContext() {
        return canvasContext;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public long getUser_id() {
        return user_id;
    }

    public User getUser() {
        return user;
    }
    public Conversation getConversation() {
        return conversation;
    }
    public boolean isChecked() {
        return isChecked;
    }
    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    private Type typeFromString(String type) {
        if(type.toLowerCase().equals("conversation")) {
            return Type.CONVERSATION;
        } else if(type.toLowerCase().equals("submission")) {
            return Type.SUBMISSION;
        } else if(type.toLowerCase().equals("discussiontopic")) {
            return Type.DISCUSSION_TOPIC;
        } else if (type.toLowerCase().equals("announcement")){
            return Type.ANNOUNCEMENT;
        } else if(type.toLowerCase().equals("message")) {
            return Type.MESSAGE;
        } else if(type.toLowerCase().equals("conference")) {
            return Type.CONFERENCE;
        } else if(type.toLowerCase().equals("webconference")) {
            return Type.CONFERENCE;
        } else if(type.toLowerCase().equals("collaboration")) {
            return Type.COLLABORATION;
        } else if(type.toLowerCase().equals("collectionitem")) {
            return Type.COLLECTION_ITEM;
        }
        return Type.UNKNOWN;
    }

    public void setConversation(Context context, Conversation conversation, long myUserId, String monologueDefault) {

        if(APIHelpers.paramIsNull(context, conversation,monologueDefault)){
            return;
        }

        this.conversation = conversation;
        title = conversation.getMessageTitle(context, myUserId,monologueDefault);
        message = createMessage(context);
    }

    public void setCanvasContextFromMap(Map<Long, Course> courseMap, Map<Long, Group> groupMap) {
        if (getContextType() == CanvasContext.Type.COURSE) {
            canvasContext = courseMap.get(getCourseId());
        } else {
            canvasContext = groupMap.get(getGroupId());
        }
    }

    private String createMessage(Context context) {
        switch (getType()) {
            case CONVERSATION:
                if (conversation == null) {
                    return context.getString(R.string.Loading);
                } else if (conversation.getLastMessagePreview() == null) {
                    return "";
                }
                return conversation.getLastMessagePreview();
            case SUBMISSION:
                //get comments from assignment
                String comment = null;
                if (submission_comments.size()> 0) {
                    comment = submission_comments.get(submission_comments.size() - 1).getComment();
                }
                //set it to the last comment if it's not null
                if(comment != null && !comment.equals("null") && score != -1.0) {
                    return (":" + score + " " + comment);
                }
                else if((comment == null || comment.equals("null")) && score != -1.0){
                    return (":" + score);
                }
                else if(comment != null && !comment.equals("null") && score == -1.0) {
                    return (comment);
                }
                break;
            case DISCUSSION_TOPIC :
                //if it's a discussionTopic, get the last entry for the message.
                if (root_discussion_entries.size() > 0) {
                    return root_discussion_entries.get(root_discussion_entries.size() - 1).getMessage(context.getString(R.string.Deleted));
                }
                break;
            default:
                break;
        }

        if (message == null) {
            return "";
        }
        return message;
    }

    private long parseAssignmentId() {
        //get the assignment from the url
        if(html_url.length() > 0 && !html_url.equals("null")) {
            String searchFor = "assignments/";
            int start = html_url.indexOf(searchFor);
            if (start == -1) {
                return 0;
            }
            start += searchFor.length();
            int end = html_url.indexOf("/", start);
            //in some urls the assignmentID might be the last thing so there wouldn't be a final /
            if(end == -1) {
                end = html_url.length();
            }
            String assignmentId = html_url.substring(start,end);

            return Long.parseLong(assignmentId);
        }
        return 0;
    }

    private long parseCourseId() {
        if(html_url.length() > 0 && !html_url.equals("null")) {
            String searchFor = "courses/";
            int start = html_url.indexOf(searchFor);
            if (start == -1) {
                return 0;
            }
            start += searchFor.length();
            int end = html_url.indexOf("/", start);

            String courseIdString = html_url.substring(start,end);

            return Long.parseLong(courseIdString);
        }
        return 0;
    }

    private long parseGroupId() {
        if(html_url.length() > 0 && !html_url.equals("null")) {
            String searchFor = "groups/";
            int start = html_url.indexOf(searchFor);
            if (start == -1) {
                return 0;
            }
            start += searchFor.length();
            int end = html_url.indexOf("/", start);

            String groupIdString = html_url.substring(start,end);

            return Long.parseLong(groupIdString);
        }
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.updated_at);
        dest.writeLong(this.id);
        dest.writeString(this.title);
        dest.writeString(this.message);
        dest.writeString(this.type);
        dest.writeString(this.context_type);
        dest.writeByte(read_state ? (byte) 1 : (byte) 0);
        dest.writeString(this.url);
        dest.writeString(this.html_url);
        dest.writeLong(this.course_id);
        dest.writeLong(this.group_id);
        dest.writeLong(this.assignment_id);
        dest.writeLong(this.message_id);
        dest.writeString(this.notification_category);
        dest.writeLong(this.conversation_id);
        dest.writeByte(isPrivate ? (byte) 1 : (byte) 0);
        dest.writeInt(this.participant_count);
        dest.writeLong(this.discussion_topic_id);
        dest.writeLong(this.announcement_id);
        dest.writeInt(this.total_root_discussion_entries);
        dest.writeByte(require_initial_post ? (byte) 1 : (byte) 0);
        dest.writeByte(user_has_posted ? (byte) 1 : (byte) 0);
        dest.writeList(this.root_discussion_entries);
        dest.writeInt(this.attempt);
        dest.writeString(this.body);
        dest.writeString(this.grade);
        dest.writeByte(grade_matches_current_submission ? (byte) 1 : (byte) 0);
        dest.writeString(this.graded_at);
        dest.writeLong(this.grader_id);
        dest.writeDouble(this.score);
        dest.writeString(this.submission_type);
        dest.writeString(this.submitted_at);
        dest.writeString(this.workflow_state);
        dest.writeByte(late ? (byte) 1 : (byte) 0);
        dest.writeString(this.preview_url);
        dest.writeList(this.submission_comments);
        dest.writeParcelable(this.canvasContext, 0);
        dest.writeParcelable(this.assignment, flags);
        dest.writeLong(this.user_id);
        dest.writeParcelable(this.user, 0);
        dest.writeInt(this.enumType == null ? -1 : this.enumType.ordinal());
        dest.writeInt(this.canvasContextType == null ? -1 : this.canvasContextType.ordinal());
        dest.writeByte(hasSetContextType ? (byte) 1 : (byte) 0);
        dest.writeLong(updatedAtDate != null ? updatedAtDate.getTime() : -1);
        dest.writeLong(gradedAtDate != null ? gradedAtDate.getTime() : -1);
        dest.writeLong(submittedAtDate != null ? submittedAtDate.getTime() : -1);
        dest.writeParcelable(this.conversation, flags);
        dest.writeByte(isChecked ? (byte) 1 : (byte) 0);
    }

    public StreamItem() {
    }

    private StreamItem(Parcel in) {
        this.updated_at = in.readString();
        this.id = in.readLong();
        this.title = in.readString();
        this.message = in.readString();
        this.type = in.readString();
        this.context_type = in.readString();
        this.read_state = in.readByte() != 0;
        this.url = in.readString();
        this.html_url = in.readString();
        this.course_id = in.readLong();
        this.group_id = in.readLong();
        this.assignment_id = in.readLong();
        this.message_id = in.readLong();
        this.notification_category = in.readString();
        this.conversation_id = in.readLong();
        this.isPrivate = in.readByte() != 0;
        this.participant_count = in.readInt();
        this.discussion_topic_id = in.readLong();
        this.announcement_id = in.readLong();
        this.total_root_discussion_entries = in.readInt();
        this.require_initial_post = in.readByte() != 0;
        this.user_has_posted = in.readByte() != 0;
        in.readList(this.root_discussion_entries, DiscussionEntry.class.getClassLoader());
        this.attempt = in.readInt();
        this.body = in.readString();
        this.grade = in.readString();
        this.grade_matches_current_submission = in.readByte() != 0;
        this.graded_at = in.readString();
        this.grader_id = in.readLong();
        this.score = in.readDouble();
        this.submission_type = in.readString();
        this.submitted_at = in.readString();
        this.workflow_state = in.readString();
        this.late = in.readByte() != 0;
        this.preview_url = in.readString();
        in.readList(this.submission_comments, SubmissionComment.class.getClassLoader());
        this.canvasContext = in.readParcelable(CanvasContext.class.getClassLoader());
        this.assignment = in.readParcelable(Assignment.class.getClassLoader());
        this.user_id = in.readLong();
        this.user = in.readParcelable(User.class.getClassLoader());
        int tmpEnumType = in.readInt();
        this.enumType = tmpEnumType == -1 ? null : Type.values()[tmpEnumType];
        int tmpCanvasContextType = in.readInt();
        this.canvasContextType = tmpCanvasContextType == -1 ? null : CanvasContext.Type.values()[tmpCanvasContextType];
        this.hasSetContextType = in.readByte() != 0;
        long tmpUpdatedAtDate = in.readLong();
        this.updatedAtDate = tmpUpdatedAtDate == -1 ? null : new Date(tmpUpdatedAtDate);
        long tmpGradedAtDate = in.readLong();
        this.gradedAtDate = tmpGradedAtDate == -1 ? null : new Date(tmpGradedAtDate);
        long tmpSubmittedAtDate = in.readLong();
        this.submittedAtDate = tmpSubmittedAtDate == -1 ? null : new Date(tmpSubmittedAtDate);
        this.conversation = in.readParcelable(Conversation.class.getClassLoader());
        this.isChecked = in.readByte() != 0;
    }

    public static Creator<StreamItem> CREATOR = new Creator<StreamItem>() {
        public StreamItem createFromParcel(Parcel source) {
            return new StreamItem(source);
        }

        public StreamItem[] newArray(int size) {
            return new StreamItem[size];
        }
    };
}
