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
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import io.pactfoundation.consumer.dsl.LambdaDslObject
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

fun LambdaDslObject.populateSubmissionCommentFields(): LambdaDslObject {
    this
            .id("id")
            .id("author_id")
            .stringType("author_name")
            //.stringType("author_pronouns") // comes with author object
            .stringType("comment")
            .stringMatcher("created_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .`object`("author") { obj ->
                obj.populateAuthorFields()
            }
//            .array("attachments") { arr ->
//                arr.`object`() { obj ->
//                    obj.populateAttachmentFields()
//                }
//            }

    // TODO: Punt on attachments, media_comment.
    return this
}

fun assertSubmissionCommentPopulated(description: String, submissionComment: SubmissionComment) {
    assertNotNull("$description + id", submissionComment.id)
    assertNotNull("$description + authorId", submissionComment.authorId)
    assertNotNull("$description + authorName", submissionComment.authorName)
    //assertNotNull("$description + authorPronouns", submissionComment.authorPronouns)
    assertNotNull("$description + comment", submissionComment.comment)
    assertNotNull("$description + createdAt", submissionComment.createdAt)
    assertNotNull("$description + author", submissionComment.author)
    assertAuthorPopulated("$description + author", submissionComment.author!!)
//    assertNotNull("$description + attachments", submissionComment.attachments)
//    assertTrue("$description + attachments should have at least one element",
//            submissionComment.attachments.size > 0);
//    assertAttachmentPopulated("$description + attachments[0]", submissionComment.attachments[0])
}

data class PactSubmissionFieldConfig(
        val includeSubmissionHistory: Boolean = false,
        val includeSubmissionComments: Boolean = false,
        val includeRubricAssessment: Boolean = false,
        val includeAssignment: Boolean = false,
        val includeVisibility: Boolean = false,
        val includeCourse: Boolean = false, // Unused -- we don't track course in submission
        val includeUser: Boolean = false,
        val includeGroup: Boolean = false,
        val submissionType: Assignment.SubmissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY
) {
    companion object {
        fun fromQuery(
                query: String,
                submissionType: Assignment.SubmissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY
        ): PactSubmissionFieldConfig {
            return PactSubmissionFieldConfig(
                    includeSubmissionHistory = query.contains("=submission_history"),
                    includeSubmissionComments = query.contains("=submission_comments"),
                    includeRubricAssessment = query.contains("=rubric_assessment"),
                    includeAssignment = query.contains("=assignment"),
                    includeVisibility = query.contains("=visibility"),
                    includeCourse = query.contains("=course"), // unused in Android code
                    includeUser = query.contains("=user"),
                    includeGroup = query.contains("=group"),
                    submissionType = submissionType
            )
        }
    }
}

