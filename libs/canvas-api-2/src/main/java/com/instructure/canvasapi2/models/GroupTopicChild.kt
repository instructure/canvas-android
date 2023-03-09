package com.instructure.canvasapi2.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GroupTopicChild(
        val id: Long,
        @SerializedName("group_id")
        val groupId: Long
) : Parcelable
