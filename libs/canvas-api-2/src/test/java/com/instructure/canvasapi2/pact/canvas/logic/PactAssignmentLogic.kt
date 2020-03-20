/*
 * Copyright (C) 2020 - present Instructure, Inc.
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

package com.instructure.canvasapi2.pact.canvas.logic

import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentDueDate
import com.instructure.canvasapi2.models.AssignmentOverride
import io.pactfoundation.consumer.dsl.LambdaDslObject
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

//
// region AssignmentDueDate logic
//
fun LambdaDslObject.populateAssignmentDueDateFields() : LambdaDslObject {
    this
            .id("id")
            .stringMatcher("due_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .stringType("title")
            .stringMatcher("unlock_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .stringMatcher("lock_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .booleanType("base")

    return this;
}

fun assertAssignmentDueDatePopulated(description: String, assignmentDueDate: AssignmentDueDate) {
    assertNotNull("$description + id", assignmentDueDate.id)
    assertNotNull("$description + dueAt", assignmentDueDate.dueAt)
    assertNotNull("$description + title", assignmentDueDate.title)
    assertNotNull("$description + unlockAt", assignmentDueDate.unlockAt)
    assertNotNull("$description + lockAt", assignmentDueDate.lockAt)
    assertNotNull("$description + isBase", assignmentDueDate.isBase)
}
// endregion

//
// region AssignmentOverride logic
//

fun LambdaDslObject.populateAssignmentOverrideFields() : LambdaDslObject {
    this
            .id("id")
            .id("assignment_id")
            .stringType("title")
            .stringMatcher("due_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .booleanType("all_day")
            .stringMatcher("all_day_date", PACT_DATE_REGEX, "2020-01-23")
            .stringMatcher("unlock_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .stringMatcher("lock_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .array("student_ids") { arr ->
                arr.id() // Assume a single student, single id
            }
    // Could not get this to work. :-(
//            .minArrayLike("student_ids", 1) { arr ->
//                arr.id()
//            }
            //.id("groupId") TODO
            //.id("course_section_id") TODO

    return this;
}

fun assertAssignmentOverridePopulated(description: String, assignmentOverride: AssignmentOverride) {
    assertNotNull("$description + id", assignmentOverride.id)
    assertNotNull("$description + assignmentId", assignmentOverride.assignmentId)
    assertNotNull("$description + title", assignmentOverride.title)
    assertNotNull("$description + dueAt", assignmentOverride.dueAt)
    assertNotNull("$description + isAllDay", assignmentOverride.isAllDay)
    assertNotNull("$description + allDayDate", assignmentOverride.allDayDate)
    assertNotNull("$description + unlockAt", assignmentOverride.unlockAt)
    assertNotNull("$description + lockAt", assignmentOverride.lockAt)
    assertNotNull("$description + studentIds", assignmentOverride.studentIds)
    assertTrue(
            "$description + studentIds should have at least one element",
            assignmentOverride.studentIds.size>=1)
}
// endregion

data class PactAssignmentFieldConfig(
        val include_submission : Boolean = false,
        val include_assignment_visibility : Boolean = false,
        val include_all_dates : Boolean = false,
        val include_overrides : Boolean = false,
        val include_observed_users : Boolean = false,
        val has_lock_info : Boolean = false,
        val has_rubric : Boolean = false,
        val has_discussion : Boolean = false,
        val is_quiz : Boolean = false,
        val is_teacher : Boolean = false
) {
    companion object {
        fun fromQueryString(isTeacher: Boolean = false, query: String) : PactAssignmentFieldConfig {
            return PactAssignmentFieldConfig(
                    is_teacher = isTeacher,
                    include_submission = query.contains("=submission"),
                    include_assignment_visibility = query.contains("=assignment_visibility"),
                    include_all_dates = query.contains("=all_dates"),
                    include_overrides = query.contains("=overrides"),
                    include_observed_users = query.contains("=observed_users")
            )
        }
    }
}

fun LambdaDslObject.populateAssignmentFields(fieldConfig : PactAssignmentFieldConfig = PactAssignmentFieldConfig()) : LambdaDslObject {
    this
            .id("id")
            .stringType("name")
            .minArrayLike(
                    "submission_types",
                    1,
                    PactDslJsonRootValue.stringMatcher(
                            "discussion_topic|online_quiz|on_paper|none|external_tool|online_text_entry|online_url|online_upload|media_recording",
                            "online_text_entry"
                    ),
                    1
            )
            .stringMatcher("due_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .numberType("points_possible", 20)
            .id("course_id")
            .booleanType("grade_group_students_individually")
            .stringMatcher("grading_type", "pass_fail|percent|letter_grade|gpa_scale|points")
            .stringType("html_url")
            // url not supported by API?
            .array("rubric") { arr -> // Assume 1 rubric criterion field
                arr.`object`() { obj ->
                    obj.populateRubricCriterionFields()
                }
            }
            .booleanType("use_rubric_for_grading")
            .`object`("rubric_settings"){ obj ->
                obj.populateRubricSettingsFields()
            }
            .array("allowed_extensions") { arr ->
                arr.stringMatcher("docx|ppt|txt", "txt") // assume one allowed extension
            }
            .id("assignment_group_id")
            .id("position")
            .booleanType("peer_reviews")
            .booleanType("locked_for_user")
            .stringMatcher("lock_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .stringMatcher("unlock_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            // TODO: discussion_topic
            // TODO: needs_grading_count_by_section
            .booleanType("free_form_criterion_comments")
            .booleanType("published")
            .booleanType("muted")
            .id("group_category_id")
            .booleanType("only_visible_to_overrides")
            .booleanType("anonymous_peer_reviews")
            .booleanType("moderated_grading")
            .booleanType("anonymous_grading")
            // is_studio_enabled not supported by API?
            //.booleanType("user_submitted") // Not used by our code


    if(fieldConfig.include_submission)  {
        this.`object`("submission") { obj -> // optional
            obj.populateSubmissionFields()
        }
    }

    if(fieldConfig.include_all_dates) {
        this.array("all_dates") { arr -> // Assume one all_dates entry
            arr.`object`() {obj ->
                obj.populateAssignmentDueDateFields()
            }
        }
    }

    if(fieldConfig.include_overrides) {
        this.array("overrides") { arr -> // Assume one assignment override object
            arr.`object`() { obj ->
                obj.populateAssignmentOverrideFields()
            }
        }
    }

    if(fieldConfig.is_quiz) {
        this.id("quiz_id") // only for online_quiz submission type
    }

    if(fieldConfig.is_teacher) {
        this
                .numberType("needs_grading_count")
                .stringType("description")
                .booleanType("unpublishable")
    }
    else {
        this.stringType("lock_explanation")
    }

    return this
}

/**
 * Assert that an assignment object in a response has been properly populated, based on
 * PactAssignmentFieldConfig settings.
 */
