package com.instructure.student.features.ai.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SummaryQuestions(
    val question: String,
    val choices: List<String>,
    val answer: String,
    val userAnswer: String? = null
) : Parcelable