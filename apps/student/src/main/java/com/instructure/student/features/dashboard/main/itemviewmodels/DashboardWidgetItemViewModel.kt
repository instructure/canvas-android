package com.instructure.student.features.dashboard.main.itemviewmodels

import com.instructure.pandautils.binding.GroupItemViewModel
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.student.R
import com.instructure.student.features.dashboard.main.DashboardWidgetItemData

class DashboardWidgetItemViewModel(val data: DashboardWidgetItemData,
                                   collapsable: Boolean,
                                   collapsed: Boolean = collapsable,
                                   items: List<ItemViewModel>
) : GroupItemViewModel(collapsable, collapsed, items) {
    override val layoutId: Int = R.layout.item_dashboard_widget

    fun getTopItems(): List<ItemViewModel> {
        return if (items.size < 4) {
            items
        } else {
            items.subList(0, 3)
        }
    }
}