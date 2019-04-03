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

public class Assignment extends CanvasModel<Assignment>{

	private long id;
	private String name;
	private String description;
    private List<String> submission_types = new ArrayList<String>();
	private String due_at;
	private double points_possible;
	private long course_id;

    @SerializedName("grade_group_students_individually")
    private boolean isGradeGroupsIndividually;

    private String grading_type;
    private long needs_grading_count;

	private String html_url;
    private String url;
    private long quiz_id; // (Optional) id of the associated quiz (applies only when submission_types is ["online_quiz"])
    private List<RubricCriterion> rubric = new ArrayList<RubricCriterion>();
    private boolean use_rubric_for_grading;
    private List<String> allowed_extensions = new ArrayList<String>();
    private Submission submission;
    private long assignment_group_id;
    private int position;
    private boolean peer_reviews;

    //Module lock info
    private LockInfo lock_info;
    private boolean locked_for_user;
    private String lock_at; //Date the teacher no longer accepts submissions.
    private String unlock_at;
    private String lock_explanation;

    private DiscussionTopicHeader discussion_topic;

    private List<NeedsGradingCount> needs_grading_count_by_section = new ArrayList<NeedsGradingCount>();

    private boolean free_form_criterion_comments;
    private boolean published;
    private boolean muted;
    private long group_category_id;

    private List<AssignmentDueDate> all_dates = new ArrayList<AssignmentDueDate>();

    @SerializedName("user_submitted")
    private boolean userSubmitted;

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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Date getDueDate() {
        if(due_at == null) {
            return null;
        }
		return APIHelpers.stringToDate(due_at);
	}
    public Date getlockAtDate(){
        if(lock_at == null){
            return null;
        }
        return APIHelpers.stringToDate(lock_at);
    }
	public void setDueDate(String dueDate) {
		this.due_at = dueDate;
	}
    public void setDueDate(Date dueDate){
        setDueDate(APIHelpers.dateToString(dueDate));
    }
    public void setLockAtDate(String lockAtDate){
        this.lock_at = lockAtDate;
    }
	public List<SUBMISSION_TYPE> getSubmissionTypes() {
        if(submission_types == null) {
            return new ArrayList<SUBMISSION_TYPE>();
        }

        List<SUBMISSION_TYPE>   submissionTypeList = new ArrayList<SUBMISSION_TYPE>();

        for(String submissionType : submission_types){
            submissionTypeList.add(getSubmissionTypeFromAPIString(submissionType));
        }

		return submissionTypeList;
	}
	public void setSubmissionTypes(ArrayList<String> submissionTypes) {
        if(submissionTypes == null){
            return;
        }

		this.submission_types = submissionTypes;
	}

    public void setSubmissionTypes(SUBMISSION_TYPE[] submissionTypes){
        if(submissionTypes == null){
            return;
        }

        ArrayList<String> listSubmissionTypes = new ArrayList<String>();

        for(SUBMISSION_TYPE submissionType: submissionTypes){
            listSubmissionTypes.add(submissionTypeToAPIString(submissionType));
        }

        setSubmissionTypes(listSubmissionTypes);
    }

    public long getNeedsGradingCount() {return needs_grading_count;}
    public void setNeedsGradingCount(long needs_grading_count) { this.needs_grading_count = needs_grading_count; }

