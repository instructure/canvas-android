package com.instructure.student.features.people.list

import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.depaginate

class PeopleListNetworkDataSource(
    private val api: UserAPI.UsersInterface
) : PeopleListDataSource {

    override suspend fun loadPeople(canvasContext: CanvasContext, forceNetwork: Boolean): List<User> {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        return api.getFirstPagePeopleList(canvasContext.id, canvasContext.apiContext(), restParams)
            .depaginate { api.getNextPeoplePagesList(it, restParams) }.dataOrThrow
    }
}