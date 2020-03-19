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

import com.instructure.canvasapi2.models.Assignment
import io.pactfoundation.consumer.dsl.LambdaDslObject

fun LambdaDslObject.populateSubmissionCommentFields() : LambdaDslObject {
    this
            .id("id")
            .id("author_id")
            .stringType("author_name")
            .stringType("author_pronouns")
            .stringType("comment")
            .stringMatcher("created_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")

    // TODO: media_comment, author.  attachments not supported by API?
    return this
}

data class PactSubmissionFieldConfig(
        val include_submission_history : Boolean = false,
        val include_submission_comments : Boolean = false,
        val include_rubric_assessment : Boolean = false,
        val include_assignment : Boolean = false,
        val include_visibility : Boolean = false,
        val include_course : Boolean = false,
        val include_user : Boolean = false,
        val include_group : Boolean = false,
        val submissionType: Assignment.SubmissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY
) {
    companion object {
        fun fromQuery(
                query: String,
                submissionType : Assignment.SubmissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY
        ): PactSubmissionFieldConfig {
            return PactSubmissionFieldConfig(
                    include_submission_history = query.contains("=submission_history"),
                    include_submission_comments = query.contains("=submission_comments"),
                    include_rubric_assessment = query.contains("=rubric_assessmeent"),
                    include_assignment = query.contains("=assignment"),
                    include_visibility = query.contains("=visibility"),
                    include_course = query.contains("=course"),
                    include_user = query.contains("=user"),
                    include_group = query.contains("=group"),
                    submissionType = submissionType
            )
        }
    }
}

fun LambdaDslObject.populateSubmissionFields(
        fieldConfig: PactSubmissionFieldConfig = PactSubmissionFieldConfig()
) : LambdaDslObject {
    this
            .id("id")
            .stringType("grade")
            .numberType("score")
            .id("attempt")
            .stringMatcher("submitted_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            // comment_created not supported by API?
            // media_content_type, media_comment_url, media_comment_display not supported by API?
            // submission_history, attachments not support by API
            .booleanType("grade_matches_current_submission")
            .stringMatcher("workflow_state", "submitted|unsubmitted|graded|pending_review", "unsubmitted")
            .stringMatcher("submission_type","online_text_entry|online_url|online_upload|media_recording|basic_lti_launch", "online_text_entry")
            .stringType("preview_url")
            .booleanType("late")
            .booleanType("excused")
            .booleanType("missing")
            // media_comment not supported by API?
            .id("assignment_id")
            .id("user_id")
            .id("grader_id")
            // discussion_entries not supported by API?
            // group not supported by API?
            .numberType("points_deducted")
            .numberType("entered_score") // supported?
            .stringType("entered_grade") // supported?
            .stringMatcher("posted_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")

    if(fieldConfig.include_submission_comments) {
        this.array("submission_comments") { array -> // Assume a single submission comment
            array.`object`() { obj ->
                obj.populateSubmissionCommentFields()
            }
        }
    }

    if(fieldConfig.include_rubric_assessment) {
        // TODO: rubric_assessment?
    }

    if(fieldConfig.include_assignment) {
        this.`object`("assignment") { obj -> // optional
            obj.populateAssignmentFields()
        }
    }

    if(fieldConfig.include_user) {
        this.`object`("user") { obj -> // optional
            obj.populateUserFields()
        }
    }

    if(fieldConfig.include_course) {
        this.`object`("course") { obj ->
            obj.populateUserFields()
        }
    }

    if(fieldConfig.submissionType == Assignment.SubmissionType.ONLINE_TEXT_ENTRY) {
        this.stringType("body")
    }
    else if(fieldConfig.submissionType == Assignment.SubmissionType.ONLINE_URL) {
        this.stringType("url")
    }

    return this
}

