package com.instructure.canvasapi2.models

import com.instructure.canvasapi2.SubmissionCommentsQuery


data class SubmissionCommentsResponseWrapper(
    val data: SubmissionCommentsQuery.Data,
    val comments: List<SubmissionCommentsQuery.Node1>
)
