package com.instructure.dataseeding.model

import com.google.gson.annotations.SerializedName

data class PlannerNoteApiModel(
    val id: String? = null,
    val title: String,
    var details: String? = null,
    @SerializedName("user_id")
    val userId: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("todo_date")
    val todoDate: String? = null,
    @SerializedName("updated_at")
    val lastUpdatedDate: String? = null,
    @SerializedName("workflow_state")
    val workflowState: String? = null,
)