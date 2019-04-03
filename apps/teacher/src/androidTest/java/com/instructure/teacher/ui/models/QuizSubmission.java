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

public class QuizSubmission {
    public static final String UNTAKEN = "untaken";
    public static final String PENDING_REVIEW = "pending_review";
    public static final String COMPLETE = "complete";
    public static final String SETTINGS_ONLY = "settings_only";
    public static final String PREVIEW = "preview";

    public int id;
    public int quizId;
    public int quizVersion;
    public int userId;
    public int submissionId;
    public double score;
    public double keptScore;
    public Date startedAt;
    public Date endAt;
    public Date finishedAt;
    public int attempt;
    public String workflowState;
    public int fudgePoints;
    public double quizPointsPossible;
    public int extraAttempts;
    public boolean manuallyUnlocked;
    public String validationToken;
    public double scoreBeforeRegrade;
    public boolean hasSeenResults;
    public int timeSpent;
    public int attemptsLeft;
    public boolean overdueAndNeedsSubmission;
    public boolean excused;
    public String htmlUrl;

    public QuizSubmission(int id, int quizId, int quizVersion, int userId, int submissionId, double score,
                          double keptScore, Date startedAt, Date endAt, Date finishedAt, int attempt,
                          String workflowState, int fudgePoints, double quizPointsPossible, int extraAttempts,
                          boolean manuallyUnlocked, String validationToken, double scoreBeforeRegrade,
                          boolean hasSeenResults, int timeSpent, int attemptsLeft, boolean overdueAndNeedsSubmission,
                          boolean excused, String htmlUrl) {
        this.id = id;
        this.quizId = quizId;
        this.quizVersion = quizVersion;
        this.userId = userId;
        this.submissionId = submissionId;
        this.score = score;
        this.keptScore = keptScore;
        this.startedAt = startedAt;
        this.endAt = endAt;
        this.finishedAt = finishedAt;
        this.attempt = attempt;
        this.workflowState = workflowState;
        this.fudgePoints = fudgePoints;
        this.quizPointsPossible = quizPointsPossible;
        this.extraAttempts = extraAttempts;
        this.manuallyUnlocked = manuallyUnlocked;
        this.validationToken = validationToken;
        this.scoreBeforeRegrade = scoreBeforeRegrade;
        this.hasSeenResults = hasSeenResults;
        this.timeSpent = timeSpent;
        this.attemptsLeft = attemptsLeft;
        this.overdueAndNeedsSubmission = overdueAndNeedsSubmission;
        this.excused = excused;
        this.htmlUrl = htmlUrl;
    }
}