    public double getPointsPossible() {
		return points_possible;
	}
	public void setPointsPossible(double pointsPossible) {
		this.points_possible = pointsPossible;
	}
	public long getCourseId() {
		return course_id;
	}
	public void setCourseId(long courseId) {
		this.course_id = courseId;
	}
    public void setDiscussionTopicHeader(DiscussionTopicHeader header) {
        discussion_topic = header;
    }
    public DiscussionTopicHeader getDiscussionTopicHeader() {
        return discussion_topic;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
	public String getHtmlUrl() {
		return html_url;
	}
	public void setHtmlUrl(String htmlUrl) {
		this.html_url = htmlUrl;
	}
    public long getQuizId() {
        return quiz_id;
    }
    public void setQuizId(long id) {
        quiz_id = id;
    }
    public List<RubricCriterion> getRubric() {
        return rubric;
    }
    public void setRubric(List<RubricCriterion> rubric) {
        this.rubric = rubric;
    }
    public boolean isUseRubricForGrading() {
        return use_rubric_for_grading;
    }
    public void setUseRubricForGrading(boolean useRubricForGrading) {
        this.use_rubric_for_grading = useRubricForGrading;
    }
    public List<String> getAllowedExtensions() {
        return allowed_extensions;
    }
    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowed_extensions = allowedExtensions;
    }
    public Submission getLastSubmission() {
        return submission;
    }
    public void setLastSubmission(Submission lastSubmission) {
        this.submission = lastSubmission;
    }
    public long getAssignmentGroupId() {
        return assignment_group_id;
    }
    public void setAssignmentGroupId(Long assignmentGroupId) {
        this.assignment_group_id = assignmentGroupId == null ?0:assignmentGroupId;
    }
    public LockInfo getLockInfo() {
        return lock_info;
    }
    public void setLockInfo(LockInfo lockInfo) {
        this.lock_info = lockInfo;
    }
    public boolean isLockedForUser() {
        return locked_for_user;
    }
    public void setLockedForUser(boolean locked) {
        this.locked_for_user = locked;
    }
    public GRADING_TYPE getGradingType(){return getGradingTypeFromAPIString(grading_type);}
    @Deprecated
    public GRADING_TYPE getGradingType(Context context){
      return  getGradingTypeFromString(grading_type, context);
    }

    public void setGradingType(GRADING_TYPE grading_type) {
        this.grading_type = gradingTypeToAPIString(grading_type);
    }

    public TURN_IN_TYPE getTurnInType(){return turnInTypeFromSubmissionType(getSubmissionTypes());}

    public Submission getLastActualSubmission() {
        if(submission == null) {
            return null;
        }
        if(submission.getWorkflowState() != null && submission.getWorkflowState().equals("submitted")) {
            return submission;
        }
        else {
            return null;
        }
    }

    public Date getUnlockAt() {
        if(unlock_at == null){
            return null;
        }
        return APIHelpers.stringToDate(unlock_at);
    }

    public void setUnlockAt(Date unlockAt){
        unlock_at = APIHelpers.dateToString(unlockAt);
    }

    public boolean hasPeerReviews() {
        return peer_reviews;
    }

    public void setPeerReviews(boolean peerReviews) {
        this.peer_reviews = peer_reviews;
    }

    public List<NeedsGradingCount> getNeedsGradingCountBySection(){
        return needs_grading_count_by_section;
    }

    public void setNeedsGradingCountBySection(List<NeedsGradingCount> needs_grading_count_by_section){
        this.needs_grading_count_by_section = needs_grading_count_by_section;
    }

    public boolean isFreeFormCriterionComments() {
        return free_form_criterion_comments;
    }

    public void setFreeFormCriterionComments(boolean free_form_criterion_comments) {
        this.free_form_criterion_comments = free_form_criterion_comments;
    }
    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public long getGroupCategoryId(){
        return this.group_category_id;
    }

    public void setGroupCategoryId(long groupId){
        this.group_category_id = groupId;
    }

    public List<AssignmentDueDate> getDueDates(){
        return this.all_dates;
    }

    public void setAllDates(List<AssignmentDueDate> all_dates){
        this.all_dates = all_dates;
    }

    public void setMuted(boolean isMuted){
        this.muted = isMuted;
    }

    public boolean isMuted(){
        return this.muted;
    }

    public String getLock_explanation() {
        return lock_explanation;
    }

    public boolean isGradeGroupsIndividually() {
        return isGradeGroupsIndividually;
    }

    public void setGradeGroupsIndividually(boolean isGradeGroupsIndividually) {
        this.isGradeGroupsIndividually = isGradeGroupsIndividually;
    }

    public void setUserSubmitted(boolean userSubmitted) {
        this.userSubmitted = userSubmitted;
    }

