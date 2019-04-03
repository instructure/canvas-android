package com.instructure.dataseeding.model

import com.google.gson.annotations.SerializedName

data class CreateObserveeWithCredentialsWrapper(
        val observee: CreateObserveeWithCredentials
)

data class CreateObserveeWithCredentials(
        @SerializedName("unique_id")
        val uniqueId: String,
        val password: String
)

data class ObserveeApiModel(
        val id: Long,
        val name: String,
        @SerializedName("short_name")
        val shortName: String,
        @SerializedName("sortable_name")
        val sortableName: String
)

data class ObserverAlertThresholdApiModel(
        val id: Long,
        @SerializedName("alert_type")
        val alertType: String,
        val threshold: String,
        @SerializedName("workflow_state")
        val workflowState: String,
        @SerializedName("user_id")
        val userId: Long,
        @SerializedName("observer_id")
        val observerId: Long
)

data class AddObserverAlertThresholdWrapper(
        @SerializedName("observer_alert_threshold")
        val observerAlertThreshold: AddObserverAlertThreshold
)

data class AddObserverAlertThreshold(
        @SerializedName("alert_type")
        val alertType: String,
        val threshold: String,
        @SerializedName("user_id")
        val userId: Long,
        @SerializedName("observer_id")
        val observerId: Long
)

data class ObserverAlertApiModel(
        val id: Long,
        @SerializedName("observer_alert_threshold_id")
        val observerAlertThresholdId: Long,
        @SerializedName("context_type")
        val contextType: String,
        @SerializedName("context_id")
        val contextId: Long,
        @SerializedName("alert_type")
        val alertType: String,
        @SerializedName("workflow_state")
        val workflowState: String,
        val title: String,
        @SerializedName("user_id")
        val userId: Long,
        @SerializedName("observer_id")
        val observerId: Long,
        @SerializedName("html_url")
        val htmlUrl: String
)
