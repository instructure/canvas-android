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
    val commentLibrarySuggestions: Boolean = false
)
