package com.instructure.pandautils.features.inbox.coursepicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoursePickerViewModel @Inject constructor(
    private val repository: CoursePickerRepository
): ViewModel() {

    private var courseState = State.LOADING
    private var _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses = _courses.asStateFlow()

    private var groupState = State.LOADING
    private var _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups = _groups.asStateFlow()

    val state = MutableStateFlow(State.LOADING)

    var listener: CoursePickerFragment.CoursePickerListener? = null

    init {
        loadData()
    }

    fun loadData() {
        getCourses()
        getGroups()
    }

    private fun getCourses() {
        viewModelScope.launch {
            courseState = State.LOADING
            updateState()

            _courses.emit(repository.getCourses())

            courseState = State.DATA
            updateState()
        }
    }

    private fun getGroups() {
        viewModelScope.launch {
            groupState = State.LOADING
            updateState()

            _groups.emit(repository.getGroups())

            groupState = State.DATA
            updateState()
        }
    }

    private suspend fun updateState() {
        state.emit(courseState.combine(groupState))
    }

    enum class State {
        LOADING,
        DATA
        ;
        fun combine(other: State) = if (this == LOADING || other == LOADING) LOADING else DATA
    }
}