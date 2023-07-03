package com.instructure.student.features.people.list

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.room.offline.facade.PeopleFacade

class PeopleListLocalDataSource(private val peopleFacade: PeopleFacade): PeopleListDataSource {
    override suspend fun loadPeople(canvasContext: CanvasContext, forceNetwork: Boolean): List<User> {
        return peopleFacade.getPeopleByCourseId(canvasContext.id)
    }
}