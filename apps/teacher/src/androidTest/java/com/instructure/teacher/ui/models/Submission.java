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

public class Submission {

    public int id;
    public int attempt;
    public int assignmentId;
    public int userId;
    public int graderId;
    public String body;
    public String url;
    public String grade;
    public String previewUrl;
    public String submissionType;
    public String workflowState;
    public Date submittedAt;
    public Date gradedAt;
    public Double score;
    public boolean excused;
    public boolean late;
    public boolean gradeMatchesCurrentSubmission;
    public ArrayList<Attachment> attachments;
    public ArrayList<Submission> submissionHistory;
    public ArrayList<SubmissionComment> submissionComments;

    public Submission(int id, int attempt, int assignmentId, int userId, int graderId,
                      String body, String url, String grade, String previewUrl, String submissionType, String workflowState,
                      Date submittedAt, Date gradedAt, Double score,
                      boolean excused, boolean late, boolean gradeMatchesCurrentSubmission, Attachment[] attachments,
                      Submission[] submissionHistory, SubmissionComment[] submissionComments) {
        this.id = id;
        this.attempt = attempt;
        this.assignmentId = assignmentId;
        this.userId = userId;
        this.graderId = graderId;
        this.body = body;
        this.url = url;
        this.grade = grade;
        this.previewUrl = previewUrl;
        this.submissionType = submissionType;
        this.workflowState = workflowState;
        this.submittedAt = submittedAt;
        this.gradedAt = gradedAt;
        this.score = score;
        this.excused = excused;
        this.late = late;
        this.gradeMatchesCurrentSubmission = gradeMatchesCurrentSubmission;
        this.attachments = new ArrayList<>(Arrays.asList(attachments));
        this.submissionHistory = new ArrayList<>(Arrays.asList(submissionHistory));
        this.submissionComments = new ArrayList<>(Arrays.asList(submissionComments));
    }
}
