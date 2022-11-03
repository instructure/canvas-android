package com.instructure.student.features.dashboard.main.itemviewmodels

import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.student.R
import com.instructure.student.features.dashboard.main.DashboardAssignmentItemViewData

class DashboardAssignmentItemViewModel(
    val data: DashboardAssignmentItemViewData
) : ItemViewModel {
    override val layoutId: Int = R.layout.item_dashboard_assignment
}