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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Quiz {
    // public ArrayList<Date> allDates;
    public int allowedAttempts;
    public int assignmentGroupId;
    public boolean cantGoBack;
    public String description;
    public Date dueAt;
    public Date hideCorrectAnswersAt;
    public boolean hideResults;
    public int id;
    public String ipFilter;
    public Date lockAt;
    public boolean oneQuestionAtATime;
    public boolean oneTimeResults;
    public double pointsPossible;
    public boolean published;
    public int questionCount;
    public ArrayList<String> questionTypes;
    public String quizType;
    public String scoringPolicy;
    public boolean showCorrectAnswers;
    public Date showCorrectAnswersAt;
    public boolean showCorrectAnswersLastAttempt;
    public boolean shuffleAnswers;
    public int timeLimit;
    public String title;
    public Date unlockAt;
    public boolean unpublishable;
    public ArrayList<QuizQuestion> questions;
    public ArrayList<AssignmentOverride> assignmentOverrides;

    public Quiz(int allowedAttempts, int assignmentGroupId, boolean cantGoBack, String description,
                Date dueAt, Date hideCorrectAnswersAt, boolean hideResults, int id, String ipFilter, Date lockAt,
                boolean oneQuestionAtATime, boolean oneTimeResults, double pointsPossible, boolean published,
                int questionCount, String[] questionTypes, String quizType, String scoringPolicy, boolean showCorrectAnswers,
                Date showCorrectAnswersAt, boolean showCorrectAnswersLastAttempt, boolean shuffleAnswers, int timeLimit,
                String title, Date unlockAt, boolean unpublishable, QuizQuestion[] questions, AssignmentOverride[] assignmentOverrides) {
        this.allowedAttempts = allowedAttempts;
        this.assignmentGroupId = assignmentGroupId;
        this.cantGoBack = cantGoBack;
        this.description = description;
        this.dueAt = dueAt;
        this.hideCorrectAnswersAt = hideCorrectAnswersAt;
        this.id = id;
        this.hideResults = hideResults;
        this.ipFilter = ipFilter;
        this.lockAt = lockAt;
        this.oneQuestionAtATime = oneQuestionAtATime;
        this.oneTimeResults = oneTimeResults;
        this.pointsPossible = pointsPossible;
        this.published = published;
        this.questionCount = questionCount;
        this.questionTypes = new ArrayList<>(Arrays.asList(questionTypes));
        this.quizType = quizType;
        this.scoringPolicy = scoringPolicy;
        this.showCorrectAnswers = showCorrectAnswers;
        this.showCorrectAnswersAt = showCorrectAnswersAt;
        this.showCorrectAnswersLastAttempt = showCorrectAnswersLastAttempt;
        this.shuffleAnswers = shuffleAnswers;
        this.timeLimit = timeLimit;
        this.title = title;
        this.unlockAt = unlockAt;
        this.unpublishable = unpublishable;
        this.questions = new ArrayList<>(Arrays.asList(questions));
        this.assignmentOverrides = new ArrayList<>(Arrays.asList(assignmentOverrides));
    }
}
