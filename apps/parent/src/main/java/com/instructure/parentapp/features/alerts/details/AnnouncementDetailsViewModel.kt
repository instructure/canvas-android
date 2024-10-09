package com.instructure.parentapp.features.alerts.details

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.parentapp.features.dashboard.SelectedStudentHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnnouncementDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AnnouncementDetailsRepository,
    private val colorKeeper: ColorKeeper,
    private val selectedStudentHolder: SelectedStudentHolder
) : ViewModel() {

    private val courseId = savedStateHandle.get<Long>(AnnouncementDetailsFragment.COURSE_ID) ?: 0
    private val announcementId =
        savedStateHandle.get<Long>(AnnouncementDetailsFragment.ANNOUNCEMENT_ID) ?: 0

    private val _uiState = MutableStateFlow(AnnouncementDetailsUiState())
    val uiState = _uiState.asStateFlow()

    private var selectedStudent: User? = null

    init {
        Log.i("$courseId", "$announcementId")
        fetchDetails()
        viewModelScope.launch {
            selectedStudentHolder.selectedStudentState.collectLatest {
                studentChanged(it)
            }
        }
    }

    private fun studentChanged(student: User?) {
        if (selectedStudent != student) {
            selectedStudent = student
            _uiState.update {
                it.copy(
                    studentColor = colorKeeper.getOrGenerateUserColor(student).color()
                )
            }
        }
    }

    fun handleAction(action: AnnouncementDetailsAction) {
        when (action) {
            is AnnouncementDetailsAction.Refresh -> fetchDetails(true)
        }
    }

    private fun handleData(data: Any?) {
        when (data) {
            is DiscussionTopicHeader -> {
                _uiState.update {
                    it.copy(announcement = data)
                }
            }

            is Course -> {
                _uiState.update {
                    it.copy(course = data)
                }
            }
        }
    }

    private fun fetchDetails(forceNetwork: Boolean = false) {
        repository
            .getCourseAnnouncement(courseId, announcementId, forceNetwork)
            .onEach { result ->
                when (result) {
                    is DataResult.Success -> {
                        handleData(result.data)
                        _uiState.update {
                            it.copy(
                                isLoading = false
                            )
                        }
                    }

                    is DataResult.Fail -> {
                        _uiState.update {
                            it.copy(isLoading = false, isError = true)
                        }
                    }

                    is DataResult.Loading -> {
                        handleData(result.data)
                        _uiState.update {
                            it.copy(
                                isLoading = result.isLoading
                            )
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }
}