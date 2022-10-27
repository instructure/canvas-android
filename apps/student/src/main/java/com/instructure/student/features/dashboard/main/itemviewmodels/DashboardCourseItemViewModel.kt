package com.instructure.student.features.dashboard.main.itemviewmodels

import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.student.R
import com.instructure.student.features.dashboard.main.DashboardCourseItemViewData

class DashboardCourseItemViewModel(
    val data: DashboardCourseItemViewData,
    val open: () -> Unit
) : ItemViewModel {
    override val layoutId: Int = R.layout.item_dashboard_course
}