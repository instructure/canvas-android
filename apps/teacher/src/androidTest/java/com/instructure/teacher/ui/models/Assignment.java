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

public class Assignment {
    // public ArrayList<Date> allDates; // trailing comma
    // public ArrayList<String> allowedExtensions; // trailing comma
    public boolean anonymousSubmissions;
    public int assignmentGroupId;
    // public ArrayList<Integer> assignmentVisibility; // trailing comma
    public boolean automaticPeerReviews;
    public int courseId;
    public Date createdAt;
    public String description;
    // public int discussionTopic; // may be null
    public Date dueAt;
    public boolean gradeGroupStudentIndividually;
    // public int gradingStandardId; // may be null
    public String gradingType;
    public int groupCategoryId;
    public boolean hasOverrides;
    public String htmlUrl;
    public int id;
    public Date lockAt;
    public boolean muted;
    public String name;
    public int needsGradingCount;
    // public ArrayList<Integer> needsGradingCountBySection; // trailing comma
    public boolean onlyVisibileToOverrides;
    // overrides // model not yet defined
    // public int peerReviewCount; // may be null
    public boolean peerReviews;
    // public Date peerReviewsAssignAt; // may be null
    public int pointsPossible;
    public int position;
    public boolean published;
    // public int quizId; // may be null
    // rubric // model not yet defined
    // rubric_settings // model not yet defined
    public ArrayList<String> submissionTypes;
    public Date unlockAt;
    public boolean unpublishable;
    public Date updatedAt;
    public boolean useRubricForGrading;

    public Assignment(boolean anonymousSubmissions, int assignmentGroupId, boolean automaticPeerReviews, int courseId,
                      Date createdAt, String description, Date dueAt, boolean gradeGroupStudentIndividually,
                      String gradingType, int groupCategoryId, boolean hasOverrides, String htmlUrl, int id, Date lockAt,
                      boolean muted, String name, int needsGradingCount, boolean onlyVisibileToOverrides, boolean peerReviews,
                      int pointsPossible, int position, boolean published, String[] submissionTypes, Date unlockAt,
                      boolean unpublishable, Date updatedAt, boolean useRubricForGrading) {
        this.anonymousSubmissions = anonymousSubmissions;
        this.assignmentGroupId = assignmentGroupId;
        this.automaticPeerReviews = automaticPeerReviews;
        this.courseId = courseId;
        this.createdAt = createdAt;
        this.description = description;
        this.dueAt = dueAt;
        this.gradeGroupStudentIndividually = gradeGroupStudentIndividually;
        this.gradingType = gradingType;
        this.groupCategoryId = groupCategoryId;
        this.hasOverrides = hasOverrides;
        this.htmlUrl = htmlUrl;
        this.id = id;
        this.muted = muted;
        this.lockAt = lockAt;
        this.name = name;
        this.needsGradingCount = needsGradingCount;
        this.onlyVisibileToOverrides = onlyVisibileToOverrides;
        this.peerReviews = peerReviews;
        this.pointsPossible = pointsPossible;
        this.position = position;
        this.published = published;
        this.submissionTypes = new ArrayList<>(Arrays.asList(submissionTypes));
        this.unlockAt = unlockAt;
        this.unpublishable = unpublishable;
        this.updatedAt = updatedAt;
        this.useRubricForGrading = useRubricForGrading;
    }

    public boolean locked() {
        if (lockAt == null) {
            return false;
        }

        Date now = new Date();
        if (lockAt.before(now) || lockAt.equals(now)) {
            return true;
        }
        return false;
    }
}