    public boolean hasUserSubmitted() {
        return userSubmitted;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Date getComparisonDate() {
        return getDueDate();
    }

    @Override
    public String getComparisonString() {
        return getName();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public Assignment() {}
	

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////
    public static final SUBMISSION_TYPE[] ONLINE_SUBMISSIONS = {SUBMISSION_TYPE.ONLINE_UPLOAD, SUBMISSION_TYPE.ONLINE_URL, SUBMISSION_TYPE.ONLINE_TEXT_ENTRY, SUBMISSION_TYPE.MEDIA_RECORDING};



    public enum TURN_IN_TYPE {ONLINE, ON_PAPER, NONE, DISCUSSION, QUIZ, EXTERNAL_TOOL}

    private boolean expectsSubmissions() {
        List<SUBMISSION_TYPE> submissionTypes = getSubmissionTypes();
        return submissionTypes.size() > 0 && !submissionTypes.contains(SUBMISSION_TYPE.NONE) && !submissionTypes.contains(SUBMISSION_TYPE.NOT_GRADED) && !submissionTypes.contains(SUBMISSION_TYPE.ON_PAPER) && !submissionTypes.contains(SUBMISSION_TYPE.EXTERNAL_TOOL);
    }

    public boolean isAllowedToSubmit() {
        List<SUBMISSION_TYPE> submissionTypes = getSubmissionTypes();
        return expectsSubmissions() && !isLockedForUser() && !submissionTypes.contains(SUBMISSION_TYPE.ONLINE_QUIZ) && !submissionTypes.contains(SUBMISSION_TYPE.ATTENDANCE);
    }

    public boolean isWithoutGradedSubmission() {
        Submission submission = getLastActualSubmission();
        return submission == null || submission.isWithoutGradedSubmission();
    }

    public static TURN_IN_TYPE stringToTurnInType(String turnInType, Context context){
        if(turnInType == null){
            return null;
        }

        if(turnInType.equals(context.getString(R.string.canvasAPI_online))){
            return TURN_IN_TYPE.ONLINE;
        } else if(turnInType.equals(context.getString(R.string.canvasAPI_onPaper))){
            return TURN_IN_TYPE.ON_PAPER;
        } else if(turnInType.equals(context.getString(R.string.canvasAPI_discussion))){
            return TURN_IN_TYPE.DISCUSSION;
        } else if(turnInType.equals(context.getString(R.string.canvasAPI_quiz))){
            return TURN_IN_TYPE.QUIZ;
        } else if(turnInType.equals(context.getString(R.string.canvasAPI_externalTool))){
            return TURN_IN_TYPE.EXTERNAL_TOOL;
        } else{
            return TURN_IN_TYPE.NONE;
        }
    }

    public static String turnInTypeToPrettyPrintString(TURN_IN_TYPE turnInType, Context context){
        if(turnInType == null){
            return null;
        }

        switch (turnInType){
            case ONLINE:
                return context.getString(R.string.canvasAPI_online);
            case ON_PAPER:
                return context.getString(R.string.canvasAPI_onPaper);
            case NONE:
                return context.getString(R.string.canvasAPI_none);
            case DISCUSSION:
                return context.getString(R.string.canvasAPI_discussion);
            case QUIZ:
                return context.getString(R.string.canvasAPI_quiz);
            case EXTERNAL_TOOL:
                return context.getString(R.string.canvasAPI_externalTool);
            default:
                return null;
        }
    }

    private TURN_IN_TYPE turnInTypeFromSubmissionType(List<SUBMISSION_TYPE> submissionTypes){

        if(submissionTypes == null || submissionTypes.size() == 0){
            return TURN_IN_TYPE.NONE;
        }

        SUBMISSION_TYPE submissionType = submissionTypes.get(0);

        if(submissionType == SUBMISSION_TYPE.MEDIA_RECORDING || submissionType == SUBMISSION_TYPE.ONLINE_TEXT_ENTRY ||
                submissionType == SUBMISSION_TYPE.ONLINE_URL || submissionType == SUBMISSION_TYPE.ONLINE_UPLOAD ){
            return TURN_IN_TYPE.ONLINE;
        }else if(submissionType == SUBMISSION_TYPE.ONLINE_QUIZ){
            return TURN_IN_TYPE.QUIZ;
        }else if(submissionType == SUBMISSION_TYPE.DISCUSSION_TOPIC){
            return TURN_IN_TYPE.DISCUSSION;
        }else if(submissionType == SUBMISSION_TYPE.ON_PAPER){
            return TURN_IN_TYPE.ON_PAPER;
        }else if(submissionType == SUBMISSION_TYPE.EXTERNAL_TOOL){
            return TURN_IN_TYPE.EXTERNAL_TOOL;
        }

        return TURN_IN_TYPE.NONE;
    }

    public boolean isLocked() {
        Date currentDate = new Date();
        if(getLockInfo() == null || getLockInfo().isEmpty()) {
            return false;
        } else if(getLockInfo().getLockedModuleName() != null && getLockInfo().getLockedModuleName().length() > 0 && !getLockInfo().getLockedModuleName().equals("null")) {
            return true;
        } else if(getLockInfo().getUnlockedAt().after(currentDate)){
            return true;
        }
        return false;

    }

	public void populateScheduleItem(ScheduleItem scheduleItem) {
        scheduleItem.setId(this.getId());
        scheduleItem.setTitle(this.getName());
        scheduleItem.setStartDate(this.getDueDate());
        scheduleItem.setType(ScheduleItem.Type.TYPE_ASSIGNMENT);
        scheduleItem.setDescription(this.getDescription());
        scheduleItem.setSubmissionTypes(getSubmissionTypes());
        scheduleItem.setPointsPossible(this.getPointsPossible());
        scheduleItem.setHtmlUrl(this.getHtmlUrl());
        scheduleItem.setQuizId(this.getQuizId());
        scheduleItem.setDiscussionTopicHeader(this.getDiscussionTopicHeader());
        scheduleItem.setAssignment(this);
        if(getLockInfo() != null && getLockInfo().getLockedModuleName() != null) {
            scheduleItem.setLockedModuleName(this.getLockInfo().getLockedModuleName());
        }
    }

    public ScheduleItem toScheduleItem() {
		ScheduleItem scheduleItem = new ScheduleItem();

		populateScheduleItem(scheduleItem);

		return scheduleItem;
	}

    public boolean hasRubric() {
        if (rubric == null) {
            return false;
        }
        return rubric.size() > 0;
    }

    public enum SUBMISSION_TYPE {ONLINE_QUIZ, NONE, ON_PAPER, DISCUSSION_TOPIC, EXTERNAL_TOOL, ONLINE_UPLOAD, ONLINE_TEXT_ENTRY, ONLINE_URL, MEDIA_RECORDING, ATTENDANCE, NOT_GRADED}

    private SUBMISSION_TYPE getSubmissionTypeFromAPIString(String submissionType){
        if(submissionType.equals("online_quiz")){
            return SUBMISSION_TYPE.ONLINE_QUIZ;
        } else if(submissionType.equals("none")){
            return SUBMISSION_TYPE.NONE;
        } else if(submissionType.equals("on_paper")){
            return SUBMISSION_TYPE.ON_PAPER;
        } else if(submissionType.equals("discussion_topic")){
            return SUBMISSION_TYPE.DISCUSSION_TOPIC;
        } else if(submissionType.equals("external_tool")){
            return SUBMISSION_TYPE.EXTERNAL_TOOL;
        } else if(submissionType.equals("online_upload")){
            return SUBMISSION_TYPE.ONLINE_UPLOAD;
        } else if(submissionType.equals("online_text_entry")){
            return SUBMISSION_TYPE.ONLINE_TEXT_ENTRY;
        } else if(submissionType.equals("online_url")){
            return SUBMISSION_TYPE.ONLINE_URL;
        } else if(submissionType.equals("media_recording")){
            return SUBMISSION_TYPE.MEDIA_RECORDING;
        } else if(submissionType.equals("attendance")) {
            return SUBMISSION_TYPE.ATTENDANCE;
        } else if(submissionType.equals("not_graded")) {
            return SUBMISSION_TYPE.NOT_GRADED;
        } else {
            return null;
        }
    }
    public static String submissionTypeToAPIString(SUBMISSION_TYPE submissionType){

        if(submissionType == null){
            return null;
        }

        switch (submissionType){
            case  ONLINE_QUIZ:
                return "online_quiz";
            case NONE:
                return "none";
            case ON_PAPER:
                return "on_paper";
            case DISCUSSION_TOPIC:
                return "discussion_topic";
            case EXTERNAL_TOOL:
                return "external_tool";
            case ONLINE_UPLOAD:
                return "online_upload";
            case ONLINE_TEXT_ENTRY:
                return "online_text_entry";
            case ONLINE_URL:
                return "online_url";
            case MEDIA_RECORDING:
                return "media_recording";
            case ATTENDANCE:
                return "attendance";
            case NOT_GRADED:
                return "not_graded";
            default:
                return "";
        }
    }
    public static String submissionTypeToPrettyPrintString(SUBMISSION_TYPE submissionType, Context context){

        if(submissionType == null){
            return null;
        }

        switch (submissionType){
            case  ONLINE_QUIZ:
                return context.getString(R.string.canvasAPI_onlineQuiz);
            case NONE:
                return context.getString(R.string.canvasAPI_none);
            case ON_PAPER:
                return context.getString(R.string.canvasAPI_onPaper);
            case DISCUSSION_TOPIC:
                return context.getString(R.string.canvasAPI_discussionTopic);
            case EXTERNAL_TOOL:
                return context.getString(R.string.canvasAPI_externalTool);
            case ONLINE_UPLOAD:
                return context.getString(R.string.canvasAPI_onlineUpload);
            case ONLINE_TEXT_ENTRY:
                return context.getString(R.string.canvasAPI_onlineTextEntry);
            case ONLINE_URL:
                return context.getString(R.string.canvasAPI_onlineURL);
            case MEDIA_RECORDING:
                return context.getString(R.string.canvasAPI_mediaRecording);
            case ATTENDANCE:
                return context.getString(R.string.canvasAPI_attendance);
            case NOT_GRADED:
                return context.getString(R.string.canvasAPI_notGraded);
            default:
                return "";
        }
    }

    public enum GRADING_TYPE {PASS_FAIL, PERCENT, LETTER_GRADE, POINTS, GPA_SCALE, NOT_GRADED}

    public static GRADING_TYPE getGradingTypeFromString(String gradingType, Context context){
        if(gradingType.equals("pass_fail") || gradingType.equals(context.getString(R.string.canvasAPI_passFail))){
            return GRADING_TYPE.PASS_FAIL;
        } else if(gradingType.equals("percent") || gradingType.equals(context.getString(R.string.canvasAPI_percent))){
            return GRADING_TYPE.PERCENT;
        } else if(gradingType.equals("letter_grade") || gradingType.equals(context.getString(R.string.canvasAPI_letterGrade))){
            return GRADING_TYPE.LETTER_GRADE;
        } else if (gradingType.equals("points") || gradingType.equals(context.getString(R.string.canvasAPI_points))){
            return GRADING_TYPE.POINTS;
        } else if (gradingType.equals("gpa_scale") || gradingType.equals(context.getString(R.string.canvasAPI_gpaScale))){
            return GRADING_TYPE.GPA_SCALE;
        } else if(gradingType.equals("not_graded") || gradingType.equals(context.getString(R.string.canvasAPI_notGraded))){
            return GRADING_TYPE.NOT_GRADED;
        }else {
            return null;
        }
    }
    public static GRADING_TYPE getGradingTypeFromAPIString(String gradingType){
        if(gradingType.equals("pass_fail")){
            return GRADING_TYPE.PASS_FAIL;
        } else if(gradingType.equals("percent")){
            return GRADING_TYPE.PERCENT;
        } else if(gradingType.equals("letter_grade")){
            return GRADING_TYPE.LETTER_GRADE;
        } else if (gradingType.equals("points")){
            return GRADING_TYPE.POINTS;
        } else if (gradingType.equals("gpa_scale")){
            return GRADING_TYPE.GPA_SCALE;
        } else if(gradingType.equals("not_graded")){
            return GRADING_TYPE.NOT_GRADED;
        }else{
            return null;
        }
    }

    public  static String gradingTypeToAPIString(GRADING_TYPE gradingType){
        if(gradingType == null){ return null;}

        switch (gradingType){
            case PASS_FAIL:
                return "pass_fail";
            case PERCENT:
                return "percent";
            case LETTER_GRADE:
                return "letter_grade";
            case POINTS:
                return "points";
            case GPA_SCALE:
                return "gpa_scale";
            case NOT_GRADED:
                return "not_graded";
            default:
                return "";
        }
    }

    public  static String gradingTypeToPrettyPrintString(GRADING_TYPE gradingType, Context context){
        if(gradingType == null){ return null;}

        switch (gradingType){
            case PASS_FAIL:
                return context.getString(R.string.canvasAPI_passFail);
            case PERCENT:
                return context.getString(R.string.canvasAPI_percent);
            case LETTER_GRADE:
                return context.getString(R.string.canvasAPI_letterGrade);
            case POINTS:
                return context.getString(R.string.canvasAPI_points);
            case GPA_SCALE:
                return context.getString(R.string.canvasAPI_gpaScale);
            case NOT_GRADED:
                return context.getString(R.string.canvasAPI_notGraded);
            default:
                return "";
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeList(this.submission_types);
        dest.writeString(this.due_at);
        dest.writeDouble(this.points_possible);
        dest.writeLong(this.course_id);
        dest.writeString(this.grading_type);
        dest.writeString(this.html_url);
        dest.writeString(this.url);
        dest.writeLong(this.quiz_id);
        dest.writeList(this.rubric);
        dest.writeByte(use_rubric_for_grading ? (byte) 1 : (byte) 0);
        dest.writeList(this.allowed_extensions);
        dest.writeParcelable(this.submission, flags);
        dest.writeLong(this.assignment_group_id);
        dest.writeByte(peer_reviews ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.lock_info, flags);
        dest.writeString(this.lock_at);
        dest.writeString(this.unlock_at);
        dest.writeParcelable(this.discussion_topic, flags);
        dest.writeLong(this.needs_grading_count);
        dest.writeList(this.needs_grading_count_by_section);
        dest.writeByte(free_form_criterion_comments ? (byte) 1 : (byte) 0);
        dest.writeByte(published ? (byte) 1 : (byte) 0);
        dest.writeLong(this.group_category_id);
        dest.writeList(this.all_dates);
        dest.writeByte(this.muted ? (byte)1 : (byte) 0);
        dest.writeByte(this.locked_for_user ? (byte)1 : (byte) 0);
        dest.writeByte(this.isGradeGroupsIndividually ? (byte)1 : (byte) 0);
        dest.writeByte(this.userSubmitted ? (byte) 1 : (byte) 0);
    }

    public Assignment createDeepCopy(Assignment in) {
        Assignment copy = new Assignment();
        copy.id = in.id;
        copy.name = in.name;
        copy.description = in.description;
        copy.submission_types = in.submission_types;
        copy.due_at = in.due_at;
        copy.points_possible = in.points_possible;
        copy.course_id = in.course_id;
        copy.grading_type = in.grading_type;
        copy.html_url = in.html_url;
        copy.url = in.url;
        copy.quiz_id = in.quiz_id;
        copy.use_rubric_for_grading = in.use_rubric_for_grading;
        copy.submission = in.submission;
        copy.assignment_group_id = in.assignment_group_id;
        copy.peer_reviews = in.peer_reviews;
        copy.lock_info =  in.lock_info;
        copy.lock_at = in.lock_at;
        copy.unlock_at = in.unlock_at;
        copy.discussion_topic = in.discussion_topic;
        copy.needs_grading_count = in.needs_grading_count;
        copy.needs_grading_count_by_section = in.needs_grading_count_by_section;
        copy.free_form_criterion_comments = in.free_form_criterion_comments;
        copy.published = in.published;
        copy.group_category_id = in.group_category_id;
        copy.all_dates = in.all_dates;
        copy.muted = in.muted;
        copy.locked_for_user = in.locked_for_user;
        copy.isGradeGroupsIndividually = in.isGradeGroupsIndividually;
        return copy;
    }

    private Assignment(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.description = in.readString();

        in.readList(this.submission_types, String.class.getClassLoader());

        this.due_at = in.readString();
        this.points_possible = in.readDouble();
        this.course_id = in.readLong();
        this.grading_type = in.readString();
        this.html_url = in.readString();
        this.url = in.readString();
        this.quiz_id = in.readLong();

        in.readList(this.rubric, RubricCriterion.class.getClassLoader());

        this.use_rubric_for_grading = in.readByte() != 0;

        in.readList(this.allowed_extensions, String.class.getClassLoader());

        this.submission = in.readParcelable(Submission.class.getClassLoader());
        this.assignment_group_id = in.readLong();
        this.peer_reviews = in.readByte() != 0;
        this.lock_info =  in.readParcelable(LockInfo.class.getClassLoader());
        this.lock_at = in.readString();
        this.unlock_at = in.readString();
        this.discussion_topic = in.readParcelable(DiscussionTopicHeader.class.getClassLoader());
        this.needs_grading_count = in.readLong();
        in.readList(this.needs_grading_count_by_section, NeedsGradingCount.class.getClassLoader());
        this.free_form_criterion_comments = in.readByte() != 0;
        this.published = in.readByte() != 0;
        this.group_category_id = in.readLong();
        in.readList(this.all_dates, AssignmentDueDate.class.getClassLoader());
        this.muted = in.readByte() != 0;
        this.locked_for_user = in.readByte() != 0;
        this.isGradeGroupsIndividually = in.readByte() != 0;
        this.userSubmitted = in.readByte() != 0;
    }

    public static Creator<Assignment> CREATOR = new Creator<Assignment>() {
        public Assignment createFromParcel(Parcel source) {
            return new Assignment(source);
        }

        public Assignment[] newArray(int size) {
            return new Assignment[size];
        }
    };
}
