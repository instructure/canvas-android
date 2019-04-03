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

import com.google.gson.annotations.SerializedName;
import com.instructure.canvasapi.utilities.APIHelpers;

import java.util.Date;


public class QuizSubmission extends CanvasModel<QuizSubmission> {
    public enum WORKFLOW_STATE { UNTAKEN, COMPLETE, PENDING_REVIEW, PREVIEW, SETTINGS_ONLY, UNKNOWN }
    //The ID of the quiz submission.
    private long id;

    //The ID of the Quiz the quiz submission belongs to.
    @SerializedName("quiz_id")
    private long quizId;

    //The ID of the Student that made the quiz submission.
    @SerializedName("user_id")
    private long userId;

    //The ID of the Submission the quiz submission represents.
    @SerializedName("submission_id")
    private long submissionId;

    //The time at which the student started the quiz submission.
    @SerializedName("started_at")
    private String startedAt;

    //The time at which the student submitted the quiz submission.
    @SerializedName("finished_at")
    private String finishedAt;

    //The time at which the quiz submission will be overdue, and be flagged as a late
    //submission.
    @SerializedName("end_at")
    private String endAt;

    //For quizzes that allow multiple attempts, this field specifies the quiz
    //submission attempt number.
    private int attempt;

    //Number of times the student was allowed to re-take the quiz over the
    //multiple-attempt limit.
    @SerializedName("extra_attempts")
    private int extraAttempts;

    //The number of attempts left. Note: the quiz object does not get updated with this information
    //in the allowed_attempts field.
    @SerializedName("attempts_left")
    private int attemptsLeft;

    //Amount of extra time allowed for the quiz submission, in minutes.
    @SerializedName("extra_time")
    private int extraTime;

    //The student can take the quiz even if it's locked for everyone else
    @SerializedName("manually_unlocked")
    private boolean manuallyUnlocked;

    //Amount of time spent, in seconds.
    @SerializedName("time_spent")
    private int timeSpent;

    //The score of the quiz submission, if graded.
    private double score;

    //The original score of the quiz submission prior to any re-grading.
    @SerializedName("score_before_regrade")
    private double scoreBeforeRegrade;

    //For quizzes that allow multiple attempts, this is the score that will be used,
    //which might be the score of the latest, or the highest, quiz submission.
    @SerializedName("kept_score")
    private double keptScore;

    //Number of points the quiz submission's score was fudged by.
    @SerializedName("fudge_points")
    private double fudgePoints;

    //Whether the student has viewed their results to the quiz.
    @SerializedName("has_seen_results")
    private boolean hasSeenResults;

    //The current state of the quiz submission. Possible values:
    //['untaken'|'pending_review'|'complete'|'settings_only'|'preview'].
    @SerializedName("workflow_state")
    private String workflowState;

    //Points possible for the quiz
    @SerializedName("quiz_points_possible")
    private double quizPointsPossible;

    //Token used to validate quiz answers when posting
    @SerializedName("validation_token")
    private String validationToken;

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getQuizId() {
        return quizId;
    }

