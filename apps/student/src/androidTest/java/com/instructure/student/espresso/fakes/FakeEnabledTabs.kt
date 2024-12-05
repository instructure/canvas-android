package com.instructure.student.espresso.fakes

import com.instructure.interactions.router.Route
import com.instructure.student.router.EnabledTabs

class FakeEnabledTabs: EnabledTabs {
    override suspend fun initTabs() {}

    override fun isPathTabNotEnabled(route: Route?): Boolean {
        return false
    }
}