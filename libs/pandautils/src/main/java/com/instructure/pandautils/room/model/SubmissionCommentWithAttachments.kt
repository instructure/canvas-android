package com.instructure.pandautils.room.model

import androidx.room.Embedded
import androidx.room.Relation
import com.instructure.canvasapi2.db.entities.Attachment
import com.instructure.canvasapi2.db.entities.Author
import com.instructure.canvasapi2.db.entities.MediaComment
import com.instructure.canvasapi2.db.entities.SubmissionComment

data class SubmissionCommentWithAttachments(
    @Embedded
    val submissionComment: SubmissionComment,
    @Relation(
        parentColumn = "mediaCommentId",
        entityColumn = "mediaId"
    )
    val mediaComment: MediaComment?,
    @Relation(
        parentColumn = "id",
        entityColumn = "submissionCommentId"
    )
    val attachments: List<Attachment>?,
    @Relation(
        parentColumn = "authorId",
        entityColumn = "id"
    )
    val author: Author?
)