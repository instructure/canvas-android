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

public class Enrollment {
    public static final String STUDENT_ENROLLMENT = "StudentEnrollment";
    public static final String TEACHER_ENROLLMENT = "TeacherEnrollment";
    public static final String OBSERVER_ENROLLMENT = "ObserverEnrollment";
    public static final String TA_ENROLLMENT = "TaEnrollment";

    public int id;
    public int courseId;
    public int courseSectionId;
    public String enrollmentState;
    public String type;
    public int userId;
    public int associatedUserId;
    public String role;
    public Date updatedAt;
    public Date startAt;
    public Date endAt;
    public Date lastActivityAt;
    public int totalActivityTime;
    public String htmlUrl;
    public String grades;

    public Enrollment(int id, int courseId, int courseSectionId, String enrollmentState, String type,
                      int userId, int associatedUserId, String role, Date updatedAt, Date startAt,
                      Date endAt, Date lastActivityAt, int totalActivityTime, String htmlUrl, String grades) {
        this.id = id;
        this.courseId = courseId;
        this.courseSectionId = courseSectionId;
        this.enrollmentState = enrollmentState;
        this.type = type;
        this.userId = userId;
        this.associatedUserId = associatedUserId;
        this.role = role;
        this.updatedAt = updatedAt;
        this.startAt = startAt;
        this.endAt = endAt;
        this.lastActivityAt = lastActivityAt;
        this.totalActivityTime = totalActivityTime;
        this.htmlUrl = htmlUrl;
        this.grades = grades;
    }
}
