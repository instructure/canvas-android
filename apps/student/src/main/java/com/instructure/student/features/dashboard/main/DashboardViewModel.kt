package com.instructure.student.features.dashboard.main

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.CourseGrade
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.student.R
import com.instructure.student.features.dashboard.main.itemviewmodels.DashboardCourseItemViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val courseManager: CourseManager,
    private val resources: Resources
) : ViewModel() {
    val data: LiveData<DashboardViewData>
        get() = _data
    private val _data = MutableLiveData<DashboardViewData>()

    val events: LiveData<Event<DashboardAction>>
        get() = _events
    private val _events = MutableLiveData<Event<DashboardAction>>()

    fun loadData(forceNetwork: Boolean = false) {
        viewModelScope.launch {
            val courses = courseManager.getCoursesAsync(forceNetwork).await().dataOrThrow

            val courseItems = courses.map {
                DashboardCourseItemViewModel(
                    DashboardCourseItemViewData(
                        it.imageUrl,
                        it.name,
                        it.courseCode,
                        getCourseGrade(it.getCourseGrade(false)),
                        ColorKeeper.getOrGenerateColor(it)
                    )
                ) {
                    _events.postValue(Event(DashboardAction.OpenCourse(it)))
                }
            }

            _data.postValue(DashboardViewData(courseItems))
        }
    }

    private fun getCourseGrade(courseGrade: CourseGrade?): String {
        return if(courseGrade == null || courseGrade.noCurrentGrade) {
            resources.getString(R.string.noGradeText)
        } else {
            val scoreString = NumberHelper.doubleToPercentage(courseGrade.currentScore, 2)
            "${if(courseGrade.hasCurrentGradeString()) courseGrade.currentGrade else ""} $scoreString"
        }
    }

    fun expandCourses() {

    }
}