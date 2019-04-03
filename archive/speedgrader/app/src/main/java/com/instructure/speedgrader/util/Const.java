/*
 * Copyright (C) 2016 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.speedgrader.util;

import com.instructure.speedgrader.BuildConfig;

public class Const {

    // Intent & Bundle Strings
    public final static String assignment         = "assignment";
    public final static String submission         = "submission";
    public final static String currentSubmission  = "currentSubmission";
    public final static String sections           = "sections";
    public final static String currentSectionId   = "currentSectionId";
    public final static String canvasContext      = "canvasContext";
    public final static String canvasContextId    = "canvasContextId";
    public final static String passedURI          = "passedURI";
    public static final String emptyMessage       = "emptyMessage";
    public static final String assignmentId       = "assignment_id";
    public static final String attempt            = "attempt";
    public static final String currentAttempts    = "currentAttempts";
    public static final String attachment         = "attachment";
    public static final String currentPosition    = "currentViewPagerPosition";
    public static final String isGroup            = "isGroup";

    // Other constants
    public static final String HTML         = "html";
    public static final String AUTHENTICATE = "authenticate";
    public static final String INTERNAL_URL = "internal_url";

    // Submission workflow states
    public static final String GRADED      = "graded";
    public static final String SUBMITTED   = "submitted";
    public static final String UNSUBMITTED = "unsubmitted";

    public static final String unsavedData  = "UNSAVED_DATA";

    // Comments
    public static final String COMMENT_HOLDER = "comment_holder";
    public static final String NO_COMMENT     = "NO_COMMENT";
    public static final String NO_POINT       = "NO_POINT";

    // User Settings
    public static final String SHOW_STUDENT_NAMES = "show_student_names";
    public static final String SHOW_UNGRADED_FIRST = "show_ungraded_first";
    public static final String VIEW_UNGRADED_COUNT = "view_ungraded_count";
    public static final String VIEW_HTML = "view_html";

    // Document Activity rotation items
    public static final String assignmentUsers = "assignmentUsers";
    public static final String assignmentSubmissions = "assignmentSubmissions";

    public static final String commentsCache = "commentsCache";
    public static final String editModeRubricItems = "editModeRubricItems";
    public static final String freeFormPointsCache = "freeFormPointsCache";
    public static final String editModeFreeFormRubricItems = "editModeFreeFormRubricItems";

    public static final String RUBRIC_FRAGMENT_TAG = "RUBRIC_FRAGMENT_TAG";
    public static final String COMMENTS_FRAGMENT_TAG = "COMMENTS_FRAGMENT_TAG";

    public static final String ASSIGNMENT = "assignment";
    public static final String ASSIGNMENTS = "assignments";
    public static final String ASSIGNMENT_ID = "assignmentId";
    public static final String UPDATED_ASSIGNMENTS = "UPDATED_ASSIGNMENTS";
    public static final int UPDATED_ASSIGNMENT_FLAGS = 1234;

    public static final String DIALOG_LISTENER = "dialog_listener";
    public static final String DIALOG_FRAGMENT_TAG = "edit_assignment_dialog_fragment";
    public static final String assignmentGroups = "assignmentGroups";
    public static final String checkedSubmissionTypes = "checkedSubmissionTypes";
    public static final String assignmentTitleCache = "assignmentTitleCache";
    public static final String pointsPossibleCache = "pointsPossibleCache";
    public static final String isNotifyUsers = "isNotifyUsers";
    public static final String dueDateCache = "dueDateCache";
    public static final String isMuted = "isMuted";

    public static final String isFreeForm = "isFreeForm";
    public static final String isSortByName = "isSortByName";
    public static final String isDrawerOpen = "isDrawerOpen";
    public static final String LIST_POSITION = "LIST_POSITION";
    public static final String nextPageURL = "nextPageURL";

    // Crocodocs
    public static final String crocodocURL = "crocodoc.com";
    public static final String SHOW_BOUNCE = "showBounce";


    public static final String SHOW_VIDEO_MESSAGE = "showVideoMessage";

    // Tutorial
    public static final String shouldShowTutorial = "shouldShowTutorial";

    public static final String rubricCriterion = "rubricCriterion";
    public static final String rubricCriterionRating = "rubricCriterionRating";
    public static final String PSPDFKIT_LICENSE_KEY = BuildConfig.PSPDFKIT_LICENSE_KEY;
}
