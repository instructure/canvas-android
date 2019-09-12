package com.instructure.dataseeding.model

import com.google.gson.annotations.SerializedName

data class CreateModuleWrapper(
        val module: CreateModule
)

data class CreateModule(
        val name: String,
        @SerializedName("unlock_at")
        val unlockAt: String? = null
)

data class UpdateModuleWrapper(
        val module: UpdateModule
)

data class UpdateModule(
        val published: Boolean
)

data class ModuleApiModel (
        val id: Long,
        val name: String,
        @SerializedName("unlock_at")
        val unlockAt: String? = null,
        val published: Boolean
)

data class CreateModuleItemWrapper(
        @SerializedName("module_item")
        val moduleItem: CreateModuleItem
)

// Enumerates possible values for module item type
enum class ModuleItemTypes(val stringVal: String) {
        FILE("File"),
        PAGE("Page"),
        DISCUSSION("Discussion"),
        ASSIGNMENT("Assignment"),
        QUIZ("Quiz"),
        SUB_HEADER("SubHeader"),
        EXTERNAL_URL("ExternalUrl"),
        EXTERNAL_TOOL("ExternalTool")
}

data class CreateModuleItem(
        val title: String?,
        val type: String, // File, Page, Discussion, Assignment, Quiz, SubHeader, ExternalUrl, ExternalTool
        @SerializedName("content_id")
        val contentId: String?,
        @SerializedName("page_url")
        val pageUrl: String? = null // Only need this for Page items
)

data class ModuleItemApiModel(
        val id: Long,
        @SerializedName("module_id")
        val moduleId: Long,
        val title: String,
        val type: String,
        @SerializedName("content_id")
        val contentId: Long
)