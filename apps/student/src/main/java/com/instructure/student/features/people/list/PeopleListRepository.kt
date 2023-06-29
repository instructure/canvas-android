package com.instructure.student.features.people.list

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.NetworkStateProvider

class PeopleListRepository(
        peopleListLocalDataSource: PeopleListLocalDataSource,
        peopleListNetworkDataSource: PeopleListNetworkDataSource,
        networkStateProvider: NetworkStateProvider,
) : Repository<PeopleListDataSource>(peopleListLocalDataSource, peopleListNetworkDataSource, networkStateProvider) {

    suspend fun loadPeople(canvasContext: CanvasContext, forceNetwork: Boolean): List<User> {
        return dataSource.loadPeople(canvasContext, forceNetwork)
    }
}