fun assertAssignmentPopulated(description: String, assignment: Assignment, fieldConfig: PactAssignmentFieldConfig = PactAssignmentFieldConfig()) {

    assertNotNull("$description + id", assignment.id)
    assertNotNull("$description + name", assignment.name)
    assertNotNull("$description + submissionTypesRaw", assignment.submissionTypesRaw)
    assertNotNull("$description + dueAt", assignment.dueAt)
    assertNotNull("$description + pointsPossible", assignment.pointsPossible)
    assertNotNull("$description + courseId", assignment.courseId)
    assertNotNull("$description + isGradeGroupsIndividually", assignment.isGradeGroupsIndividually)
    assertNotNull("$description + gradingType", assignment.gradingType)
    assertNotNull("$description + htmlUrl", assignment.htmlUrl)
    assertNotNull("$description + rubric", assignment.rubric)
    assertTrue("$description + rubric count should be >= 1", assignment.rubric!!.size >= 1)
    assertRubricCriterionPopulated("$description + rubric", assignment.rubric!!.get(0))
    assertNotNull("$description + isUseRubricForGrading", assignment.isUseRubricForGrading)
    assertNotNull("$description + rubricSettings", assignment.rubricSettings)
    assertRubricSettingsPopulated("$description + rubricSettings", assignment.rubricSettings!!)
    assertNotNull("$description + allowedExtensions", assignment.allowedExtensions)
    assertNotNull("$description + assignmentGroupId", assignment.assignmentGroupId)
    assertNotNull("$description + position", assignment.position)
    assertNotNull("$description + anonymousPeerReviews", assignment.anonymousPeerReviews)
    assertNotNull("$description + lockedForUser", assignment.lockedForUser)
    assertNotNull("$description + lockAt", assignment.lockAt)
    assertNotNull("$description + unlockAt", assignment.unlockAt)
    assertNotNull("$description + freeFormCriterionComments", assignment.freeFormCriterionComments)
    assertNotNull("$description + published", assignment.published)
    assertNotNull("$description + muted", assignment.muted)
    assertNotNull("$description + groupCategoryId", assignment.groupCategoryId)
    //assertNotNull("$description + userSubmitted", assignment.userSubmitted)
    assertNotNull("$description + onlyVisibleToOverrides", assignment.onlyVisibleToOverrides)
    assertNotNull("$description + moderatedGrading", assignment.moderatedGrading)
    assertNotNull("$description + anonymousGrading", assignment.anonymousGrading)

    if(fieldConfig.include_submission)  {
        assertNotNull("$description + submission", assignment.submission)
    }

    if(fieldConfig.include_all_dates) {
        assertNotNull("$description + allDates", assignment.allDates)
        assertTrue(
                "$description + allDates should have at least one element",
                assignment.allDates.size >= 1)
        assertAssignmentDueDatePopulated("$description + allDates[0]", assignment.allDates[0])
    }

    if(fieldConfig.include_overrides) {
        assertNotNull("$description + overrides", assignment.overrides)
        assertTrue(
                "$description + overrides should have at least one element",
                assignment.overrides!!.size >= 1)
        assertAssignmentOverridePopulated("$description + overrides[0]", assignment.overrides!![0])
    }

    if(fieldConfig.is_quiz) {
        assertNotNull("$description + quizId", assignment.quizId)
    }

    if(fieldConfig.is_teacher) {
        assertNotNull("$description + description", assignment.description)
        assertNotNull("$description + needsGradingCount", assignment.needsGradingCount)
        assertNotNull("$description + unpublishable", assignment.unpublishable)
    }
    else {
        assertNotNull("$description + lockExplanation", assignment.lockExplanation)
    }

    // TODO: More in-depth checking of objects and arrays

}