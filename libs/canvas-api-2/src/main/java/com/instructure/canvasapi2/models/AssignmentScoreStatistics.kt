package com.instructure.canvasapi2.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AssignmentScoreStatistics(
        var mean: Double,
        var min: Double,
        var max: Double
) : Parcelable