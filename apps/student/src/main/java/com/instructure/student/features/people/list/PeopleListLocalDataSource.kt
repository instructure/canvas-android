package com.instructure.student.features.people.list

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.User

class PeopleListLocalDataSource(): PeopleListDataSource {
    override suspend fun loadPeople(canvasContext: CanvasContext, forceNetwork: Boolean): List<User> {
        return emptyList()
    }
}