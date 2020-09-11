package com.instructure.canvasapi2.models

import android.content.Context
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.R
import com.instructure.canvasapi2.utils.toDate
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
data class AssignmentScoreStatistics(
        var mean: Double,
        var min: Double,
        var max: Double
) : Parcelable