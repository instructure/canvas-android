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

import android.os.Parcel;

import com.instructure.canvasapi.utilities.APIHelpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Submission extends CanvasModel<Submission>{

    private long id;
	private String grade;
	private double score;
    private long attempt;

	private String submitted_at;

	private ArrayList<SubmissionComment> submission_comments = new ArrayList<SubmissionComment>();
	private Date commentCreated;
	private String mediaContentType;
	private String mediaCommentUrl;
	private String mediaCommentDisplay;
	private ArrayList<Submission> submission_history = new ArrayList<Submission>();
	private ArrayList<Attachment> attachments = new ArrayList<Attachment>();
	private String body;
    private HashMap<String,RubricCriterionRating> rubric_assessment = new HashMap<>();
	private boolean grade_matches_current_submission;
	private String workflow_state;
	private String submission_type;
	private String preview_url;
	private String url;
    private boolean late;
    private boolean excused;

    private MediaComment media_comment;

    //Conversation Stuff
    private long assignment_id;
    private Assignment assignment;
    private long user_id;
    private long grader_id;
    private User user;

    //this value could be null. Currently will only be returned when getting the submission for
    //a user when the submission_type is discussion_topic
    private ArrayList<DiscussionEntry> discussion_entries = new ArrayList<DiscussionEntry>();

    // Group Info only available when including groups in the Submissions#index endpoint
    private Group group;

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////
    public boolean isWithoutGradedSubmission() {
        return !isGraded() && getSubmissionType() == null;
    }

    public boolean isGraded() {
        return getGrade() != null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public long getUser_id(){return user_id;}
    public void setUser_id(long user_id){this.user_id = user_id;}

	public ArrayList<SubmissionComment> getComments() {
		return submission_comments;
	}
	public void setComments(ArrayList<SubmissionComment> comments) {
		this.submission_comments = comments;
	}
	public Date getCommentCreated() {
		return commentCreated;
	}
	public void setCommentCreated(Date commentCreated) {
		this.commentCreated = commentCreated;
	}
	public String getMediaContentType() {
		return mediaContentType;
	}
	public void setMediaContentType(String mediaContentType) {
		this.mediaContentType = mediaContentType;
	}
	public String getMediaCommentUrl() {
		return mediaCommentUrl;
	}
	public void setMediaCommentUrl(String mediaCommentUrl) {
		this.mediaCommentUrl = mediaCommentUrl;
	}
	public String getMediaCommentDisplay() {
		return mediaCommentDisplay;
	}
	public void setMediaCommentDisplay(String mediaCommentDisplay) {
		this.mediaCommentDisplay = mediaCommentDisplay;
	}
	public String getGrade() {
		return grade;
	}
	public void setGrade(String grade) {
		this.grade = grade;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}

    public long getAssignment_id() {return assignment_id;}
    public void setAssignment_id(long assignment_id) {this.assignment_id = assignment_id;}
    public Assignment getAssignment(){
        return assignment;
    }
    public void setAssignment(Assignment assignment){this.assignment = assignment;}

    public long getGraderID(){
        return grader_id;
    }
    public boolean isExcused(){ return excused; }
    public void setExcused(boolean excused){
        this.excused = excused;
    }

	public Date getSubmitDate() {
        if(submitted_at == null) {
            return null;
        }
		return APIHelpers.stringToDate(submitted_at);
	}
	public void setSubmitDate(String submitDate) {
        if(submitDate == null) {
            this.submitted_at = null;
        }
        else {
		    this.submitted_at = submitDate;
        }
	}
	public void setSubmissionHistory(ArrayList<Submission> history) {
	    this.submission_history = history;
	}
	public ArrayList<Submission> getSubmissionHistory() {
	    return submission_history;
	}
    public ArrayList<Attachment> getAttachments() {
        return attachments;
    } 
    public void setAttachments(ArrayList<Attachment> attachments) {
        this.attachments = attachments;
    }
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public boolean isGradeMatchesCurrentSubmission() {
        return grade_matches_current_submission;
    }
    public void setGradeMatchesCurrentSubmission(
            boolean gradeMatchesCurrentSubmission) {
        this.grade_matches_current_submission = gradeMatchesCurrentSubmission;
    }
    public String getWorkflowState() {
        return workflow_state;
    }
    public void setWorkflowState(String workflowState) {
        this.workflow_state = workflowState;
    }
    public String getSubmissionType() {
        return submission_type;
    }
    public void setSubmissionType(String submissionType) {
        this.submission_type = submissionType;
    }
    public String getPreviewUrl() {
        return preview_url;
    }
    public void setPreviewUrl(String previewUrl) {
        this.preview_url = previewUrl;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public RubricAssessment getRubricAssessment() {
        RubricAssessment assessment = new RubricAssessment();
        ArrayList<RubricCriterionRating> ratings = new ArrayList<RubricCriterionRating>();
        if (rubric_assessment != null) {
            for (Map.Entry<String, RubricCriterionRating> entry : rubric_assessment.entrySet()) {
                RubricCriterionRating rating = entry.getValue();
                rating.setCriterionId(entry.getKey());
                ratings.add(rating);
            }
        }
        assessment.setRatings(ratings);
        return assessment;
    }

    public HashMap<String,RubricCriterionRating> getRubricAssessmentHash(){
        return this.rubric_assessment;
    }

    public void setRubricAssessment(HashMap<String,RubricCriterionRating> ratings){
        this.rubric_assessment = ratings;
    }

    public ArrayList<DiscussionEntry> getDiscussion_entries() {
        return discussion_entries;
    }

    public void setDiscussion_entries(ArrayList<DiscussionEntry> discussion_entries) {
        this.discussion_entries = discussion_entries;
    }

    public MediaComment getMediaComment() { return media_comment; }
    public void setMediaComment(MediaComment media_comment) {
        this.media_comment = media_comment;
    }

    public long getAttempt() { return attempt; }
    public void setAttempt(long attempt) {
        this.attempt = attempt;
    }
    public boolean isLate() { return late; }
    public void setIslate(boolean late){
        this.late = late;
    }
    public Group getGroup() {
        return group;
    }
    public void setGroup(Group group) {
        this.group = group;
    }
    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Date getComparisonDate() {
        return getSubmitDate();
    }

    @Override
    public String getComparisonString() {
        return getSubmissionType();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public Submission() {}

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    public ArrayList<Long> getUserIds() {
        ArrayList<Long> ids = new ArrayList<Long>();

        for(int i = 0; i < submission_comments.size(); i++)
        {
            ids.add(submission_comments.get(i).getAuthorID());
        }

        return ids;
    }
    /*
     * Submissions will have dummy submissions if they grade an assignment with no actual submissions.
     * We want to see if any are not dummy submissions
     */
    public boolean hasRealSubmission(){
        if(submission_history != null) {
            for (Submission submission : submission_history) {
                if (submission != null && submission.getSubmissionType() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.grade);
        dest.writeDouble(this.score);
        dest.writeString(this.submitted_at);
        dest.writeList(this.submission_comments);
        dest.writeLong(commentCreated != null ? commentCreated.getTime() : -1);
        dest.writeString(this.mediaContentType);
        dest.writeString(this.mediaCommentUrl);
        dest.writeString(this.mediaCommentDisplay);
        dest.writeList(this.submission_history);
        dest.writeList(this.attachments);
        dest.writeString(this.body);
        dest.writeSerializable(this.rubric_assessment);
        dest.writeByte(grade_matches_current_submission ? (byte) 1 : (byte) 0);
        dest.writeString(this.workflow_state);
        dest.writeString(this.submission_type);
        dest.writeString(this.preview_url);
        dest.writeString(this.url);
        dest.writeParcelable(this.assignment, flags);
        dest.writeLong(this.user_id);
        dest.writeLong(this.grader_id);
        dest.writeLong(this.assignment_id);
        dest.writeParcelable(this.user, flags);
        dest.writeParcelable(this.media_comment, flags);
        dest.writeList(this.discussion_entries);
        dest.writeLong(this.attempt);
        dest.writeByte(this.excused ? (byte) 1 : (byte) 0);
        dest.writeByte(this.late ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.group, flags);
    }

    private Submission(Parcel in) {
        this.assignment = new Assignment();
        this.id = in.readLong();
        this.grade = in.readString();
        this.score = in.readDouble();
        this.submitted_at = in.readString();
        in.readList(this.submission_comments, SubmissionComment.class.getClassLoader());
        long tmpCommentCreated = in.readLong();
        this.commentCreated = tmpCommentCreated == -1 ? null : new Date(tmpCommentCreated);
        this.mediaContentType = in.readString();
        this.mediaCommentUrl = in.readString();
        this.mediaCommentDisplay = in.readString();
        in.readList(this.submission_history, Submission.class.getClassLoader());
        in.readList(this.attachments, Attachment.class.getClassLoader());
        this.body = in.readString();
        this.rubric_assessment =(HashMap<String,RubricCriterionRating>) in.readSerializable();
        this.grade_matches_current_submission = in.readByte() != 0;
        this.workflow_state = in.readString();
        this.submission_type = in.readString();
        this.preview_url = in.readString();
        this.url = in.readString();
        this.assignment = in.readParcelable(Assignment.class.getClassLoader());
        this.user_id = in.readLong();
        this.grader_id = in.readLong();
        this.assignment_id = in.readLong();
        this.user = in.readParcelable(User.class.getClassLoader());
        this.media_comment = in.readParcelable(MediaComment.class.getClassLoader());
        in.readList(this.discussion_entries, DiscussionEntry.class.getClassLoader());
        this.attempt = in.readLong();
        this.excused = in.readByte() != 0;
        this.late = in.readByte() != 0;
        this.group = in.readParcelable(Group.class.getClassLoader());
    }

    public static Creator<Submission> CREATOR = new Creator<Submission>() {
        public Submission createFromParcel(Parcel source) {
            return new Submission(source);
        }

        public Submission[] newArray(int size) {
            return new Submission[size];
        }
    };

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
