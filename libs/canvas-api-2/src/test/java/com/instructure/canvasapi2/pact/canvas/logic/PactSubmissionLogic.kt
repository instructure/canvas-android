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

import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Author
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import io.pactfoundation.consumer.dsl.LambdaDslObject
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import java.util.*

fun LambdaDslObject.populateAuthorFields() : LambdaDslObject {
    this
            .id("id")
            .stringType("display_name")
            .stringType("avatar_image_url")
            .stringType("html_url")
            .stringType("pronouns")

    return this
}

fun assertAuthorPopulated(description: String, author: Author) {
    assertNotNull("$description + id", author.id)
    assertNotNull("$description + displayName", author.displayName)
    assertNotNull("$description + avatarImageUrl", author.avatarImageUrl)
    assertNotNull("$description + htmlUrl", author.htmlUrl)
    assertNotNull("$description + pronouns", author.pronouns)
}

fun LambdaDslObject.populateSubmissionCommentFields() : LambdaDslObject {
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

    // TODO: media_comment.  attachments not supported by API?
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
}

fun LambdaDslObject.populateAttachmentFields(): LambdaDslObject {
    this
            .id("id")
            .stringType("content-type")
            .stringType("filename")
            .stringType("display_name")
            .stringType("url")
            // .stringType("thumbnail_url") // TODO: Punt
            // .stringType("preview_url") // TODO: Punt
            .stringMatcher("created_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .id("size") // long whole number

    return this;
}

fun assertAttachmentPopulated(description: String, attachment: Attachment) {
    assertNotNull("$description + id", attachment.id)
    assertNotNull("$description + contentType", attachment.contentType)
    assertNotNull("$description + filename", attachment.filename)
    assertNotNull("$description + displayName", attachment.displayName)
    assertNotNull("$description + url", attachment.url)
    assertNotNull("$description + createdAt", attachment.createdAt)
    assertNotNull("$description + size", attachment.size)

}

data class PactSubmissionFieldConfig(
        val include_submission_history : Boolean = false,
        val include_submission_comments : Boolean = false,
        val include_rubric_assessment : Boolean = false,
        val include_assignment : Boolean = false,
        val include_visibility : Boolean = false,
        val include_course : Boolean = false, // Unused -- we don't track course in submission
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
                    include_rubric_assessment = query.contains("=rubric_assessment"),
                    include_assignment = query.contains("=assignment"),
                    include_visibility = query.contains("=visibility"),
                    include_course = query.contains("=course"), // unused in Android code
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
            // attachments not support by API
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

    if(fieldConfig.include_submission_history) {
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
    // That _7316 is an id, and it's not possible to define it as "just some string".
//    if(fieldConfig.include_rubric_assessment) {
//        this.`object`("rubric_assessment") { obj ->
//            obj.`object`("blah") { inner ->
//                inner.stringType("rating_id")
//                inner.stringType("comments")
//                inner.numberType("points")
//            }
//        }
//    }

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

    if(fieldConfig.submissionType == Assignment.SubmissionType.ONLINE_TEXT_ENTRY) {
        this.stringType("body")
    }
    else if(fieldConfig.submissionType == Assignment.SubmissionType.ONLINE_URL) {
        this.stringType("url")
    }
    else if(fieldConfig.submissionType == Assignment.SubmissionType.ONLINE_UPLOAD) {
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

    if(fieldConfig.include_submission_comments) {
        assertNotNull("$description + submissionComments", submission.submissionComments)
        assertTrue(
                "$description + submissionComments should have at least one comment",
                submission.submissionComments.size >= 1)
        submission.submissionComments.forEach() { sc ->
            assertSubmissionCommentPopulated("$description + submissionComments[?]", sc)
        }
    }

    if(fieldConfig.include_submission_history) {
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

    if(fieldConfig.include_rubric_assessment) {
        // TODO: rubric_assessment?
    }

    if(fieldConfig.include_assignment) {
        assertNotNull("$description + assignment", submission.assignment)
        assertAssignmentPopulated("$description + assignment", submission.assignment!!)
    }

    if(fieldConfig.include_user) {
        assertNotNull("$description + user", submission.user)
        assertUserPopulated("$description + user", submission.user!!)
    }

    if(fieldConfig.submissionType == Assignment.SubmissionType.ONLINE_TEXT_ENTRY) {
        assertNotNull("$description + body", submission.body!!)
    }
    else if(fieldConfig.submissionType == Assignment.SubmissionType.ONLINE_URL) {
        assertNotNull("$description + url", submission.url!!)
    }
    else if(fieldConfig.submissionType == Assignment.SubmissionType.ONLINE_UPLOAD) {
        assertNotNull("$description + attachments", submission.attachments)
        assertTrue(
                "$description + attachments should have at least one element",
                submission.attachments.size >= 1)
        assertAttachmentPopulated("$description + attachments[0]", submission.attachments[0])
    }


}