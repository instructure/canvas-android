package com.instructure.pandautils.features.inbox.compose

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.RecipientAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.depaginate
import javax.inject.Inject

interface InboxComposeRepository {
    suspend fun getCourses(): List<Course>
    suspend fun getGroups(): List<Group>
    suspend fun getRecipients(searchQuery: String, context: CanvasContext): List<Recipient>
}

class InboxComposeRepositoryImpl @Inject constructor(
    private val courseAPI: CourseAPI.CoursesInterface,
    private val groupAPI: GroupAPI.GroupInterface,
    private val recipientAPI: RecipientAPI.RecipientInterface,
): InboxComposeRepository {
    override suspend fun getCourses(): List<Course> {
        val params = RestParams(usePerPageQueryParam = true)

        return courseAPI.getFirstPageCourses(params).depaginate {
            courseAPI.next(it, params)
        }.dataOrNull ?: emptyList()
    }

    override suspend fun getGroups(): List<Group> {
        val params = RestParams(usePerPageQueryParam = true)

        return groupAPI.getFirstPageGroups(params).depaginate {
            groupAPI.getNextPageGroups(it, params)
        }.dataOrNull ?: emptyList()
    }

    override suspend fun getRecipients(searchQuery: String, context: CanvasContext): List<Recipient> {
        val params = RestParams(usePerPageQueryParam = true)
        return recipientAPI.getFirstPageRecipientList(
            searchQuery = searchQuery,
            context = context.apiContext(),
            restParams = params,
        ).depaginate {
            recipientAPI.getNextPageRecipientList(it, params)
        }.dataOrNull ?: emptyList()
    }
}