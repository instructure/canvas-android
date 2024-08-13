package com.instructure.parentapp.features.calendartodo

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.pandautils.features.calendartodo.details.ToDoRouter
import com.instructure.parentapp.util.navigation.Navigation

class ParentToDoRouter(private val activity: FragmentActivity, private val navigation: Navigation) : ToDoRouter {
    override fun openEditToDo(plannerItem: PlannerItem) {
        navigation.navigate(activity, navigation.updateToDoRoute(plannerItem))
    }
}