fun LambdaDslObject.populateSubmissionFields(
        fieldConfig: PactSubmissionFieldConfig = PactSubmissionFieldConfig()
): LambdaDslObject {
    this
            .id("id")
            .stringType("grade")
            .numberType("score")
            .id("attempt")
            .stringMatcher("submitted_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            // comment_created not supported by API?
            // media_content_type, media_comment_url, media_comment_display not supported by API?
            .booleanType("grade_matches_current_submission")
            .stringMatcher("workflow_state", "submitted|unsubmitted|graded|pending_review", "unsubmitted")
            .stringMatcher("submission_type", "online_text_entry|online_url|online_upload|media_recording|basic_lti_launch", "online_text_entry")
            .stringType("preview_url")
            .booleanType("late")
            .booleanType("excused")
            .booleanType("missing")
            // media_comment not supported by API?
            .id("assignment_id")
            .id("user_id")
            .id("grader_id")
            // TODO: Punt on discussion_entries; could be covered by another test
            .numberType("points_deducted")
            .numberType("entered_score") // supported?
            .stringType("entered_grade") // supported?
            .stringMatcher("posted_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")

    if (fieldConfig.includeGroup) {
        this.`object`("group") { obj ->
            obj.id("id")
            obj.stringType("name")

        }
    }

    if (fieldConfig.includeSubmissionComments) {
        this.array("submission_comments") { array ->
            // Assume a single submission comment
            array.`object`() { obj ->
                obj.populateSubmissionCommentFields()
            }
        }
    }

    if (fieldConfig.includeSubmissionHistory) {
        this.minArrayLike("submission_history", 1) { obj ->
            obj.populateSubmissionFields(PactSubmissionFieldConfig(submissionType = fieldConfig.submissionType))
        }
    }

    // Punting on this for now because there is no way to specify "I don't care what the value
    // of 'blah' is".  In real life, the rubric_assessment looks something like this:
    //
    //     "rubric_assessment": {
    //        "_7316": {
    //            "rating_id": "_6085",
    //            "comments": "",
    //            "points": 3.0
    //        }
    //    },
    //
    // That _7316 is an assessment id, and it's not possible to define it as "just some string".
//    if(fieldConfig.include_rubric_assessment) {
//        this.`object`("rubric_assessment") { obj ->
//            obj.`object`("blah") { inner ->
//                inner.stringType("rating_id")
//                inner.stringType("comments")
//                inner.numberType("points")
//            }
//        }
//    }

    if (fieldConfig.includeAssignment) {
        this.`object`("assignment") { obj ->
            // optional
            obj.populateAssignmentFields()
        }
    }

    if (fieldConfig.includeUser) {
        this.`object`("user") { obj ->
            // optional
            obj.populateUserFields()
        }
    }

    if (fieldConfig.submissionType == Assignment.SubmissionType.ONLINE_TEXT_ENTRY) {
        this.stringType("body")
    } else if (fieldConfig.submissionType == Assignment.SubmissionType.ONLINE_URL) {
        this.stringType("url")
    } else if (fieldConfig.submissionType == Assignment.SubmissionType.ONLINE_UPLOAD) {
        this.array("attachments") { arr ->
            arr.`object`() { obj ->
                obj.populateAttachmentFields()
            }
        }
    }

    return this
}

fun assertSubmissionPopulated(
        description: String,
        submission: Submission,
        fieldConfig: PactSubmissionFieldConfig = PactSubmissionFieldConfig()
) {
    assertNotNull("$description + id", submission.id)
    assertNotNull("$description + grade", submission.grade)
    assertNotNull("$description + score", submission.score)
    assertNotNull("$description + attempt", submission.attempt)
    assertNotNull("$description + submittedAt", submission.submittedAt)
    assertNotNull("$description + isGradeMatchesCurrentSubmission", submission.isGradeMatchesCurrentSubmission)
    assertNotNull("$description + workflowState", submission.workflowState)
    assertNotNull("$description + submissionType", submission.submissionType)
    assertNotNull("$description + previewUrl", submission.previewUrl)
    assertNotNull("$description + late", submission.late)
    assertNotNull("$description + excused", submission.excused)
    assertNotNull("$description + missing", submission.missing)
    assertNotNull("$description + assignmentId", submission.assignmentId)
    assertNotNull("$description + userId", submission.userId)
    assertNotNull("$description + graderId", submission.graderId)
    assertNotNull("$description + pointsDeducted", submission.pointsDeducted)
    assertNotNull("$description + enteredScore", submission.enteredScore)
    assertNotNull("$description + enteredGrade", submission.enteredGrade)
    assertNotNull("$description + postedAt", submission.postedAt)

    if (fieldConfig.includeGroup) {
        assertNotNull("$description + group", submission.group)
        assertNotNull("$description + group.id", submission.group!!.id)
        assertNotNull("$description + group.name", submission.group!!.name)
    }

    if (fieldConfig.includeSubmissionComments) {
        assertNotNull("$description + submissionComments", submission.submissionComments)
        assertTrue(
                "$description + submissionComments should have at least one comment",
                submission.submissionComments.size >= 1)
        submission.submissionComments.forEach() { sc ->
            assertSubmissionCommentPopulated("$description + submissionComments[?]", sc)
        }
    }

    if (fieldConfig.includeSubmissionHistory) {
        assertNotNull("$description + submissionHistory", submission.submissionHistory)
        assertTrue(
                "$description + submissionHistory should have at least one element",
                submission.submissionHistory.size >= 1)
        submission.submissionHistory.forEach() { item ->
            assertSubmissionPopulated(
                    description = "$description + submissionHistory[?]",
                    submission = item!!,
                    fieldConfig = PactSubmissionFieldConfig(submissionType = fieldConfig.submissionType))
        }
    }

    if (fieldConfig.includeRubricAssessment) {
        // TODO: rubric_assessment?
    }

    if (fieldConfig.includeAssignment) {
        assertNotNull("$description + assignment", submission.assignment)
        assertAssignmentPopulated("$description + assignment", submission.assignment!!)
    }

    if (fieldConfig.includeUser) {
        assertNotNull("$description + user", submission.user)
        assertUserPopulated("$description + user", submission.user!!)
    }

    if (fieldConfig.submissionType == Assignment.SubmissionType.ONLINE_TEXT_ENTRY) {
        assertNotNull("$description + body", submission.body!!)
    } else if (fieldConfig.submissionType == Assignment.SubmissionType.ONLINE_URL) {
        assertNotNull("$description + url", submission.url!!)
    } else if (fieldConfig.submissionType == Assignment.SubmissionType.ONLINE_UPLOAD) {
        assertNotNull("$description + attachments", submission.attachments)
        assertTrue(
                "$description + attachments should have at least one element",
                submission.attachments.size >= 1)
        assertAttachmentPopulated("$description + attachments[0]", submission.attachments[0])
    }


}