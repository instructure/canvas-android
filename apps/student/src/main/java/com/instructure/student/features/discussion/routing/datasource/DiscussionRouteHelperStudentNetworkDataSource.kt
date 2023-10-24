package com.instructure.student.features.discussion.routing.datasource

import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.isCourse
import com.instructure.pandautils.utils.isGroup

class DiscussionRouteHelperStudentNetworkDataSource(
    private val discussionApi: DiscussionAPI.DiscussionInterface,
    private val groupApi: GroupAPI.GroupInterface,
    private val featuresApi: FeaturesAPI.FeaturesInterface,
    private val featureFlagProvider: FeatureFlagProvider,
): DiscussionRouteHelperStudentDataSource {

    suspend fun getEnabledFeaturesForCourse(canvasContext: CanvasContext, forceNetwork: Boolean): Boolean {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return if (canvasContext.isCourse) {
            val featureFlags = featuresApi.getEnabledFeaturesForCourse(canvasContext.id, params).dataOrNull
            featureFlags?.contains("react_discussions_post") ?: false && featureFlagProvider.getDiscussionRedesignFeatureFlag()
        } else if (canvasContext.isGroup) {
            val group = canvasContext as Group
            if (group.courseId == 0L) {
                featureFlagProvider.getDiscussionRedesignFeatureFlag()
            } else {
                val featureFlags = featuresApi.getEnabledFeaturesForCourse(group.courseId, params).dataOrNull
                featureFlags?.contains("react_discussions_post") ?: false && featureFlagProvider.getDiscussionRedesignFeatureFlag()
            }
        } else {
            false
        }
    }

    override suspend fun getDiscussionTopicHeader(canvasContext: CanvasContext, discussionTopicHeaderId: Long, forceNetwork: Boolean): DiscussionTopicHeader? {
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return discussionApi.getDiscussionTopicHeader(canvasContext.apiContext(), canvasContext.id, discussionTopicHeaderId, params).dataOrNull
    }

    override suspend fun getAllGroups(discussionTopicHeader: DiscussionTopicHeader, userId: Long, forceNetwork: Boolean): List<Group> {
        val params = RestParams(isForceReadFromNetwork = forceNetwork, usePerPageQueryParam = true)
        return groupApi.getFirstPageGroups(params).depaginate { nextUrl -> groupApi.getNextPageGroups(nextUrl, params) }.dataOrNull.orEmpty()
    }
}