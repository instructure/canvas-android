package com.instructure.student.features.calendar

import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.features.calendar.CalendarRouter
import com.instructure.student.activity.NavigationActivity

class StudentCalendarRouter(val activity: FragmentActivity) : CalendarRouter {
    override fun openNavigationDrawer() {
        (activity as? NavigationActivity)?.openNavigationDrawer()
    }
}