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
