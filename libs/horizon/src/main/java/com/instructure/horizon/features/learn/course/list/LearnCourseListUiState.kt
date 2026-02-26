package com.instructure.horizon.features.learn.course.list

import androidx.annotation.StringRes
import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.platform.LoadingState

data class LearnCourseListUiState(
    val loadingState: LoadingState = LoadingState(),
    val coursesToDisplay: List<LearnCourseState> = emptyList(),
    val searchQuery: TextFieldValue = TextFieldValue(""),
    val updateSearchQuery: (TextFieldValue) -> Unit = {},
    val selectedFilterValue: LearnCourseFilterOption = LearnCourseFilterOption.All,
    val updateFilterValue: (LearnCourseFilterOption) -> Unit = {},
    val visibleItemCount: Int = 10,
    val increaseVisibleItemCount: () -> Unit = {},
)

data class LearnCourseState(
    val imageUrl: String? = null,
    val courseName: String = "",
    val courseId: Long = -1L,
    val progress: Double = 0.0,
)

enum class LearnCourseFilterOption(@StringRes val labelRes: Int) {
    All(R.string.learnCourseListFilterAll),
    NotStarted(R.string.learnCourseListFilterNotStarted),
    InProgress(R.string.learnCourseListFilterInProgress),
    Completed(R.string.learnCourseListFilterCompleted);

    companion object {
        internal fun Double.getProgressOption(): LearnCourseFilterOption {
            return when (this) {
                0.0 -> NotStarted
                in 0.0..<100.0 -> InProgress
                else -> Completed
            }
        }
    }
}