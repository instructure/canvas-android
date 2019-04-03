//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//


package com.instructure.dataseeding.model

import com.google.gson.annotations.SerializedName

data class SubmissionApiModel (
        val id: Long,
        var url: String?,
        var body: String?,
        @SerializedName("user_id")
        var userId: Long,
        var late: Boolean = false,
        var excused: Boolean = false,
        @SerializedName("submission_comments")
        val submissionComments: List<SubmissionCommentApiModel>,
        var grade: String?,
        // This field is not passed back from the API and is not meant to be
        // serialized.  It is only populated in certain test situations and it
        // is OK for it to be null.
        @Transient
        var submissionAttachments: List<AttachmentApiModel>? = null
)

// OkReplay/Ditto don't like to traffic in Lists of things, so we made this extraneous class to
// represent a list of SubmissionApiModels.
data class SubmissionListApiModel (
        @SerializedName("submission_list")
        val submissionList : List<SubmissionApiModel>
)

data class SubmitCourseAssignmentSubmissionWrapper(
        val submission: SubmitCourseAssignmentSubmission,
        var body: String? = null
)

data class SubmissionCommentApiModel(
        @SerializedName("author_name")
        val authorName: String,
        val comment: String,
        val attachments: List<AttachmentApiModel>?
)

data class CreateSubmissionComment(
        @SerializedName("text_comment")
        val comment: String,

        @SerializedName("file_ids")
        val fileIds: List<Long>? = null
)

data class CreateSubmissionCommentWrapper(
        val comment: CreateSubmissionComment
)

data class SubmitCourseAssignmentSubmission(
        @SerializedName("submission_type")
        var submissionType: String? = null,
        var body: String? = null,
        var url: String? = null,

        @SerializedName("file_ids")
        var fileIds: List<Long>? = null
)

data class GradeSubmission(
        @SerializedName("posted_grade")
        val grade: String,

        @SerializedName("excuse")
        val excused: Boolean
)

data class GradeSubmissionWrapper(
        val submission: GradeSubmission
)