    public void setQuizId(long quizId) {
        this.quizId = quizId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(long submissionId) {
        this.submissionId = submissionId;
    }

    public Date getStartedAt() {
        return APIHelpers.stringToDate(startedAt);
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    public Date getFinishedAt() {
        return APIHelpers.stringToDate(finishedAt);
    }

    public void setFinishedAt(String finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Date getEndAt() {
        return APIHelpers.stringToDate(endAt);
    }

    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public int getExtraAttempts() {
        return extraAttempts;
    }

    public void setExtraAttempts(int extraAttempts) {
        this.extraAttempts = extraAttempts;
    }

    public int getAttemptsLeft() {
        return attemptsLeft;
    }

    public void setAttemptsLeft(int attemptsLeft) {
        this.attemptsLeft = attemptsLeft;
    }

    public int getExtraTime() {
        return extraTime;
    }

    public void setExtraTime(int extraTime) {
        this.extraTime = extraTime;
    }

    public boolean isManuallyUnlocked() {
        return manuallyUnlocked;
    }

    public void setManuallyUnlocked(boolean manuallyUnlocked) {
        this.manuallyUnlocked = manuallyUnlocked;
    }

    public int getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(int timeSpent) {
        this.timeSpent = timeSpent;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getScoreBeforeRegrade() {
        return scoreBeforeRegrade;
    }

    public void setScoreBeforeRegrade(double scoreBeforeRegrade) {
        this.scoreBeforeRegrade = scoreBeforeRegrade;
    }

    public double getKeptScore() {
        return keptScore;
    }

    public void setKeptScore(double keptScore) {
        this.keptScore = keptScore;
    }

    public double getFudgePoints() {
        return fudgePoints;
    }

    public void setFudgePoints(int fudgePoints) {
        this.fudgePoints = fudgePoints;
    }

    public boolean hasSeenResults() {
        return hasSeenResults;
    }

    public void setHasSeenResults(boolean hasSeenResults) {
        this.hasSeenResults = hasSeenResults;
    }

    public WORKFLOW_STATE getWorkflowState() {
        return parseWorkflowState(workflowState);
    }

    public void setWorkflowState(String workflowState) {
        this.workflowState = workflowState;
    }

    public double getQuizPointsPossible() {
        return quizPointsPossible;
    }

    public void setQuizPointsPossible(double quizPointsPossible) {
        this.quizPointsPossible = quizPointsPossible;
    }

    public String getValidationToken() {
        return validationToken;
    }

    public void setValidationToken(String validationToken) {
        this.validationToken = validationToken;
    }

    @Override
    public Date getComparisonDate() {
        return getFinishedAt();
    }

    @Override
    public String getComparisonString() {
        return null;
    }

    @Override
    public int compareTo(QuizSubmission another) {
        return 0;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public static WORKFLOW_STATE parseWorkflowState(String workflowState) {
        switch(workflowState) {
            case "untaken":
                return WORKFLOW_STATE.UNTAKEN;
            case "complete":
                return WORKFLOW_STATE.COMPLETE;
            case "preview":
                return WORKFLOW_STATE.PREVIEW;
            case "settings_only":
                return WORKFLOW_STATE.SETTINGS_ONLY;
            case "pending_review":
                return WORKFLOW_STATE.PENDING_REVIEW;
        }

        return WORKFLOW_STATE.UNKNOWN;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.quizId);
        dest.writeLong(this.userId);
        dest.writeLong(this.submissionId);
        dest.writeString(this.startedAt);
        dest.writeString(this.finishedAt);
        dest.writeString(this.endAt);
        dest.writeInt(this.attempt);
        dest.writeInt(this.extraAttempts);
        dest.writeInt(this.extraTime);
        dest.writeByte(manuallyUnlocked ? (byte) 1 : (byte) 0);
        dest.writeInt(this.timeSpent);
        dest.writeDouble(this.score);
        dest.writeDouble(this.scoreBeforeRegrade);
        dest.writeDouble(this.keptScore);
        dest.writeDouble(this.fudgePoints);
        dest.writeByte(hasSeenResults ? (byte) 1 : (byte) 0);
        dest.writeString(this.workflowState);
        dest.writeDouble(this.quizPointsPossible);
        dest.writeString(this.validationToken);
    }

    public QuizSubmission() {
    }

    private QuizSubmission(Parcel in) {
        this.id = in.readLong();
        this.quizId = in.readLong();
        this.userId = in.readLong();
        this.submissionId = in.readLong();
        this.startedAt = in.readString();
        this.finishedAt = in.readString();
        this.endAt = in.readString();
        this.attempt = in.readInt();
        this.extraAttempts = in.readInt();
        this.extraTime = in.readInt();
        this.manuallyUnlocked = in.readByte() != 0;
        this.timeSpent = in.readInt();
        this.score = in.readDouble();
        this.scoreBeforeRegrade = in.readDouble();
        this.keptScore = in.readDouble();
        this.fudgePoints = in.readDouble();
        this.hasSeenResults = in.readByte() != 0;
        this.workflowState = in.readString();
        this.quizPointsPossible = in.readDouble();
        this.validationToken = in.readString();
    }

    public static final Creator<QuizSubmission> CREATOR = new Creator<QuizSubmission>() {
        public QuizSubmission createFromParcel(Parcel source) {
            return new QuizSubmission(source);
        }

        public QuizSubmission[] newArray(int size) {
            return new QuizSubmission[size];
        }
    };
}
