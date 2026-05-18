package com.instructure.canvasapi2.models

import com.google.gson.annotations.SerializedName

data class UserSettings(
    @SerializedName("manual_mark_as_read")
    val manualMarkAsRead: Boolean = false,
    @SerializedName("collapse_global_nav")
    val collapseGlobalNav: Boolean = false,
    @SerializedName("hide_dashcard_color_overlays")
    val hideDashCardColorOverlays: Boolean = false,
    @SerializedName("comment_library_suggestions_enabled")
    val commentLibrarySuggestions: Boolean = false,
    @SerializedName("usage_metrics")
    val usageMetrics: String? = null,
    @SerializedName("pendo_mobile_student_api_key")
    val pendoMobileStudentApiKey: String? = null,
    @SerializedName("pendo_mobile_teacher_api_key")
    val pendoMobileTeacherApiKey: String? = null,
    @SerializedName("pendo_mobile_parent_api_key")
    val pendoMobileParentApiKey: String? = null
) {
    companion object {
        const val USAGE_METRICS_TRACK = "track_usage"
        const val USAGE_METRICS_NO_TRACK = "no_track_usage"
        const val USAGE_METRICS_ASK_FOR_CONSENT = "ask_for_consent"
    }
}
